package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.core.errors.PINLockedException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Ending a batch ends everything that belongs to it: open documents, waiting signatures and the batch window. */
class BatchEndTest {
    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-13T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static class RecordingUI extends TestAutogramFactory.FakeUI {
        final SigningMode mode;
        final AtomicReference<Batch> batch = new AtomicReference<>();
        final List<SigningJob> closedJobs = new ArrayList<>();
        final List<AutogramException> shownErrors = new ArrayList<>();
        final AtomicInteger closedBatches = new AtomicInteger();

        RecordingUI(SigningMode mode) {
            this.mode = mode;
        }

        @Override
        public void selectBatchMode(Batch batch, Consumer<SigningMode> onSelected, Runnable onCancel) {
            this.batch.set(batch);
            onSelected.accept(mode);
        }

        @Override
        public void closeSigningJob(SigningJob job) {
            closedJobs.add(job);
        }

        @Override
        public void closeBatch() {
            closedBatches.incrementAndGet();
        }

        @Override
        public void onSigningFailed(AutogramException e, SigningJob job) {
            shownErrors.add(e);
        }
    }

    private static Batch startBatch(Autogram autogram, RecordingUI ui, int documents) {
        autogram.startBatchSigning(documents, mock(BatchResponder.class));
        var batch = ui.batch.get();
        if (!batch.isInteractive())
            autogram.signBatchWithKey(batch, mock(SigningKey.class));
        return batch;
    }

    private static SigningJob signableJob(Batch batch, Responder responder) {
        var job = spy(TestSigningJobFactory.create(batch, responder));
        doReturn(mock(SignedDocument.class)).when(job).signWithKey(any(), any());
        return job;
    }

    @Test
    void bulkBatchExpiresOnlyAfterStartAndNeverWhileADocumentIsInProgress() {
        var clock = new MutableClock();
        var batch = new SigningBatch(3, clock);
        clock.advance(Duration.ofHours(1));
        assertFalse(batch.isExpired(), "the user may still be picking the key");

        batch.start(null);
        var batchId = batch.getBatchId();
        clock.advance(Duration.ofMinutes(4));
        assertFalse(batch.isExpired());

        var job = TestSigningJobFactory.create(batch, mock(Responder.class));
        batch.addJob(batchId, job);
        clock.advance(Duration.ofMinutes(30));
        assertFalse(batch.isExpired(), "the document may wait for a PIN");

        batch.success(job);
        clock.advance(Duration.ofSeconds(59));
        assertFalse(batch.isExpired());
        clock.advance(Duration.ofSeconds(2));
        assertTrue(batch.isExpired());
        assertThrows(BatchExpiredException.class, () -> batch.validate(batchId));
        assertDoesNotThrow(() -> batch.validateId(batchId));
    }

    @Test
    void bulkBatchWaitsFiveMinutesForTheFirstDocument() {
        var clock = new MutableClock();
        var batch = new SigningBatch(1, clock);
        batch.start(null);
        clock.advance(Duration.ofMinutes(5));
        assertFalse(batch.isExpired());
        clock.advance(Duration.ofSeconds(1));
        assertTrue(batch.isExpired());
    }

    @Test
    void interactiveBatchExpiresAfterTenIdleMinutes() {
        var clock = new MutableClock();
        var batch = new SigningBatch(2, clock);
        batch.setMode(SigningMode.INTERACTIVE);
        batch.start(null);
        var job = TestSigningJobFactory.create(batch, mock(Responder.class));
        batch.addJob(batch.getBatchId(), job);
        clock.advance(Duration.ofHours(2));
        assertFalse(batch.isExpired(), "the document window is open");

        batch.failure(job);
        clock.advance(Duration.ofMinutes(10));
        assertFalse(batch.isExpired());
        clock.advance(Duration.ofSeconds(1));
        assertTrue(batch.isExpired());
    }

    @Test
    void skippingRemainingDocumentsEndsOtherOpenDocumentsOfTheBatch() {
        var ui = new RecordingUI(SigningMode.INTERACTIVE);
        var autogram = TestAutogramFactory.create(ui);
        var batch = startBatch(autogram, ui, 3);
        var firstResponder = mock(Responder.class);
        var secondResponder = mock(Responder.class);
        var first = TestSigningJobFactory.create(batch, firstResponder);
        var second = signableJob(batch, secondResponder);
        autogram.batchSign(first, batch.getBatchId());
        autogram.batchSign(second, batch.getBatchId());

        autogram.skipRemainingDocuments(first);

        verify(firstResponder).onDocumentSignFailed(any(SigningCanceledByUserException.class));
        verify(secondResponder).onDocumentSignFailed(any(SigningCanceledByUserException.class));
        assertTrue(ui.closedJobs.containsAll(List.of(first, second)));

        autogram.sign(second, mock(SigningKey.class));
        verify(second, never()).signWithKey(any(), any());
        verify(secondResponder, never()).onDocumentSigned(any());
    }

    @Test
    void clientEndingTheBatchEndsItsOpenDocumentsAndCanAskAgain() {
        var ui = new RecordingUI(SigningMode.INTERACTIVE);
        var autogram = TestAutogramFactory.create(ui);
        var batch = startBatch(autogram, ui, 2);
        var batchId = batch.getBatchId();
        var responder = mock(Responder.class);
        var job = signableJob(batch, responder);
        autogram.batchSign(job, batchId);

        assertFalse(autogram.endBatchSigning(batchId));

        verify(responder).onDocumentSignFailed(any(BatchEndedException.class));
        assertTrue(ui.closedJobs.contains(job));
        assertEquals(1, ui.closedBatches.get());
        autogram.sign(job, mock(SigningKey.class));
        verify(job, never()).signWithKey(any(), any());

        assertFalse(autogram.endBatchSigning(batchId), "ending an ended batch tells how it went");
        assertThrows(BatchInvalidIdException.class, () -> autogram.endBatchSigning("another"));
        assertEquals(1, ui.closedBatches.get());
    }

    @Test
    void endingABatchAfterAllDocumentsReportsFinished() {
        var ui = new RecordingUI(SigningMode.BULK);
        var autogram = TestAutogramFactory.create(ui);
        var batch = startBatch(autogram, ui, 1);
        var batchId = batch.getBatchId();
        autogram.batchSign(signableJob(batch, mock(Responder.class)), batchId);
        assertTrue(batch.isEnded(), "ends on its own after the last document");

        assertTrue(autogram.endBatchSigning(batchId));
    }

    @Test
    void fatalErrorEndsTheBatchWithTheSameErrorForAllOpenDocuments() {
        var ui = new RecordingUI(SigningMode.INTERACTIVE);
        var autogram = TestAutogramFactory.create(ui);
        var batch = startBatch(autogram, ui, 3);
        var failingResponder = mock(Responder.class);
        var otherResponder = mock(Responder.class);
        var failing = spy(TestSigningJobFactory.create(batch, failingResponder));
        var locked = new PINLockedException();
        doThrow(locked).when(failing).signWithKey(any(), any());
        var other = TestSigningJobFactory.create(batch, otherResponder);
        autogram.batchSign(failing, batch.getBatchId());
        autogram.batchSign(other, batch.getBatchId());

        autogram.sign(failing, mock(SigningKey.class));

        assertTrue(batch.isEnded());
        verify(failingResponder).onDocumentSignFailed(locked);
        verify(otherResponder).onDocumentSignFailed(locked);
        assertEquals(List.of(locked), ui.shownErrors);
        assertTrue(ui.closedJobs.contains(other));
    }

    @Test
    void closingThePinDialogInBulkEndsTheBatch() {
        var ui = new RecordingUI(SigningMode.BULK);
        var autogram = TestAutogramFactory.create(ui);
        var batch = startBatch(autogram, ui, 3);
        var batchId = batch.getBatchId();
        var responder = mock(Responder.class);
        var job = spy(TestSigningJobFactory.create(batch, responder));
        doThrow(new PasswordNotProvidedException()).when(job).signWithKey(any(), any());
        doAnswer(call -> {
            assertTrue(batch.isEnded(), "the batch ends before the client learns about it");
            return null;
        }).when(responder).onDocumentSignFailed(any());

        autogram.batchSign(job, batchId);

        verify(responder).onDocumentSignFailed(any(PasswordNotProvidedException.class));
        assertEquals(1, ui.closedBatches.get());
        assertThrows(BatchEndedException.class,
                () -> autogram.batchSign(TestSigningJobFactory.create(batch, mock(Responder.class)), batchId));
    }

    @Test
    void expiredBatchEndsItsWindowAndUnblocksOtherSigning() {
        var clock = new MutableClock();
        var ui = new RecordingUI(SigningMode.BULK);
        var autogram = TestAutogramFactory.create(ui);
        autogram.setClock(clock);
        var batch = startBatch(autogram, ui, 2);
        var batchId = batch.getBatchId();
        autogram.batchSign(signableJob(batch, mock(Responder.class)), batchId);

        clock.advance(Duration.ofMinutes(2));
        autogram.expireBatchIfNeeded();

        assertTrue(batch.isEnded());
        assertEquals(1, ui.closedBatches.get());
        assertThrows(BatchExpiredException.class,
                () -> autogram.batchSign(TestSigningJobFactory.create(batch, mock(Responder.class)), batchId));
        assertFalse(autogram.endBatchSigning(batchId), "the client can still end an expired batch");

        var individual = signableJob(new NoBatch(), mock(Responder.class));
        autogram.sign(individual, mock(SigningKey.class));
        verify(individual).signWithKey(any(), any());
    }

    @Test
    void newBatchCanStartOnceTheCurrentOneExpired() {
        var clock = new MutableClock();
        var ui = new RecordingUI(SigningMode.INTERACTIVE);
        var autogram = TestAutogramFactory.create(ui);
        autogram.setClock(clock);
        var abandoned = startBatch(autogram, ui, 3);
        var abandonedId = abandoned.getBatchId();

        clock.advance(Duration.ofMinutes(11));
        autogram.startBatchSigning(1, mock(BatchResponder.class));

        assertTrue(abandoned.isEnded());
        assertNotSame(abandoned, ui.batch.get());
        assertThrows(BatchInvalidIdException.class, () -> autogram.endBatchSigning(abandonedId),
                "the ID of the old batch doesn't belong to the new one");
    }
}
