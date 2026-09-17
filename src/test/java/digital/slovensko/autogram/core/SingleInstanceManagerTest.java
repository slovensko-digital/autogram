package digital.slovensko.autogram.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SingleInstanceManagerTest {
    private final List<SingleInstanceManager> created = new ArrayList<>();

    @TempDir
    Path tempDir;

    @AfterEach
    void cleanup() {
        for (var manager : created)
            manager.shutdown();
        created.clear();
    }

    @Test
    void testBecomesPrimaryAndDeliversItsOwnFiles() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of("/tmp/first.pdf", "/tmp/second.asice")));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        primary.onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/first.pdf", "/tmp/second.asice"), received.get());
    }

    @Test
    void testForwardsFilesToRunningPrimary() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        primary.onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/second.pdf", "/tmp/third.asice")));

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/second.pdf", "/tmp/third.asice"), received.get());
    }

    @Test
    void testBuffersFilesUntilHandlerIsRegistered() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/late.pdf")));

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        primary.onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/late.pdf"), received.get());
    }

    @Test
    void testBecomesPrimaryAfterPreviousPrimaryShutsDown() {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        primary.shutdown();

        assertTrue(SingleInstanceManager.start(tempDir, List.of("/tmp/after.pdf")));
        created.add(SingleInstanceManager.getInstance());
    }

    @Test
    void testCombinesFilesThatArriveInQuickSuccession() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        primary.onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/a.pdf")));
        Thread.sleep(50);
        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/b.pdf", "/tmp/c.asice")));

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/a.pdf", "/tmp/b.pdf", "/tmp/c.asice"), received.get());
    }

    @Test
    void testNotifiesPrimaryWhenStartedWithoutFiles() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var latch = new CountDownLatch(1);
        primary.onActivated(latch::countDown);

        assertFalse(SingleInstanceManager.start(tempDir, List.of()));

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testDispatchesBatchesThatArriveAfterPreviousOne() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var batches = new LinkedBlockingQueue<List<String>>();
        primary.onArgsReceived(batches::add);

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/a.pdf")));
        assertEquals(List.of("/tmp/a.pdf"), batches.poll(5, TimeUnit.SECONDS));

        Thread.sleep(600);
        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/b.pdf")));
        assertEquals(List.of("/tmp/b.pdf"), batches.poll(5, TimeUnit.SECONDS));
    }

    @Test
    void testFailsOpenWhenLockFileIsUncreatable() throws IOException, InterruptedException {
        var blocked = Files.createFile(tempDir.resolve("blocked"));
        assertTrue(SingleInstanceManager.start(blocked, List.of("/tmp/lockless.pdf")));
        created.add(SingleInstanceManager.getInstance());

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        SingleInstanceManager.getInstance().onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/lockless.pdf"), received.get());
    }

    @Test
    void testFailsOpenWhenSocketIsUnbindable() throws IOException, InterruptedException {
        Files.createDirectories(tempDir.resolve("autogram.sock").resolve("occupied"));
        assertTrue(SingleInstanceManager.start(tempDir, List.of("/tmp/socketless.pdf")));
        created.add(SingleInstanceManager.getInstance());

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        SingleInstanceManager.getInstance().onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/socketless.pdf"), received.get());
    }

    @Test
    void testKeepsPrimaryInstanceWhenForwarding() {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/forwarded.pdf")));
        assertSame(primary, SingleInstanceManager.getInstance());
    }

    @Test
    void testFailsOpenWhenUnixSocketsAreUnsupported() throws InterruptedException {
        var manager = new SingleInstanceManager(tempDir.resolve("autogram.sock"), tempDir.resolve("autogram.lock")) {
            @Override
            SocketChannel openClientChannel() {
                throw new UnsupportedOperationException("Unix domain sockets not supported");
            }

            @Override
            ServerSocketChannel openServerChannel() {
                throw new UnsupportedOperationException("Unix domain sockets not supported");
            }
        };
        created.add(manager);

        assertTrue(SingleInstanceManager.start(manager, List.of("/tmp/unsupported.pdf")));
        assertSame(manager, SingleInstanceManager.getInstance());

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        manager.onArgsReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/unsupported.pdf"), received.get());

        // Every launch is its own primary here, the lock must not be held.
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        created.add(SingleInstanceManager.getInstance());
    }

    @Test
    void testOnlyOneOfConcurrentStartsBecomesPrimary() throws Exception {
        var threads = 8;
        var barrier = new CyclicBarrier(threads);
        var primaries = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(threads);
        try {
            var futures = new ArrayList<Future<?>>();
            for (var i = 0; i < threads; i++) {
                var file = "/tmp/concurrent-" + i + ".pdf";
                futures.add(executor.submit(() -> {
                    barrier.await();
                    if (SingleInstanceManager.start(tempDir, List.of(file)))
                        primaries.incrementAndGet();
                    return null;
                }));
            }
            for (var future : futures)
                future.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertEquals(1, primaries.get());
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var received = new LinkedBlockingQueue<String>();
        primary.onArgsReceived(received::addAll);
        var all = new HashSet<String>();
        while (all.size() < threads) {
            var next = received.poll(5, TimeUnit.SECONDS);
            assertNotNull(next, "timed out with " + all);
            all.add(next);
        }
        for (var i = 0; i < threads; i++)
            assertTrue(all.contains("/tmp/concurrent-" + i + ".pdf"));
    }

    @Test
    void testStalledClientDoesNotBlockOtherLaunches() throws Exception {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var batches = new LinkedBlockingQueue<List<String>>();
        primary.onArgsReceived(batches::add);

        try (var stalled = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            stalled.connect(UnixDomainSocketAddress.of(tempDir.resolve("autogram.sock")));

            assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/after-stall.pdf")));
            assertEquals(List.of("/tmp/after-stall.pdf"), batches.poll(1500, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    void testRejectsOversizedMessage() throws Exception {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var batches = new LinkedBlockingQueue<List<String>>();
        primary.onArgsReceived(batches::add);

        try (var client = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            client.connect(UnixDomainSocketAddress.of(tempDir.resolve("autogram.sock")));
            var huge = new byte[SingleInstanceManager.MAX_MESSAGE_BYTES + 10];
            Arrays.fill(huge, (byte) 'a');
            Channels.newOutputStream(client).write(huge);
        }
        assertNull(batches.poll(1, TimeUnit.SECONDS));

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/after-huge.pdf")));
        assertEquals(List.of("/tmp/after-huge.pdf"), batches.poll(5, TimeUnit.SECONDS));
    }

    @Test
    void testSurvivesThrowingHandlers() throws Exception {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var batches = new LinkedBlockingQueue<List<String>>();
        var firstBatch = new AtomicBoolean(true);
        primary.onActivated(() -> {
            throw new IllegalStateException("activate failed");
        });
        primary.onArgsReceived(files -> {
            if (firstBatch.getAndSet(false))
                throw new IllegalStateException("first batch failed");
            batches.add(files);
        });

        assertFalse(SingleInstanceManager.start(tempDir, List.of()));
        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/lost.pdf")));
        Thread.sleep(700);
        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/survived.pdf")));
        assertEquals(List.of("/tmp/survived.pdf"), batches.poll(5, TimeUnit.SECONDS));
    }

    @Test
    void testForwardsPathContainingNewline() throws Exception {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var batches = new LinkedBlockingQueue<List<String>>();
        primary.onArgsReceived(batches::add);

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/multi\nline.pdf", "/tmp/plain.pdf")));
        assertEquals(List.of("/tmp/multi\nline.pdf", "/tmp/plain.pdf"), batches.poll(5, TimeUnit.SECONDS));
    }

    @Test
    void testDeliversActivationReceivedBeforeHandlerRegistered() throws Exception {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        assertFalse(SingleInstanceManager.start(tempDir, List.of()));
        Thread.sleep(200);

        var latch = new CountDownLatch(1);
        primary.onActivated(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testShutdownRemovesSocketFile() {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        assertTrue(Files.exists(tempDir.resolve("autogram.sock")));

        primary.shutdown();
        assertFalse(Files.exists(tempDir.resolve("autogram.sock")));
    }
}
