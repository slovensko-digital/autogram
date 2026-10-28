package digital.slovensko.autogram.core;

import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The trusted lists the certificate selection waits for, see {@link SignatureValidator#whenLoaded}. */
class SignatureValidatorLoadingTest {
    @AfterEach
    void useTrustedLists() {
        SignatureValidator.getInstance().useCustomVerifier(null);
    }

    @Test
    void testChecksStartedAfterReloadIsAnnouncedWaitForIt() {
        var validator = SignatureValidator.getInstance();
        var verifier = new CommonCertificateVerifier();
        validator.useCustomVerifier(verifier);
        assertSame(verifier, validator.whenLoaded().getNow(null));

        validator.reloadStarting();
        var reload = validator.whenLoaded();
        assertFalse(reload.isDone());

        validator.updateLotl(List.of("SK"));
        assertSame(verifier, reload.getNow(null));
    }

    @Test
    void testReloadsAnnouncedBeforeTheyStartShareOneLoad() {
        var validator = SignatureValidator.getInstance();
        validator.useCustomVerifier(new CommonCertificateVerifier());

        validator.reloadStarting();
        var first = validator.whenLoaded();
        validator.reloadStarting();

        assertSame(first, validator.whenLoaded());
    }

    @Test
    void testTrustedListsStateDoesNotWaitForRunningValidation() throws Exception {
        var validator = SignatureValidator.getInstance();
        validator.useCustomVerifier(new CommonCertificateVerifier());
        var validationRunning = new CountDownLatch(1);
        var validationDone = new CountDownLatch(1);
        var validation = new Thread(() -> {
            synchronized (validator) {
                validationRunning.countDown();
                try {
                    validationDone.await();
                } catch (InterruptedException ignored) {
                }
            }
        });
        validation.start();
        validationRunning.await();

        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertTrue(validator.areTLsLoaded()));
        } finally {
            validationDone.countDown();
            validation.join();
        }
    }
}
