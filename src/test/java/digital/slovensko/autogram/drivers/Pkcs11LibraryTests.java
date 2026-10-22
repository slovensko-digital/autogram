package digital.slovensko.autogram.drivers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pkcs11LibraryTests {
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testLibraryThatCannotBeLoadedIsReported(@TempDir Path tempDir) {
        var library = tempDir.resolve("pkcs11.dll").toString();

        var e = assertThrows(Pkcs11LibraryException.class, () -> Pkcs11Library.load(library));
        assertTrue(e.getMessage().endsWith(library), e.getMessage());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testLibraryIsLoaded() {
        assertDoesNotThrow(() -> Pkcs11Library.load("C:\\Windows\\System32\\winscard.dll"));
    }
}
