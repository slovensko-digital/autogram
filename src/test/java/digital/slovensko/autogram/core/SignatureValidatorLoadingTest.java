package digital.slovensko.autogram.core;

import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

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
}
