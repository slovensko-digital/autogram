package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.io.IOException;
import java.nio.file.Path;

public class PKCS11TokenDriver extends TokenDriver {
    public PKCS11TokenDriver(String name, Path path) {
        super(name, path);
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken() throws IOException {
        PasswordInputCallback passwordCallback = new DummyPasswordCallback(); // TODO some drivers actually need password
        return new Pkcs11SignatureToken(getPath().toString(), passwordCallback, -1);
    }
}
