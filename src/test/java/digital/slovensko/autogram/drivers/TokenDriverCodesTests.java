package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.TokenCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenDriverCodesTests {
    @ParameterizedTest
    @CsvSource({
            "eid, true, true, 6",
            // cards whose PINs are digits: a letter in them is a typing mistake
            "cz_eid, false, true, 0",
            "secure_store, false, true, 0",
            "monet, false, true, 0",
            // SafeNet eToken passwords and e.g. YubiKey PIV PINs may have letters
            "gemalto, false, false, 0",
            "custom_pkcs11, false, false, 0",
    })
    void testPkcs11DriverCodes(String shortname, boolean bok, boolean digitsOnly, int length) {
        var driver = new PKCS11TokenDriver("driver", Path.of("/lib.so"), shortname, "");

        assertEquals(new TokenCodes(bok, digitsOnly, length), driver.getTokenCodes());
    }

    @Test
    void testKeystoreFilePasswordMayHaveLetters() {
        var driver = new PKCS12KeystoreTokenDriver("keystore", Path.of("/keystore.p12"), "keystore", "");

        assertEquals(TokenCodes.ANY, driver.getTokenCodes());
    }
}
