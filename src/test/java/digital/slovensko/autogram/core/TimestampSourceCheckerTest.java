package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.errors.TsaServerMisconfiguredException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TimestampSourceCheckerTest {
    private static TestPki pki;
    private static TimestampSourceChecker checker;

    @BeforeAll
    static void createPki() {
        pki = TestPki.create();
        checker = new TimestampSourceChecker(() -> CompletableFuture.completedFuture(pki.verifier()));
    }

    @Test
    void qualifiedTimestampIsRecognizedUsingItsResponseAndTrustedLists() {
        var result = checker.check(pki.tspSource(), pki.verifier());

        assertEquals(TimestampQualification.QTSA, result.qualification());
        assertNotNull(result.source().getTimeStampResponse(DigestAlgorithm.SHA256, new byte[32]));
    }

    @Test
    void nonQualifiedTimestampIsNotAdvertisedAsQualified() {
        var source = pki.nonQualifiedTspSource();
        var result = checker.check(source, pki.verifier());

        assertEquals(TimestampQualification.TSA, result.qualification());
        assertSame(source, result.source());
    }

    @Test
    void probeDoesNotSendTheDocument() {
        var requests = new AtomicInteger();
        TSPSource source = (algorithm, digest) -> {
            assertEquals(DigestAlgorithm.SHA256, algorithm);
            assertEquals(32, digest.length);
            requests.incrementAndGet();
            return pki.tspSource().getTimeStampResponse(algorithm, digest);
        };

        assertEquals(TimestampQualification.QTSA, checker.check(source, pki.verifier()).qualification());
        assertEquals(1, requests.get());
    }

    @Test
    void aTimestampForADifferentDigestCannotQualifyTheSource() {
        TSPSource source = (algorithm, digest) -> pki.tspSource().getTimeStampResponse(algorithm, new byte[32]);

        assertNull(checker.check(source, pki.verifier()).qualification());
    }

    @Test
    void checkedSourceDoesNotSilentlyDowngradeToANonQualifiedFallback() {
        var requests = new AtomicInteger();
        TSPSource source = (algorithm, digest) -> (requests.getAndIncrement() == 0 ? pki.tspSource() : pki.nonQualifiedTspSource())
                .getTimeStampResponse(algorithm, digest);
        var result = checker.check(source, pki.verifier());
        assertEquals(TimestampQualification.QTSA, result.qualification());

        assertThrows(TsaServerMisconfiguredException.class,
                () -> result.source().getTimeStampResponse(DigestAlgorithm.SHA256, new byte[32]));
    }

    @Test
    void unavailableSourceDoesNotBecomeQualified() {
        TSPSource source = (algorithm, digest) -> { throw new IllegalStateException("offline"); };

        assertNull(checker.check(source, pki.verifier()).qualification());
        assertNull(checker.check(null, pki.verifier()).qualification());
    }

    @Test
    void missingTrustedListsDoNotTriggerAProbe() {
        TSPSource source = (algorithm, digest) -> { throw new AssertionError("must wait for the trusted lists"); };

        assertNull(checker.check(source, null).qualification());
        assertNull(checker.check(source, new CommonCertificateVerifier()).qualification());
    }

    @Test
    void sourceCheckWaitsForTrustedLists() throws Exception {
        var loaded = new CompletableFuture<CertificateVerifier>();
        var waitingChecker = new TimestampSourceChecker(() -> loaded);
        var result = waitingChecker.checkAsync(pki.tspSource(), Duration.ofSeconds(10), Duration.ofSeconds(10));

        assertFalse(result.isDone());
        loaded.complete(pki.verifier());
        assertEquals(TimestampQualification.QTSA, result.get(10, TimeUnit.SECONDS).qualification());
    }

    @Test
    void waitingForTrustedListsTimesOutWithoutCompletingTheSharedFuture() throws Exception {
        var loaded = new CompletableFuture<CertificateVerifier>();
        var waitingChecker = new TimestampSourceChecker(() -> loaded);
        var result = waitingChecker.checkAsync(pki.tspSource(), Duration.ofSeconds(10), Duration.ofMillis(100));

        assertNull(result.get(10, TimeUnit.SECONDS).qualification());
        assertFalse(loaded.isDone());
    }

    @Test
    void hangingSourceTimesOut() throws Exception {
        var release = new CountDownLatch(1);
        TSPSource source = (algorithm, digest) -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        };
        try {
            var result = checker.checkAsync(source, Duration.ofMillis(100), Duration.ofSeconds(10));
            assertNull(result.get(10, TimeUnit.SECONDS).qualification());
        } finally {
            release.countDown();
        }
    }
}
