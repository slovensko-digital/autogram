package digital.slovensko.autogram.core;

import eu.europa.esig.dss.enumerations.CertificateQualification;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

class LocalBatchSignatureChoiceTest {
    private static final SignatureChoice WITHOUT_TIMESTAMP = new SignatureChoice(
            SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, false), null);

    @Test
    void onlyALocalBulkOperationCanSelectTheGoal() {
        var api = new SigningBatch(2);
        assertFalse(api.canSelectSignatureGoal());
        assertThrows(IllegalStateException.class, () -> api.setSignatureChoice(WITHOUT_TIMESTAMP));

        var interactive = new SigningBatch(2, Clock.systemUTC(), true);
        interactive.setMode(SigningMode.INTERACTIVE);
        assertFalse(interactive.canSelectSignatureGoal());
        assertThrows(IllegalStateException.class, () -> interactive.setSignatureChoice(WITHOUT_TIMESTAMP));
    }

    @Test
    void aBulkGoalCannotChangeAfterSigningStarts() {
        var batch = new SigningBatch(2, Clock.systemUTC(), true);
        assertTrue(batch.canSelectSignatureGoal());
        assertThrows(IllegalStateException.class, () -> batch.start(null));
        batch.setSignatureChoice(WITHOUT_TIMESTAMP);
        batch.start(null);

        assertSame(WITHOUT_TIMESTAMP, batch.getSignatureChoice());
        assertThrows(IllegalStateException.class, () -> batch.setSignatureChoice(WITHOUT_TIMESTAMP));
    }
}
