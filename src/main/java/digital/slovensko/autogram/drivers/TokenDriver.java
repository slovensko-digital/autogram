package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class TokenDriver {
    public static List<TokenDriver> getAvailableDrivers() {
        return Arrays.asList(new EIDTokenDriver());
    }

    public abstract AbstractKeyStoreTokenConnection createToken() throws IOException;
}
