package digital.slovensko.autogram.util;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;

public final class MsixPackage {
    private static final int ERROR_INSUFFICIENT_BUFFER = 122;
    private static Boolean current;

    private MsixPackage() {
    }

    public static synchronized boolean isCurrent() {
        if (current == null)
            current = OperatingSystem.current() == OperatingSystem.WINDOWS && hasPackageIdentity();

        return current;
    }

    // AF_UNIX sockets can't be connected to in AppData, which is redirected to the package, on some systems TEMP too
    public static Path socketDirectory() {
        return Path.of(System.getProperty("user.home"), ".autogram");
    }

    // NIO selectors (the server for websites, HttpClient) connect to an AF_UNIX socket in TEMP, which can fail in
    // a package; when it can't be created in the given directory, they use a TCP loopback connection instead
    public static void useTcpForNioPipes() {
        System.setProperty("jdk.net.unixdomain.tmpdir",
                Path.of(System.getProperty("java.home"), "no-unix-domain-sockets").toString());
    }

    private static boolean hasPackageIdentity() {
        try (var arena = Arena.ofConfined()) {
            var linker = Linker.nativeLinker();
            var kernel32 = SymbolLookup.libraryLookup("kernel32", arena);
            var getCurrentPackageFullName = linker.downcallHandle(kernel32.findOrThrow("GetCurrentPackageFullName"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            var length = arena.allocate(ValueLayout.JAVA_INT);
            return (int) getCurrentPackageFullName.invokeExact(length, MemorySegment.NULL) == ERROR_INSUFFICIENT_BUFFER;
        } catch (Throwable e) {
            Logging.log("Unable to find out whether running from MSIX package: " + e);
            return false;
        }
    }
}
