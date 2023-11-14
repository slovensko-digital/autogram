package digital.slovensko.autogram.drivers;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

public class PKCS12KeystoreTokenDriver extends TokenDriver {
    public PKCS12KeystoreTokenDriver(String name, Path path, boolean needsPassword, String shortname) {
        super(name, path, needsPassword, shortname);
    }


    @Override
    public AbstractKeyStoreTokenConnection createTokenWithPassword(Integer slotId, char[] password) {
        try {
            return new Pkcs12SignatureToken(getPath().toString(), new KeyStore.PasswordProtection(password));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
