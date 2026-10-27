package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.errors.SignatureRequirementsNotMetException;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.RevocationOrigin;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.revocation.ocsp.OCSP;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSourceAlternateUrlsSupport;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CachedRevocationSourceTest {
    private static TestPki pki;
    private static CertificateToken certificate;
    private static CertificateToken issuer;

    @BeforeAll
    static void certificates() {
        pki = TestPki.create();
        certificate = new CertificateToken(pki.signer(TestPki.JANA).certificate());
        issuer = new CertificateToken(pki.caCertificate());
    }

    @Test
    void crlEvidenceIsReusedButMutableTokensAreNotShared() {
        var requests = new AtomicInteger();
        CRLSource online = (cert, ca) -> {
            requests.incrementAndGet();
            return pki.crlSource(null).getRevocationToken(cert, ca);
        };
        var cached = CachedRevocationSource.crl(online, Clock.systemUTC());
        var first = cached.getRevocationToken(certificate, issuer);
        first.setSourceURL("modified by caller");
        var second = cached.getRevocationToken(certificate, issuer);

        assertEquals(1, requests.get());
        assertNotSame(first, second);
        assertNotEquals(first.getSourceURL(), second.getSourceURL());
        assertArrayEquals(first.getEncoded(), second.getEncoded());
        assertEquals(CertificateStatus.GOOD, second.getStatus());
        assertEquals(RevocationOrigin.CACHED, second.getExternalOrigin());
        assertTrue(second.isValid());
    }

    @Test
    void ocspExpiresAtNextUpdateAndNeverFallsBackToStaleEvidence() {
        var clock = new TestClock();
        var response = new AtomicReference<>(ocsp(clock, -60, 10));
        var requests = new AtomicInteger();
        OCSPSource online = (cert, ca) -> { requests.incrementAndGet(); return response.get(); };
        var cached = CachedRevocationSource.ocsp(online, clock);

        var first = cached.getRevocationToken(certificate, issuer);
        var second = cached.getRevocationToken(certificate, issuer);
        assertNotSame(first, second);
        assertArrayEquals(first.getEncoded(), second.getEncoded());
        assertEquals(CertificateStatus.GOOD, second.getStatus());
        assertEquals(RevocationOrigin.CACHED, second.getExternalOrigin());
        assertEquals(1, requests.get());

        clock.advance(Duration.ofSeconds(10));
        response.set(null);
        assertNull(cached.getRevocationToken(certificate, issuer));
        response.set(ocsp(clock, -1, 100));
        assertNotNull(cached.getRevocationToken(certificate, issuer));
        assertEquals(3, requests.get(), "neither stale evidence nor a failed download is cached");
    }

    @Test
    void evenLongLivedEvidenceIsRefetchedAfterFiveMinutes() {
        var clock = new TestClock();
        var requests = new AtomicInteger();
        OCSPSource online = (cert, ca) -> { requests.incrementAndGet(); return ocsp(clock, -60, 3600); };
        var cached = CachedRevocationSource.ocsp(online, clock);
        cached.getRevocationToken(certificate, issuer);
        clock.advance(Duration.ofSeconds(299));
        cached.getRevocationToken(certificate, issuer);
        assertEquals(1, requests.get());
        clock.advance(Duration.ofSeconds(1));
        cached.getRevocationToken(certificate, issuer);
        assertEquals(2, requests.get());
    }

    @Test
    void crlIsRefreshedAndNewRevocationsAreNotHiddenAfterCacheExpires() {
        var signer = pki.issue("Newly revoked cache signer", TestPki.Kind.QESIG);
        var cert = new CertificateToken(signer.certificate());
        var clock = new TestClock();
        var requests = new AtomicInteger();
        CRLSource online = (c, ca) -> { requests.incrementAndGet(); return pki.crlSource(null).getRevocationToken(c, ca); };
        var cached = CachedRevocationSource.crl(online, clock);
        assertEquals(CertificateStatus.GOOD, cached.getRevocationToken(cert, issuer).getStatus());
        pki.revoke(signer);
        clock.advance(Duration.ofMinutes(5));
        assertEquals(CertificateStatus.REVOKED, cached.getRevocationToken(cert, issuer).getStatus());
        assertEquals(CertificateStatus.REVOKED, cached.getRevocationToken(cert, issuer).getStatus());
        assertEquals(2, requests.get());
    }

    @Test
    void settingTheClockBackDoesNotExtendACacheEntry() {
        var clock = new TestClock();
        var online = mock(OCSPSource.class);
        when(online.getRevocationToken(certificate, issuer)).thenReturn(ocsp(clock, -60, 3600));
        var cached = CachedRevocationSource.ocsp(online, clock);
        cached.getRevocationToken(certificate, issuer);
        clock.advance(Duration.ofSeconds(-1));
        cached.getRevocationToken(certificate, issuer);
        verify(online, times(2)).getRevocationToken(certificate, issuer);
    }

    @Test
    void leastRecentlyUsedEntriesAreEvictedAtTheEntryLimit() {
        @SuppressWarnings("unchecked")
        var online = (RevocationSourceAlternateUrlsSupport<OCSP>) mock(RevocationSourceAlternateUrlsSupport.class);
        when(online.getRevocationToken(eq(certificate), eq(issuer), anyList())).thenReturn(ocsp(new TestClock(), -60, 3600));
        var cached = CachedRevocationSource.ocsp(online, Clock.systemUTC());
        var urls = IntStream.range(0, 257).mapToObj(i -> List.of("https://alternative.test/" + i)).toList();
        for (var url : urls.subList(0, 256))
            cached.getRevocationToken(certificate, issuer, url);
        cached.getRevocationToken(certificate, issuer, urls.getFirst()); // keep the oldest accessed entry
        cached.getRevocationToken(certificate, issuer, urls.getLast());
        cached.getRevocationToken(certificate, issuer, urls.getFirst());
        verify(online, times(257)).getRevocationToken(eq(certificate), eq(issuer), anyList());
        cached.getRevocationToken(certificate, issuer, urls.get(1)); // this one was evicted
        verify(online, times(258)).getRevocationToken(eq(certificate), eq(issuer), anyList());
    }

    @Test
    void encodedEvidenceHasAnAggregateMemoryLimit() {
        var response = spy(ocsp(new TestClock(), -60, 3600));
        // Reconstructing these oversized test bytes is intentionally unnecessary: the first entry
        // must have been evicted before it is requested again.
        when(response.getEncoded()).thenReturn(new byte[9 * 1024 * 1024]);
        var online = mock(OCSPSource.class);
        when(online.getRevocationToken(any(), eq(issuer))).thenReturn(response);
        var cached = new CachedRevocationSource<OCSP>(online, Clock.systemUTC()) {
            @Override
            protected OCSPToken decode(Entry entry, CertificateToken cert, CertificateToken ca) {
                throw new AssertionError("Evicted evidence must not be decoded");
            }
        };
        cached.getRevocationToken(certificate, issuer);
        cached.getRevocationToken(new CertificateToken(pki.signer(TestPki.PETER).certificate()), issuer);
        cached.getRevocationToken(certificate, issuer);
        verify(online, times(3)).getRevocationToken(any(), eq(issuer));
    }

    @ParameterizedTest
    @CsvSource({"-60,0", "-60,-1", "60,3600"})
    void missingExpiredOrFutureValidityIsNotCached(int thisUpdate, int nextUpdate) {
        var clock = new TestClock();
        var requests = new AtomicInteger();
        OCSPSource online = (cert, ca) -> { requests.incrementAndGet(); return ocsp(clock, thisUpdate, nextUpdate); };
        var cached = CachedRevocationSource.ocsp(online, clock);
        cached.getRevocationToken(certificate, issuer);
        cached.getRevocationToken(certificate, issuer);
        assertEquals(2, requests.get());
    }

    @Test
    void invalidUnknownAndMissingEvidenceAreNotCached() {
        var clock = new TestClock();
        var invalid = spy(ocsp(clock, -60, 3600));
        when(invalid.isValid()).thenReturn(false);
        var unknown = spy(ocsp(clock, -60, 3600));
        when(unknown.getStatus()).thenReturn(CertificateStatus.UNKNOWN);
        var undated = spy(ocsp(clock, -60, 3600));
        when(undated.getThisUpdate()).thenReturn(null);
        for (var token : Arrays.asList(invalid, unknown, undated, null)) {
            var requests = new AtomicInteger();
            OCSPSource online = (cert, ca) -> { requests.incrementAndGet(); return token; };
            var cached = CachedRevocationSource.ocsp(online, clock);
            cached.getRevocationToken(certificate, issuer);
            cached.getRevocationToken(certificate, issuer);
            assertEquals(2, requests.get());
        }
    }

    @Test
    void revokedStatusIsPreservedWhenReconstructingEvidence() {
        var signer = pki.issue("Cache revoked signer", TestPki.Kind.QESIG);
        pki.revoke(signer);
        var revoked = new CertificateToken(signer.certificate());
        var clock = new TestClock();
        OCSPSource online = (cert, ca) -> pki.ocspToken(cert, Date.from(clock.instant().minusSeconds(60)),
                Date.from(clock.instant().plusSeconds(3600)));
        var cached = CachedRevocationSource.ocsp(online, clock);
        assertEquals(CertificateStatus.REVOKED, cached.getRevocationToken(revoked, issuer).getStatus());
        assertEquals(CertificateStatus.REVOKED, cached.getRevocationToken(revoked, issuer).getStatus());
    }

    @Test
    void differentCertificatesIssuersAndAlternativeUrlsDoNotShareEntries() {
        var requests = new AtomicInteger();
        var clock = new TestClock();
        var alternatives = new AtomicReference<List<String>>();
        RevocationSourceAlternateUrlsSupport<OCSP> online = new RevocationSourceAlternateUrlsSupport<>() {
            @Override public OCSPToken getRevocationToken(CertificateToken cert, CertificateToken ca) {
                throw new AssertionError("The alternative URLs must be forwarded");
            }
            @Override public OCSPToken getRevocationToken(CertificateToken cert, CertificateToken ca, List<String> urls) {
                alternatives.set(urls);
                requests.incrementAndGet();
                return pki.ocspToken(cert, Date.from(clock.instant().minusSeconds(60)), Date.from(clock.instant().plusSeconds(3600)));
            }
        };
        var cached = CachedRevocationSource.ocsp(online, clock);
        var urls = List.of("https://alternative.test/ocsp");
        cached.getRevocationToken(certificate, issuer, urls);
        assertEquals(urls, alternatives.get());
        cached.getRevocationToken(certificate, issuer, urls);
        assertEquals(1, requests.get());
        cached.getRevocationToken(new CertificateToken(pki.signer(TestPki.PETER).certificate()), issuer, urls);
        cached.getRevocationToken(certificate, new CertificateToken(pki.signer(TestPki.SEAL).certificate()), urls);
        cached.getRevocationToken(certificate, issuer, List.of());
        assertEquals(4, requests.get());
    }

    @Test
    void expiredCacheIsNotUsedWhenNetworkThrows() {
        var clock = new TestClock();
        var online = mock(OCSPSource.class);
        when(online.getRevocationToken(certificate, issuer)).thenReturn(ocsp(clock, -60, 10))
                .thenThrow(new DSSExternalResourceException("offline"));
        var cached = CachedRevocationSource.ocsp(online, clock);
        cached.getRevocationToken(certificate, issuer);
        clock.advance(Duration.ofSeconds(10));
        assertThrows(DSSExternalResourceException.class, () -> cached.getRevocationToken(certificate, issuer));
    }

    @Test
    void parallelRequestsShareOneFetch() throws Exception {
        var requests = new AtomicInteger();
        OCSPSource online = (cert, ca) -> {
            requests.incrementAndGet();
            return ocsp(new TestClock(), -60, 3600);
        };
        var cached = CachedRevocationSource.ocsp(online, Clock.systemUTC());
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, 10)
                    .mapToObj(i -> threads.submit(() -> cached.getRevocationToken(certificate, issuer))).toList();
            for (var future : futures)
                assertEquals(CertificateStatus.GOOD, future.get().getStatus());
        }
        assertEquals(1, requests.get());
    }

    @Test
    void pickerEvidenceIsReusedButEveryDocumentGetsItsOwnCheckedTimestamp() {
        var requests = new AtomicInteger();
        var timestamps = new AtomicInteger();
        CRLSource online = (cert, ca) -> { requests.incrementAndGet(); return pki.crlSource(null).getRevocationToken(cert, ca); };
        var cached = CachedRevocationSource.crl(online, Clock.systemUTC());
        var checker = new CertificateStatusChecker((cert, ca) -> null, cached, null, () -> null);
        var key = pki.signer(TestPki.JANA).signingKey();
        var verifier = pki.verifier();
        verifier.setCrlSource(cached);
        SignatureValidator.getInstance().useCustomVerifier(verifier);
        try {
            assertEquals(CertificateStatus.GOOD, checker.checkStatus(key.getPrivateKey()));
            var parameters = SigningParametersResolver.buildRequested(
                    SignatureProfile.BASELINE_T, SignatureForm.XAdES, DigestAlgorithm.SHA256, null, SignaturePackaging.ENVELOPING,
                    true, null, null, null, false, 640, true);
            var input = SigningInput.fromDocument(AutogramDocument.build(
                    new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null), parameters);
            for (int i = 0; i < 3; i++) {
                var job = SigningJob.fromLocalInput(input, null);
                job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, true));
                assertNotNull(job.signWithKey(key, (algorithm, digest) -> {
                    timestamps.incrementAndGet();
                    return pki.tspSource().getTimeStampResponse(algorithm, digest);
                }));
            }
            assertEquals(1, requests.get());
            assertEquals(3, timestamps.get());

            var job = SigningJob.fromLocalInput(input, null);
            job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, true));
            assertThrows(SignatureRequirementsNotMetException.class,
                    () -> job.signWithKey(key, pki.nonQualifiedTspSource()));
        } finally {
            key.close();
            SignatureValidator.getInstance().useCustomVerifier(null);
        }
    }

    private static OCSPToken ocsp(Clock clock, int thisUpdateSeconds, int nextUpdateSeconds) {
        return pki.ocspToken(certificate, Date.from(clock.instant().plusSeconds(thisUpdateSeconds)),
                nextUpdateSeconds == 0 ? null : Date.from(clock.instant().plusSeconds(nextUpdateSeconds)));
    }

    private static class TestClock extends Clock {
        private Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        void advance(Duration duration) { now = now.plus(duration); }
        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException(); }
    }
}
