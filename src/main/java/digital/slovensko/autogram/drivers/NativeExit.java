package digital.slovensko.autogram.drivers;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ends the process without running native destructors of loaded libraries.
 * <p>
 * Both System.exit and Runtime.halt end with C exit(), which runs them, so a library whose destructor hangs keeps the
 * process running forever. Needs native access enabled for app code (--enable-native-access=ALL-UNNAMED), otherwise
 * Java only warns about it, or refuses it, and the process exits as usual.
 */
final class NativeExit {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeExit.class);

    private NativeExit() {
    }

    /**
     * Ends the process right away, returns only if that's not possible.
     */
    static void exitImmediately(int status) {
        LOGGER.warn("Exiting without running native destructors");
        System.out.flush();
        System.err.flush();

        try {
            var linker = Linker.nativeLinker();
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                // ExitProcess, and _exit which calls it, notify loaded libraries, unlike TerminateProcess
                var kernel32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
                var terminateProcess = linker.downcallHandle(kernel32.findOrThrow("TerminateProcess"),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
                var currentProcess = MemorySegment.ofAddress(-1); // GetCurrentProcess() pseudo handle
                var result = (int) terminateProcess.invokeExact(currentProcess, status);
                LOGGER.warn("Unable to terminate process: {}", result);
            } else {
                var exit = linker.downcallHandle(linker.defaultLookup().findOrThrow("_exit"),
                        FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT));
                exit.invokeExact(status);
            }
        } catch (Throwable e) {
            LOGGER.warn("Unable to exit immediately: {}", e.toString());
        }
    }
}
