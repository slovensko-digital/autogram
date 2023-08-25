package digital.slovensko.autogram.drivers;

import java.nio.file.Path;
import java.security.KeyStore;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.PrefilledPasswordCallback;

public class PKCS11GemaltoMandateTokenDriver extends TokenDriver {
    public PKCS11GemaltoMandateTokenDriver(String name, Path path, boolean needsPassword, String shortname) {
        super(name, path, needsPassword, shortname);
    }

    @Override
    public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
        return new Pkcs11SignatureToken(getPath().toString(), new PrefilledPasswordCallback(new KeyStore.PasswordProtection(password)), -1, 8, null);
    }
}
