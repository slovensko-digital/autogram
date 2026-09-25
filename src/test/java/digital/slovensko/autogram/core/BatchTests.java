package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BatchTests {

    @Test
    void interactiveBatchHasFiveMinuteDocumentTimeout() {
        var batch = new Batch(10);
        batch.setMode(SigningMode.INTERACTIVE);

        Assertions.assertTrue(batch.isInteractive());
        Assertions.assertEquals(5 * 60_000L, batch.documentTimeoutMillis());
    }

    @Test
    void automatedBatchKeepsOneMinuteDocumentTimeout() {
        var batch = new Batch(10);

        Assertions.assertEquals(SigningMode.BULK, batch.getMode());
        Assertions.assertFalse(batch.isInteractive());
        Assertions.assertEquals(60_000L, batch.documentTimeoutMillis());
    }

    @Test
    void modeCannotBeChangedAfterStart() {
        var batch = new Batch(1);
        batch.start(null);

        Assertions.assertThrows(IllegalStateException.class, () -> batch.setMode(SigningMode.INTERACTIVE));
    }

    @Test
    void nullBatchIsInactiveAndRejectsBatchIds() {
        var batch = new NoBatch();

        Assertions.assertFalse(batch.isActive());
        Assertions.assertFalse(batch.isInteractive());
        Assertions.assertThrows(BatchNotStartedException.class, () -> batch.addJob("anything"));
    }
}
