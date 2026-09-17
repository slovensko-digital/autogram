package digital.slovensko.autogram.core;

import digital.slovensko.autogram.util.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Ensures that only one GUI instance of Autogram is running at a time, forwards
 * the unnamed launch arguments (file paths opened from the file manager and/or
 * an autogram:// URL) to that instance and hands them over to the UI as a
 * single batch. An empty argument list means the user simply started the app
 * again and the running instance should activate its window.
 * <p>
 * The first instance to start acquires a lock file and becomes the "primary".
 * It listens on a Unix domain socket (supported on Windows 10 1803+ and all
 * supported Linux distributions). Any later instance connects to the socket,
 * sends the arguments it was launched with (NUL-separated, UTF-8) and exits
 * without starting the GUI. Where Unix domain sockets are not available, every
 * launch becomes its own primary instance and just opens the files it was
 * started with.
 * <p>
 * Arguments that arrive around the same time (on Windows, opening several files
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
    private static final long CONNECTION_READ_TIMEOUT_MS = 2_000;
    private static final long DEBOUNCE_DELAY_MS = 500;
    static final int MAX_MESSAGE_BYTES = 64 * 1024;
    private static final String ARG_SEPARATOR = "\0";
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
    private ServerSocketChannel serverChannel;
    private volatile boolean running;

    private final Object argsLock = new Object();
    private final List<String> pendingArgs = new ArrayList<>();
    private boolean activationPending;
    private Consumer<List<String>> argsHandler;
    private Runnable activateHandler;
    private ScheduledFuture<?> debounceTask;

    SingleInstanceManager(Path socketPath, Path lockPath) {
        this.socketPath = socketPath;
        this.lockPath = lockPath;
    }

    /**
     * Tries to become the primary instance, or forwards the given launch
     * arguments to the running primary instance.
     *
     * @return {@code true} if this process should continue and start the GUI
     * (it is the primary instance), {@code false} if the arguments were handed
     * over to an already running instance and this process should exit.
     */
    public static boolean start(List<String> args) {
        try {
            var dataDir = switch (OperatingSystem.current()) {
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

            return start(dataDir, args);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize single-instance support, continuing without it", e);
            return true;
        }
    }

    static boolean start(Path dataDir, List<String> args) {
        return start(new SingleInstanceManager(
                dataDir.resolve(SOCKET_FILE_NAME),
                dataDir.resolve(LOCK_FILE_NAME)), args);
    }

    static boolean start(SingleInstanceManager manager, List<String> args) {
        if (!manager.tryBecomePrimaryOrForward(args == null ? List.of() : args))
            return false;

        instance = manager;
        return true;
    }

    public static SingleInstanceManager getInstance() {
        return instance;
    }

    /**
     * Registers a handler that is called (on a background thread) for every
     * batch of launch arguments received from another instance. Any arguments
     * that were received before the handler was registered are delivered as
     * well.
     */
    public void onArgsReceived(Consumer<List<String>> handler) {
        synchronized (argsLock) {
            this.argsHandler = handler;
            if (!pendingArgs.isEmpty())
                scheduleDispatch();
        }
    }

    /**
     * Registers a handler that is called (on a background thread) when another
     * instance of Autogram was started without any arguments, e.g. by clicking
     * the application icon. The running instance should bring its window to
     * the foreground. An activation received before the handler was registered
     * is delivered as well.
     */
    public void onActivated(Runnable handler) {
        boolean deliverNow;
        synchronized (argsLock) {
            this.activateHandler = handler;
            deliverNow = activationPending;
            activationPending = false;
        }
        if (deliverNow)
            runHandler(handler);
    }

    /**
     * Stops listening, removes the socket file and releases the lock, so that
     * the next launch becomes the primary instance without any cleanup.
     */
    public void shutdown() {
        running = false;
        closeServer();
        releaseLock();
        serverExecutor.shutdownNow();
        debounceExecutor.shutdownNow();
    }

    private boolean tryBecomePrimaryOrForward(List<String> args) {
        try {
            return becomePrimaryOrForward(args);
        } catch (UnsupportedOperationException e) {
            // Thrown by SocketChannel.open(UNIX) on systems without Unix domain
            // sockets (Windows before 10 1803).
            LOGGER.warn("Unix domain sockets are not supported on this system, continuing without single-instance support");
            closeServer();
            releaseLock();
            enqueue(args);
            return true;
        }
    }

    private boolean becomePrimaryOrForward(List<String> args) {
        for (var attempt = 0; ; attempt++) {
            if (tryConnectAndSend(args))
                return false;

            try {
                if (acquireLock()) {
                    if (!startServer()) {
                        releaseLock();
                        LOGGER.error("Failed to start single-instance server, continuing without it");
                    }
                    enqueue(args);
                    return true;
                }
            } catch (IOException e) {
                LOGGER.error("Could not create single-instance lock at " + lockPath + ", continuing without single-instance support", e);
                releaseLock();
                enqueue(args);
                return true;
            }

            if (attempt >= MAX_CONNECT_ATTEMPTS) {
                LOGGER.error("Could not hand off launch arguments to a running Autogram instance");
                return false;
            }

            try {
                Thread.sleep(CONNECT_RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                // Nobody is expected to interrupt the startup; rather than
                // spinning through the remaining attempts, start the GUI.
                Thread.currentThread().interrupt();
                enqueue(args);
                return true;
            }
        }
    }

    SocketChannel openClientChannel() throws IOException {
        return SocketChannel.open(StandardProtocolFamily.UNIX);
    }

    ServerSocketChannel openServerChannel() throws IOException {
        return ServerSocketChannel.open(StandardProtocolFamily.UNIX);
    }

    private boolean tryConnectAndSend(List<String> args) {
        try (var channel = openClientChannel(); var out = Channels.newOutputStream(channel)) {
            channel.connect(UnixDomainSocketAddress.of(socketPath));
            out.write(String.join(ARG_SEPARATOR, args).getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            LOGGER.debug("Could not connect to running Autogram instance", e);
            return false;
        }
    }

    private boolean acquireLock() throws IOException {
        createPrivateDirectory(lockPath.getParent());
        var channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock acquired;
        try {
            acquired = channel.tryLock();
        } catch (OverlappingFileLockException e) {
            acquired = null;
        }

        if (acquired == null) {
            closeQuietly(channel);
            return false;
        }

        lockChannel = channel;
        return true;
    }

    private boolean startServer() {
        try {
            createPrivateDirectory(socketPath.getParent());
            Files.deleteIfExists(socketPath);
            serverChannel = openServerChannel();
            serverChannel.bind(UnixDomainSocketAddress.of(socketPath));
            restrictToOwner(socketPath);
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
            try {
                var channel = serverChannel.accept();
                Thread.startVirtualThread(() -> handleConnection(channel));
            } catch (ClosedChannelException e) {
                break;
            } catch (IOException | RuntimeException e) {
                if (running)
                    LOGGER.warn("Error accepting single-instance connection", e);
            }
        }
    }

    private void handleConnection(SocketChannel channel) {
        // A blocking read on a Unix domain socket has no timeout, so a client that
        // never closes its end is cut off by closing the channel under it.
        var watchdog = debounceExecutor.schedule(() -> closeQuietly(channel), CONNECTION_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try (channel; var in = Channels.newInputStream(channel)) {
            var message = in.readNBytes(MAX_MESSAGE_BYTES + 1);
            if (message.length > MAX_MESSAGE_BYTES) {
                LOGGER.warn("Ignoring single-instance request larger than {} bytes", MAX_MESSAGE_BYTES);
                return;
            }

            var args = parseArgs(new String(message, StandardCharsets.UTF_8));
            if (args.isEmpty())
                activate();
            else
                enqueue(args);
        } catch (IOException e) {
            LOGGER.debug("Failed to read single-instance request", e);
        } finally {
            watchdog.cancel(false);
        }
    }

    private static List<String> parseArgs(String message) {
        return Arrays.stream(message.split(ARG_SEPARATOR)).filter(arg -> !arg.isBlank()).toList();
    }

    private void activate() {
        Runnable handler;
        synchronized (argsLock) {
            handler = activateHandler;
            if (handler == null)
                activationPending = true;
        }
        if (handler != null)
            runHandler(handler);
    }

    private void enqueue(List<String> args) {
        if (args.isEmpty())
            return;
        synchronized (argsLock) {
            pendingArgs.addAll(args);
            if (argsHandler != null)
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
        synchronized (argsLock) {
            debounceTask = null;
            handler = argsHandler;
            batch = List.copyOf(pendingArgs);
            pendingArgs.clear();
        }
        if (handler != null && !batch.isEmpty())
            runHandler(() -> handler.accept(batch));
    }

    // A failing handler must not take the single-instance server down with it,
    // otherwise later launches would hand their files to an instance that
    // never processes them.
    private static void runHandler(Runnable handler) {
        try {
            handler.run();
        } catch (RuntimeException e) {
            LOGGER.error("Single-instance handler failed", e);
        }
    }

    private static void createPrivateDirectory(Path dir) throws IOException {
        if (Files.isDirectory(dir))
            return;
        Files.createDirectories(dir);
        restrictToOwner(dir);
    }

    private static void restrictToOwner(Path path) {
        if (!path.getFileSystem().supportedFileAttributeViews().contains("posix"))
            return;
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwx------"));
        } catch (IOException | UnsupportedOperationException e) {
            LOGGER.debug("Could not restrict permissions of " + path, e);
        }
    }

    private void closeServer() {
        if (serverChannel == null)
            return;
        closeQuietly(serverChannel);
        serverChannel = null;
        try {
            Files.deleteIfExists(socketPath);
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(Channel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private void releaseLock() {
        // Closing the channel also releases the lock held on it.
        if (lockChannel != null)
            closeQuietly(lockChannel);
        lockChannel = null;
    }
}
