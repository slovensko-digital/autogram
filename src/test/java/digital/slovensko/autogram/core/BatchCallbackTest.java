package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BatchCallbackTest {
    @Test
    void cancelBeforeSelectingKeyRespondsAndClosesThroughUiOnce() {
        var batchRef = new AtomicReference<Batch>();
        var closed = new AtomicInteger();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void startBatch(Batch batch, Autogram autogram) {
                batchRef.set(batch);
            }

            @Override
            public void closeBatch() {
                assertTrue(batchRef.get().isEnded());
                closed.incrementAndGet();
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var responder = mock(BatchResponder.class);
        autogram.startBatchSigning(2, responder);

        autogram.cancelBatch(batchRef.get());
        autogram.cancelBatch(batchRef.get());
        autogram.signBatchWithKey(batchRef.get(), mock(SigningKey.class));

        verify(responder).onBatchStartFailure(any(BatchCanceledException.class));
        verify(responder, never()).onBatchStartSuccess(any());
        assertEquals(1, closed.get());
    }

    @Test
    void cancelAfterStartingDoesNotReportStartFailure() {
        var batchRef = new AtomicReference<Batch>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void startBatch(Batch batch, Autogram autogram) {
                batchRef.set(batch);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var responder = mock(BatchResponder.class);
        autogram.startBatchSigning(2, responder);
        var batch = batchRef.get();

        autogram.signBatchWithKey(batch, mock(SigningKey.class));
        autogram.cancelBatch(batch);

        assertTrue(batch.isEnded());
        verify(responder).onBatchStartSuccess(batch);
        verify(responder, never()).onBatchStartFailure(any());
    }

    @Test
    void updateEndsCompletedBatchThroughApplication() {
        var batchRef = new AtomicReference<Batch>();
        var closed = new AtomicInteger();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void startBatch(Batch batch, Autogram autogram) {
                batchRef.set(batch);
            }

            @Override
            public void closeBatch() {
                closed.incrementAndGet();
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        autogram.startBatchSigning(1, mock(BatchResponder.class));
        var batch = batchRef.get();
        autogram.signBatchWithKey(batch, mock(SigningKey.class));

        autogram.updateBatch(batch);
        assertFalse(batch.isEnded());
        batch.onJobSuccess();
        autogram.updateBatch(batch);
        autogram.updateBatch(batch);

        assertTrue(batch.isEnded());
        assertEquals(1, closed.get());
    }
}
