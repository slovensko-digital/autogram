package digital.slovensko.autogram.drivers;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import digital.slovensko.autogram.util.Logging;
import digital.slovensko.autogram.util.OperatingSystem;

final class Pkcs11Library {
    private static final int LOAD_WITH_ALTERED_SEARCH_PATH = 0x8;
    private static final Set<String> loaded = ConcurrentHashMap.newKeySet();

    private Pkcs11Library() {
    }

    // SunPKCS11 finds dependencies of the library in its directory only via PATH, which apps from Microsoft Store
    // don't use, and when the library fails to load, it frees an uninitialized pointer and crashes
    static void load(String path) throws Pkcs11LibraryException {
        if (OperatingSystem.current() != OperatingSystem.WINDOWS || loaded.contains(path))
            return;

        MemorySegment module;
        try (var arena = Arena.ofConfined()) {
            var kernel32 = SymbolLookup.libraryLookup("kernel32", arena);
            var loadLibraryEx = Linker.nativeLinker().downcallHandle(kernel32.findOrThrow("LoadLibraryExW"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
            module = (MemorySegment) loadLibraryEx.invokeExact(arena.allocateFrom(path, UTF_16LE), MemorySegment.NULL,
                    LOAD_WITH_ALTERED_SEARCH_PATH);
        } catch (Throwable e) {
            Logging.log("Unable to load " + path + " before SunPKCS11: " + e);
            return;
        }

        if (module.address() == 0)
            throw new Pkcs11LibraryException(path);

        loaded.add(path);
    }
}
