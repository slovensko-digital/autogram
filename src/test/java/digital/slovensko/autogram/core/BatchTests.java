package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;

class BatchTests {

    @Test
    void interactiveBatchNeverExpires() throws Exception {
        var batch = new Batch(10);
        batch.setMode(SigningMode.INTERACTIVE);
        batch.start(null);
        expire(batch);

        Assertions.assertTrue(batch.isInteractive());
        Assertions.assertDoesNotThrow(() -> batch.addJob(batch.getId()));
    }

    @Test
    void automatedBatchExpires() throws Exception {
        var batch = new Batch(10);
        batch.start(null);
        expire(batch);

        Assertions.assertEquals(SigningMode.BULK, batch.getMode());
        Assertions.assertFalse(batch.isInteractive());
        Assertions.assertThrows(BatchExpiredException.class, () -> batch.addJob(batch.getId()));
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

    private static void expire(Batch batch) throws Exception {
        Field field = Batch.class.getDeclaredField("expirationDate");
        field.setAccessible(true);
        field.set(batch, new Date(System.currentTimeMillis() - 1000));
    }
}
