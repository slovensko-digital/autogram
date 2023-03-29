package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class TokenDriver {
    protected final String name;

    public TokenDriver(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<TokenDriver> getAvailableDrivers() {
        return Arrays.asList(
                new PKCS11TokenDriver("eID", "/usr/lib/eID_klient/libpkcs11_x64.so"),
                new PKCS11TokenDriver("ICASecureStore", "/usr/lib/pkcs11/libICASecureStorePkcs11.so")
        );
    }

    public abstract AbstractKeyStoreTokenConnection createToken() throws IOException;
}
