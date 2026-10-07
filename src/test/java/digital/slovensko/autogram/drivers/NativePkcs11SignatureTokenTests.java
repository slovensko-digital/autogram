package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.UserSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.ProviderException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativePkcs11SignatureTokenTests {
    @Test
    void testRemovedCardIsNotPolledForInBackground(@TempDir Path tempDir) {
        // SunPKCS11 requires an absolute library path, which differs between platforms
        var library = tempDir.resolve("libpkcs11.so").toString();
        var config = new NativePkcs11SignatureToken(library, null, new UserSettings(), -1, 0) {
            String config() {
                return buildConfig();
            }
        }.config();

        assertTrue(config.lines().anyMatch(NativePkcs11SignatureToken.NO_TOKEN_POLLING_CONFIG::equals), config);
        // SunPKCS11 accepts the configuration, it fails only on the missing library
        var e = assertThrows(ProviderException.class, () -> Security.getProvider("SunPKCS11").configure("--" + config));
        assertEquals("Library " + library + " does not exist", e.getMessage());
    }
}
