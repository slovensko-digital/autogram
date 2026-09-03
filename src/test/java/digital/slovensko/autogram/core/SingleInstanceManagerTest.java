package digital.slovensko.autogram.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        primary.onFilesReceived(files -> {
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
        primary.onFilesReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/second.pdf", "/tmp/third.asice")));
        created.add(SingleInstanceManager.getInstance());

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/second.pdf", "/tmp/third.asice"), received.get());
    }

    @Test
    void testBuffersFilesUntilHandlerIsRegistered() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        assertFalse(SingleInstanceManager.start(tempDir, List.of("/tmp/late.pdf")));
        created.add(SingleInstanceManager.getInstance());

        var received = new AtomicReference<List<String>>();
        var latch = new CountDownLatch(1);
        primary.onFilesReceived(files -> {
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
        primary.onFilesReceived(files -> {
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
        created.add(SingleInstanceManager.getInstance());

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testDispatchesBatchesThatArriveAfterPreviousOne() throws InterruptedException {
        assertTrue(SingleInstanceManager.start(tempDir, List.of()));
        var primary = SingleInstanceManager.getInstance();
        created.add(primary);

        var batches = new LinkedBlockingQueue<List<String>>();
        primary.onFilesReceived(batches::add);

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
        SingleInstanceManager.getInstance().onFilesReceived(files -> {
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
        SingleInstanceManager.getInstance().onFilesReceived(files -> {
            received.set(files);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("/tmp/socketless.pdf"), received.get());
    }
}
