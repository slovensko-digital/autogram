package digital.slovensko.autogram.drivers;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

public class PKCS12KeystoreTokenDriver extends TokenDriver {
    private final boolean needsPassword;

    public PKCS12KeystoreTokenDriver(String name, Path path, boolean needsPassword, String shortname) {
        super(name, path, shortname);
        this.needsPassword = needsPassword;
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken(Integer slotId, PasswordManager pm, SignatureTokenSettings settings) {
        try {
            var password = needsPassword ? pm.getPassword() : "".toCharArray();
            return new Pkcs12SignatureToken(getPath().toString(), new KeyStore.PasswordProtection(password));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
