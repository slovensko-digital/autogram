package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.nio.file.Path;
import java.security.KeyStore;

public class PKCS11TokenDriver extends TokenDriver {
    public PKCS11TokenDriver(String name, Path path, boolean needsPassword, String shortname) {
        super(name, path, needsPassword, shortname);
    }

    @Override
    public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
        return new Pkcs11SignatureToken(getPath().toString(), new KeyStore.PasswordProtection(password), -1);
    }
}
