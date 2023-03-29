package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.io.IOException;

public class PKCS11TokenDriver extends TokenDriver {
    private final String pkcsPath;

    public PKCS11TokenDriver(String name, String pkcsPath) {
        super(name);
        this.pkcsPath = pkcsPath;
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken() throws IOException {
        PasswordInputCallback passwordCallback = new DummyPasswordCallback();
        return new Pkcs11SignatureToken(pkcsPath, passwordCallback, -1);
    }
}
