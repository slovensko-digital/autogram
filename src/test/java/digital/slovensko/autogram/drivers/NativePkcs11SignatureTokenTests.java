package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.TokenCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        var config = new NativePkcs11SignatureToken(library, null, -1, 0, TokenCodes.ANY) {
            String config() {
                return buildConfig();
            }
        }.config();

        assertTrue(config.lines().anyMatch(NativePkcs11SignatureToken.NO_TOKEN_POLLING_CONFIG::equals), config);
        // SunPKCS11 accepts the configuration, it fails only on the missing library
        var e = assertThrows(ProviderException.class, () -> Security.getProvider("SunPKCS11").configure("--" + config));
        assertEquals("Library " + library + " does not exist", e.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            // key without always authenticate needs no PIN for each signature
            "false, EID, false, true, false",
            // eID: Autogram asks for the signing PIN even for one signature, eID klient's window never opens
            "true, EID, false, true, true",
            // other tokens with a protected authentication path ask for it themselves, except in a batch
            "true, ANY, false, true, false",
            "true, ANY, true, true, true",
            "true, ANY, false, false, true",
    })
    void testWhenAutogramAsksForTheSigningPin(boolean alwaysAuthenticate, String codes, boolean cachingPin, boolean protectedPath, boolean asks) throws Exception {
        var tokenCodes = codes.equals("EID") ? TokenCodes.EID : TokenCodes.ANY;

        assertEquals(asks, NativePkcs11SignatureToken.asksForSigningPin(alwaysAuthenticate, tokenCodes, cachingPin, () -> protectedPath));
    }
}
