package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestPki;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CertificateStatusCheckerTest {
    private static final OCSPSource NO_OCSP = (certificate, issuer) -> null;
    private static final CRLSource NO_CRL = (certificate, issuer) -> null;

    private static TestPki pki;

    @BeforeAll
    static void createPki() {
        pki = TestPki.create();
    }

    @Test
    void testValidCertificateIsGood() {
        var checker = new CertificateStatusChecker(NO_OCSP, pki.crlSource(null), null, () -> null);

        assertEquals(CertificateStatus.GOOD, checker.checkStatus(keyOf(pki.signer(TestPki.JANA))));
    }

    @Test
    void testRevokedCertificateIsRevoked() {
        var signer = pki.issue("Zrušený Podpisovateľ", TestPki.Kind.QESIG);
        pki.revoke(signer);
        var checker = new CertificateStatusChecker(NO_OCSP, pki.crlSource(null), null, () -> null);

        assertEquals(CertificateStatus.REVOKED, checker.checkStatus(keyOf(signer)));
    }

    @Test
    void testFailingOcspFallsBackToCrl() {
        var signer = pki.issue("Iný Zrušený", TestPki.Kind.QESIG);
        pki.revoke(signer);
        OCSPSource failingOcsp = (certificate, issuer) -> {
            throw new DSSExternalResourceException("OCSP responder is down");
        };
        var checker = new CertificateStatusChecker(failingOcsp, pki.crlSource(null), null, () -> null);

        assertEquals(CertificateStatus.REVOKED, checker.checkStatus(keyOf(signer)));
    }

    @Test
    void testStatusIsUnknownWithoutRevocationData() {
        var checker = new CertificateStatusChecker(NO_OCSP, NO_CRL, null, () -> null);

        assertEquals(CertificateStatus.UNKNOWN, checker.checkStatus(keyOf(pki.signer(TestPki.PETER))));
    }

    @Test
    void testStatusIsUnknownWhenRevocationSourcesFail() {
        CRLSource failingCrl = (certificate, issuer) -> {
            throw new DSSExternalResourceException("offline");
        };
        var checker = new CertificateStatusChecker(NO_OCSP, failingCrl, null, () -> null);

        assertEquals(CertificateStatus.UNKNOWN, checker.checkStatus(keyOf(pki.signer(TestPki.PETER))));
    }

    @Test
    void testIssuerMissingInChainIsDownloadedFromAia() {
        var key = keyWithoutIssuer(pki.signer(TestPki.SEAL));
        var checker = new CertificateStatusChecker(NO_OCSP, pki.crlSource(null),
                certificate -> Set.of(new CertificateToken(pki.caCertificate())), () -> null);

        assertEquals(CertificateStatus.GOOD, checker.checkStatus(key));
    }

    @Test
    void testStatusIsUnknownWhenIssuerIsNotFound() {
        var key = keyWithoutIssuer(pki.signer(TestPki.SEAL));
        var checker = new CertificateStatusChecker(NO_OCSP, pki.crlSource(null), certificate -> Set.of(), () -> null);

        assertEquals(CertificateStatus.UNKNOWN, checker.checkStatus(key));
    }

    @Test
    void testCertificateFromQualifiedServiceIsQualified() {
        var checker = new CertificateStatusChecker(NO_OCSP, NO_CRL, null, () -> null);

        assertEquals(CertificateQualification.QCERT_FOR_ESIG_QSCD, checker.checkQualification(keyOf(pki.signer(TestPki.JANA)), pki.verifier()));
        assertEquals(CertificateQualification.QCERT_FOR_ESEAL_QSCD, checker.checkQualification(keyOf(pki.signer(TestPki.SEAL)), pki.verifier()));
    }

    @Test
    void testQualifiedCertificateWithoutQscdKeepsItsSignatureLevel() {
        var signer = pki.issue("Uznaný Podpisovateľ", TestPki.Kind.ADESIG_QC);
        var checker = new CertificateStatusChecker(NO_OCSP, NO_CRL, null, () -> null);

        assertEquals(CertificateQualification.QCERT_FOR_ESIG, checker.checkQualification(keyOf(signer), pki.verifier()));
    }

    @Test
    void testCertificateWithoutQcStatementsIsNotQualified() {
        var signer = pki.issue("Bežný Používateľ", TestPki.Kind.NOT_QUALIFIED);
        var checker = new CertificateStatusChecker(NO_OCSP, NO_CRL, null, () -> null);

        assertEquals(CertificateQualification.CERT_FOR_UNKNOWN, checker.checkQualification(keyOf(signer), pki.verifier()));
    }

    @Test
    void testCertificateFromCaOutsideTrustedListsIsNotQualified() {
        var otherPki = TestPki.create();
        var checker = new CertificateStatusChecker(NO_OCSP, NO_CRL, null, () -> null);

        var qualification = checker.checkQualification(keyOf(otherPki.signer(TestPki.JANA)), pki.verifier());
        assertEquals(CertificateQualification.NA, qualification);
        assertTrue(new CertificateStatusChecker.Result(CertificateStatus.GOOD, qualification).isNotQualified());
    }

    @Test
    void testQualificationIsUnknownWithoutTrustedLists() {
        var key = keyOf(pki.signer(TestPki.JANA));
        var checker = new CertificateStatusChecker(NO_OCSP, NO_CRL, null, () -> null);

        assertNull(checker.checkQualification(key, null));
        assertNull(checker.checkQualification(key, new CommonCertificateVerifier()));
        assertFalse(new CertificateStatusChecker.Result(CertificateStatus.GOOD, null).isNotQualified());
    }

    @ParameterizedTest
    @EnumSource(value = CertificateQualification.class, names = {
            "QCERT_FOR_ESIG_QSCD", "QCERT_FOR_ESIG", "QCERT_FOR_ESEAL_QSCD", "QCERT_FOR_ESEAL",
            "QCERT_FOR_UNKNOWN_QSCD", "QCERT_FOR_UNKNOWN"
    })
    void testQualifiedAndUnknownCertificatesDoNotRequireTheNonQualifiedSetting(CertificateQualification qualification) {
        assertFalse(new CertificateStatusChecker.Result(CertificateStatus.GOOD, qualification).isNotQualified());
    }

    @ParameterizedTest
    @EnumSource(value = CertificateQualification.class, names = {
            "CERT_FOR_ESIG", "CERT_FOR_ESEAL", "CERT_FOR_UNKNOWN", "CERT_FOR_WSA", "QCERT_FOR_WSA", "NA"
    })
    void testUnqualifiedAndWebCertificatesRequireTheNonQualifiedSetting(CertificateQualification qualification) {
        assertTrue(new CertificateStatusChecker.Result(CertificateStatus.GOOD, qualification).isNotQualified());
    }

    @Test
    void testQualificationWaitsForTrustedListsLongerThanStatus() throws Exception {
        var trustedLists = new CompletableFuture<CertificateVerifier>();
        var checker = new CertificateStatusChecker(NO_OCSP, pki.crlSource(null), null, () -> trustedLists);

        var result = checker.checkAsync(keyOf(pki.signer(TestPki.JANA)), Duration.ofMillis(100), Duration.ofSeconds(10));
        CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() -> trustedLists.complete(pki.verifier()));

        assertEquals(new CertificateStatusChecker.Result(CertificateStatus.GOOD, CertificateQualification.QCERT_FOR_ESIG_QSCD), result.get(10, TimeUnit.SECONDS));
    }

    @Test
    void testQualificationIsUnknownWhenTrustedListsDoNotLoadInTime() throws Exception {
        var trustedLists = new CompletableFuture<CertificateVerifier>();
        var checker = new CertificateStatusChecker(NO_OCSP, pki.crlSource(null), null, () -> trustedLists);

        var result = checker.checkAsync(keyOf(pki.signer(TestPki.JANA)), Duration.ofSeconds(10), Duration.ofMillis(200));

        assertEquals(new CertificateStatusChecker.Result(CertificateStatus.GOOD, null), result.get(10, TimeUnit.SECONDS));
        assertFalse(trustedLists.isDone());
    }

    @Test
    void testIssuerCountryIsReadFromIssuerName() {
        var certificate = keyOf(pki.signer(TestPki.JANA)).getCertificate();

        assertEquals(Optional.of("SK"), CertificateStatusChecker.issuerCountry(certificate));
    }

    @Test
    void testSlowCheckIsUnknownAfterTimeoutWhileOtherCheckFinishes() throws Exception {
        var release = new CountDownLatch(1);
        OCSPSource hangingOcsp = (certificate, issuer) -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        };
        var checker = new CertificateStatusChecker(hangingOcsp, pki.crlSource(null), null,
                () -> CompletableFuture.completedFuture(pki.verifier()));

        try {
            var result = checker.checkAsync(keyOf(pki.signer(TestPki.JANA)), Duration.ofMillis(500), Duration.ofSeconds(10))
                    .get(10, TimeUnit.SECONDS);

            assertEquals(new CertificateStatusChecker.Result(CertificateStatus.UNKNOWN, CertificateQualification.QCERT_FOR_ESIG_QSCD), result);
        } finally {
            release.countDown();
        }
    }

    private static DSSPrivateKeyEntry keyOf(TestPki.Signer signer) {
        return signer.token().getKeys().get(0);
    }

    private static DSSPrivateKeyEntry keyWithoutIssuer(TestPki.Signer signer) {
        var certificate = new CertificateToken(signer.certificate());
        var key = mock(DSSPrivateKeyEntry.class);
        when(key.getCertificate()).thenReturn(certificate);
        when(key.getCertificateChain()).thenReturn(new CertificateToken[]{certificate});
        return key;
    }
}
