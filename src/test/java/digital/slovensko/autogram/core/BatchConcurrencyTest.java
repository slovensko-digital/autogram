package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Bulk documents arrive on parallel HTTP threads, see AutogramServer's executor. */
class BatchConcurrencyTest {
    private static final int THREADS = 8;

    private static class BulkUI extends TestAutogramFactory.FakeUI {
        @Override
        public void startBatch(Batch batch, Autogram autogram) {
            autogram.signBatchWithKey(batch, mock(SigningKey.class));
        }
    }

    @Test
    void parallelBulkRequestsAreSignedOneAtATimeAndAllCounted() throws Exception {
        var documents = 40;
        var autogram = TestAutogramFactory.create(new BulkUI());
        var batch = startedBulkBatch(autogram, documents);
        var active = new AtomicInteger();
        var maxActive = new AtomicInteger();

        runInParallel(documents, i -> {
            var job = spy(TestSigningJobFactory.create(batch, mock(Responder.class)));
            doAnswer(invocation -> {
                maxActive.accumulateAndGet(active.incrementAndGet(), Math::max);
                Thread.sleep(1);
                active.decrementAndGet();
                return mock(SignedDocument.class);
            }).when(job).signWithKey(any(), any());
            autogram.batchSign(job, batch.getBatchId());
        });

        assertEquals(1, maxActive.get());
        assertEquals(documents, batch.getProcessedDocumentsCount());
        assertTrue(batch.isEnded());
    }

    @Test
    void mistypedPinReachesTheCardOnlyOnceInParallelBulkRequests() throws Exception {
        var prompts = new AtomicInteger();
        var ui = new BulkUI() {
            @Override
            public char[] getContextSpecificPassword(boolean incorrectPIN, boolean canReturnToSigning) {
                return (prompts.incrementAndGet() == 1 ? "0000" : "1234").toCharArray();
            }
        };
        var autogram = TestAutogramFactory.create(ui);
        var manager = InteractiveBatchTest.passwordManager(autogram);
        var batch = startedBulkBatch(autogram, 2);
        var usedPins = Collections.synchronizedList(new ArrayList<String>());

        runInParallel(2, i -> {
            var job = spy(TestSigningJobFactory.create(batch, mock(Responder.class)));
            doAnswer(invocation -> {
                var pin = new String(manager.getContextSpecificPassword());
                usedPins.add(pin);
                if (pin.equals("0000")) {
                    Thread.sleep(50); // the card takes a while to reject the PIN
                    throw new PINIncorrectException();
                }
                return mock(SignedDocument.class);
            }).when(job).signWithKey(any(), any());
            autogram.batchSign(job, batch.getBatchId());
        });

        assertEquals(List.of("0000", "1234", "1234"), usedPins);
        assertEquals(2, prompts.get());
        assertEquals(2, batch.getProcessedDocumentsCount());
    }

    @Test
    void onlyOneOfParallelBatchStartsSucceeds() throws Exception {
        for (var attempt = 0; attempt < 20; attempt++) {
            var autogram = TestAutogramFactory.create();
            var conflicts = new AtomicInteger();
            runInParallel(THREADS, i -> {
                try {
                    autogram.startBatchSigning(1, mock(BatchResponder.class));
                } catch (BatchConflictException e) {
                    conflicts.incrementAndGet();
                }
            });
            assertEquals(THREADS - 1, conflicts.get());
        }
    }

    private static Batch startedBulkBatch(Autogram autogram, int totalNumberOfDocuments) {
        var responder = mock(BatchResponder.class);
        autogram.startBatchSigning(totalNumberOfDocuments, responder);
        var captor = org.mockito.ArgumentCaptor.forClass(Batch.class);
        verify(responder).onBatchStartSuccess(captor.capture());
        return captor.getValue();
    }

    private interface Task {
        void run(int index) throws Exception;
    }

    private static void runInParallel(int tasks, Task task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        var barrier = new CyclicBarrier(Math.min(tasks, THREADS));
        try {
            var futures = new ArrayList<Future<?>>();
            for (var i = 0; i < tasks; i++) {
                var index = i;
                futures.add(executor.submit(() -> {
                    if (index < THREADS) barrier.await();
                    task.run(index);
                    return null;
                }));
            }
            for (var future : futures)
                future.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
}
