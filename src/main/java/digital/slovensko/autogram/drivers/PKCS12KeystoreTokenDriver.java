package digital.slovensko.autogram.drivers;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

public class PKCS12KeystoreTokenDriver extends TokenDriver {
    public PKCS12KeystoreTokenDriver(String name, Path path, String shortname, String noKeysHelperText) {
        super(name, path, shortname, noKeysHelperText);
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
        try {
            return new Pkcs12SignatureToken(getPath().toString(), new KeyStore.PasswordProtection(pm.getPassword()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
