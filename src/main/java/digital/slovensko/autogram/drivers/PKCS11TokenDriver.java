package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.PrefilledPasswordCallback;

import java.nio.file.Path;
import java.security.KeyStore;

public class PKCS11TokenDriver extends TokenDriver {
    public PKCS11TokenDriver(String name, Path path, String shortname) {
        super(name, path, shortname);
    }

    public AbstractKeyStoreTokenConnection createToken(Integer slotId, PasswordManager pm, SignatureTokenSettings settings) {
        return new NativePkcs11SignatureToken(getPath().toString(), pm, settings);
    }
}
