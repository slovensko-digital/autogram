package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.nio.file.Path;

public class PKCS11TokenDriver extends TokenDriver {
    public PKCS11TokenDriver(String name, Path path, boolean needsPassword) {
        super(name, path, needsPassword);
    }

    @Override
    public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
        PasswordInputCallback passwordCallback = new StaticPasswordCallback(password);
        return new Pkcs11SignatureToken(getPath().toString(), passwordCallback, -1);
    }
}
