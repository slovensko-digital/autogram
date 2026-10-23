package digital.slovensko.autogram.util;

import java.io.IOException;
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

    // In a package, AF_UNIX sockets can't be connected to via the 8.3 short path Windows gives TEMP for long user names,
    // nor in new directories in AppData, which are redirected to the package
    public static Path socketDirectory() throws IOException {
        return Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
    }

    // NIO selectors connect to an AF_UNIX socket in this directory, the app couldn't start otherwise
    public static void useSocketDirectoryForNio() {
        try {
            System.setProperty("jdk.net.unixdomain.tmpdir", socketDirectory().toString());
        } catch (IOException e) {
            Logging.log("Unable to resolve socket directory: " + e);
        }
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
