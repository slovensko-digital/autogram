package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import digital.slovensko.autogram.core.dto.SignedDocument;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BatchCallbackTest {
    @Test
    void noBatchAllowsFirstStartButRejectsBatchRequests() {
        var autogram = TestAutogramFactory.create(new TestAutogramFactory.FakeUI());
        assertThrows(BatchNotStartedException.class, () -> autogram.getBatch("unknown"));
        assertThrows(BatchNotStartedException.class, () -> autogram.endBatchSigning("unknown"));
        assertThrows(BatchNotStartedException.class,
                () -> autogram.batchSign(mock(SigningJob.class), "unknown"));

        autogram.startBatchSigning(1, mock(BatchResponder.class));
        assertThrows(BatchConflictException.class,
                () -> autogram.startBatchSigning(1, mock(BatchResponder.class)));
    }

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
    void failedStartResponseIsNotReportedAsFailedBatchStart() {
        var batchRef = new AtomicReference<Batch>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void startBatch(Batch batch, Autogram autogram) {
                batchRef.set(batch);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var responder = mock(BatchResponder.class);
        autogram.startBatchSigning(1, responder);
        var batch = batchRef.get();
        doThrow(new IllegalStateException("response failed")).when(responder).onBatchStartSuccess(batch);

        assertThrows(IllegalStateException.class,
                () -> autogram.signBatchWithKey(batch, mock(SigningKey.class)));

        assertFalse(batch.isEnded());
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
        batch.success();
        autogram.updateBatch(batch);
        autogram.updateBatch(batch);

        assertTrue(batch.isEnded());
        assertEquals(1, closed.get());
    }

    @Test
    void resultCallbacksCountBeforeNotifyingAdapterAndEndOnFatalFailure() {
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
        autogram.startBatchSigning(2, mock(BatchResponder.class));
        var batch = batchRef.get();
        autogram.signBatchWithKey(batch, mock(SigningKey.class));

        var successResponder = mock(Responder.class);
        var first = spy(TestSigningJobFactory.create(batch, successResponder));
        doAnswer(call -> {
            assertEquals(1, batch.getProcessedDocumentsCount());
            return null;
        }).when(successResponder).onDocumentSigned(any());
        doReturn(mock(SignedDocument.class)).when(first).signWithKey(any(), any());
        autogram.batchSign(first, batch.getBatchId());

        var fatal = mock(AutogramException.class);
        when(fatal.batchCanContinue()).thenReturn(false);
        var failureResponder = mock(Responder.class);
        var second = spy(TestSigningJobFactory.create(batch, failureResponder));
        doAnswer(call -> {
            assertEquals(2, batch.getProcessedDocumentsCount());
            assertTrue(batch.isEnded());
            return null;
        }).when(failureResponder).onDocumentSignFailed(fatal);
        doThrow(fatal).when(second).signWithKey(any(), any());
        autogram.batchSign(second, batch.getBatchId());

        verify(failureResponder).onDocumentSignFailed(fatal);
        assertEquals(1, closed.get());
        assertEquals(2, batch.getProcessedDocumentsCount());
    }

    @Test
    void responderFailureIsNotCountedAsSigningFailure() {
        var batchRef = new AtomicReference<Batch>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override
            public void startBatch(Batch batch, Autogram autogram) {
                batchRef.set(batch);
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        autogram.startBatchSigning(1, mock(BatchResponder.class));
        var batch = batchRef.get();
        autogram.signBatchWithKey(batch, mock(SigningKey.class));

        var responder = mock(Responder.class);
        doThrow(new IllegalStateException("response failed")).when(responder).onDocumentSigned(any());
        var job = spy(TestSigningJobFactory.create(batch, responder));
        doReturn(mock(SignedDocument.class)).when(job).signWithKey(any(), any());

        assertThrows(IllegalStateException.class, () -> autogram.batchSign(job, batch.getBatchId()));
        assertEquals(1, batch.getProcessedDocumentsCount());
        verify(responder, never()).onDocumentSignFailed(any());
    }
}
