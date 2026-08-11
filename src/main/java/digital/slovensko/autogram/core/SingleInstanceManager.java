package digital.slovensko.autogram.core;

import digital.slovensko.autogram.util.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Ensures that only one GUI instance of Autogram is running at a time, forwards
 * file paths opened from the file manager to that instance and hands them over
 * to the UI as a single batch.
 * <p>
 * The first instance to start acquires a lock file and becomes the "primary".
 * It listens on a Unix domain socket (supported on Windows 10+ and all
 * supported Linux distributions). Any later instance connects to the socket,
 * sends the file paths it was launched with and exits without starting the GUI.
 * <p>
 * Files that arrive around the same time (on Windows, opening several files
 * launches one process per file) are collected for a short debounce window and
 * delivered together, so the running instance opens them in a single batch.
 * <p>
 * The socket is only protected by the OS file permissions of the user's data
 * directory, so any process running as the same user could connect to it and
 * inject file paths. This is the standard trust model for a single-user desktop
 * app and is not treated as a security boundary.
 */
public class SingleInstanceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleInstanceManager.class);

    private static final long CONNECT_RETRY_DELAY_MS = 200;
    private static final int MAX_CONNECT_ATTEMPTS = 50;
    private static final long CONNECTION_READ_TIMEOUT_MS = 10_000;
    private static final long DEBOUNCE_DELAY_MS = 500;
    private static final String SOCKET_FILE_NAME = "autogram.sock";
    private static final String LOCK_FILE_NAME = "autogram.lock";

    private static SingleInstanceManager instance;

    private final Path socketPath;
    private final Path lockPath;

    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "autogram-single-instance");
        thread.setDaemon(true);
        return thread;
    });

    private final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        var thread = new Thread(runnable, "autogram-file-open-debounce");
        thread.setDaemon(true);
        return thread;
    });

    private FileChannel lockChannel;
    private FileLock lock;
    private ServerSocketChannel serverChannel;
    private volatile boolean running;

    private final Object filesLock = new Object();
    private final List<String> pendingFiles = new ArrayList<>();
    private Consumer<List<String>> filesHandler;
    private Runnable activateHandler;
    private ScheduledFuture<?> debounceTask;

    SingleInstanceManager(Path socketPath, Path lockPath) {
        this.socketPath = socketPath;
        this.lockPath = lockPath;
    }

    /**
     * Tries to become the primary instance, or forwards the given files to the
     * running primary instance.
     *
     * @return {@code true} if this process should continue and start the GUI
     * (it is the primary instance), {@code false} if the files were handed over
     * to an already running instance and this process should exit.
     */
    public static boolean start(List<String> files) {
        try {
            return start(resolveDataDir(), files);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize single-instance support, continuing without it", e);
            return true;
        }
    }

    static boolean start(Path dataDir, List<String> files) {
        var manager = new SingleInstanceManager(
                dataDir.resolve(SOCKET_FILE_NAME),
                dataDir.resolve(LOCK_FILE_NAME));
        instance = manager;
        return manager.tryBecomePrimaryOrForward(files == null ? List.of() : files);
    }

    public static SingleInstanceManager getInstance() {
        return instance;
    }

    /**
     * Registers a handler that is called (on the single-instance server thread)
     * for every batch of files opened in the file manager. Any files that were
     * received before the handler was registered are delivered as well.
     */
    public void onFilesReceived(Consumer<List<String>> handler) {
        synchronized (filesLock) {
            this.filesHandler = handler;
            if (!pendingFiles.isEmpty())
                scheduleDispatch();
        }
    }

    /**
     * Registers a handler that is called (on the single-instance server thread)
     * when another instance of Autogram was started without any files to open,
     * e.g. by clicking the application icon. The running instance should bring
     * its window to the foreground.
     */
    public void onActivated(Runnable handler) {
        synchronized (filesLock) {
            this.activateHandler = handler;
        }
    }

    void shutdown() {
        running = false;
        closeServer();
        releaseLock();
        serverExecutor.shutdownNow();
        debounceExecutor.shutdownNow();
    }

    private boolean tryBecomePrimaryOrForward(List<String> files) {
        for (var attempt = 0; ; attempt++) {
            if (tryConnectAndSend(files))
                return false;

            try {
                if (acquireLock()) {
                    if (!startServer()) {
                        releaseLock();
                        LOGGER.error("Failed to start single-instance server, continuing without it");
                    }
                    enqueue(files);
                    return true;
                }
            } catch (IOException e) {
                LOGGER.error("Could not create single-instance lock at " + lockPath + ", continuing without single-instance support", e);
                releaseLock();
                enqueue(files);
                return true;
            }

            if (attempt >= MAX_CONNECT_ATTEMPTS) {
                LOGGER.error("Could not hand off files to a running Autogram instance");
                return false;
            }

            sleep(CONNECT_RETRY_DELAY_MS);
        }
    }

    private boolean tryConnectAndSend(List<String> files) {
        try (var channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(UnixDomainSocketAddress.of(socketPath));
            channel.write(ByteBuffer.wrap(String.join("\n", files).getBytes(StandardCharsets.UTF_8)));
            channel.close();
            return true;
        } catch (IOException e) {
            LOGGER.debug("Could not connect to running Autogram instance", e);
            return false;
        }
    }

    private boolean acquireLock() throws IOException {
        Files.createDirectories(lockPath.getParent());
        lockChannel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            lock = lockChannel.tryLock();
        } catch (OverlappingFileLockException e) {
            lock = null;
        }
        return lock != null;
    }

    private boolean startServer() {
        try {
            Files.createDirectories(socketPath.getParent());
            Files.deleteIfExists(socketPath);
            serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            serverChannel.bind(UnixDomainSocketAddress.of(socketPath));
            running = true;
            serverExecutor.execute(this::acceptLoop);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to bind single-instance socket at " + socketPath, e);
            closeServer();
            return false;
        }
    }

    private void acceptLoop() {
        while (running) {
            try (var channel = serverChannel.accept()) {
                handleConnection(channel);
            } catch (ClosedChannelException e) {
                break;
            } catch (IOException e) {
                if (running)
                    LOGGER.warn("Error accepting single-instance connection", e);
            }
        }
    }

    private void handleConnection(SocketChannel channel) {
        try {
            var files = parseFiles(readUntilEof(channel, CONNECTION_READ_TIMEOUT_MS));
            if (files.isEmpty())
                notifyActivated();
            else
                enqueue(files);
        } catch (IOException e) {
            LOGGER.debug("Failed to read single-instance request", e);
        }
    }

    private void notifyActivated() {
        Runnable handler;
        synchronized (filesLock) {
            handler = activateHandler;
        }
        if (handler != null)
            handler.run();
    }

    private static List<String> parseFiles(String message) {
        return message.lines().filter(line -> !line.isBlank()).toList();
    }

    private void enqueue(List<String> files) {
        if (files.isEmpty())
            return;
        synchronized (filesLock) {
            pendingFiles.addAll(files);
            if (filesHandler != null)
                scheduleDispatch();
        }
    }

    private void scheduleDispatch() {
        if (debounceTask != null)
            debounceTask.cancel(false);
        debounceTask = debounceExecutor.schedule(this::dispatch, DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void dispatch() {
        List<String> batch;
        Consumer<List<String>> handler;
        synchronized (filesLock) {
            debounceTask = null;
            handler = filesHandler;
            batch = List.copyOf(pendingFiles);
            pendingFiles.clear();
        }
        if (handler != null && !batch.isEmpty())
            handler.accept(batch);
    }

    private static String readUntilEof(SocketChannel channel, long timeoutMs) throws IOException {
        channel.configureBlocking(false);
        var result = new StringBuilder();
        var buffer = ByteBuffer.allocate(8192);
        var deadline = System.currentTimeMillis() + timeoutMs;

        try (var selector = Selector.open()) {
            channel.register(selector, SelectionKey.OP_READ);

            while (true) {
                var read = channel.read(buffer);
                if (read < 0)
                    break;

                if (read > 0) {
                    buffer.flip();
                    result.append(StandardCharsets.UTF_8.decode(buffer));
                    buffer.clear();
                    continue;
                }

                var remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0)
                    throw new SocketTimeoutException("Timed out reading single-instance request");

                selector.select(remaining);
            }
        }
        return result.toString();
    }

    private void closeServer() {
        try {
            if (serverChannel != null)
                serverChannel.close();
        } catch (IOException ignored) {
        }
    }

    private void releaseLock() {
        try {
            if (lock != null) {
                lock.release();
                lock = null;
            }
        } catch (IOException ignored) {
        }

        try {
            if (lockChannel != null) {
                lockChannel.close();
                lockChannel = null;
            }
        } catch (IOException ignored) {
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Path resolveDataDir() {
        return switch (OperatingSystem.current()) {
            case WINDOWS -> {
                var localAppData = System.getenv("LOCALAPPDATA");
                if (localAppData != null && !localAppData.isBlank())
                    yield Path.of(localAppData, "Autogram");
                yield Path.of(System.getProperty("user.home"), ".autogram");
            }
            case MAC -> Path.of(System.getProperty("user.home"), "Library", "Application Support", "Autogram");
            case LINUX -> {
                var xdgDataHome = System.getenv("XDG_DATA_HOME");
                if (xdgDataHome != null && !xdgDataHome.isBlank())
                    yield Path.of(xdgDataHome, "autogram");
                yield Path.of(System.getProperty("user.home"), ".local", "share", "autogram");
            }
        };
    }
}
