package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.PrefilledPasswordCallback;

import java.nio.file.Path;
import java.security.KeyStore;

public class PKCS11TokenDriver extends TokenDriver {
    public PKCS11TokenDriver(String name, Path path, boolean needsPassword, String shortname) {
        super(name, path, needsPassword, shortname);
    }

    @Override
    public AbstractKeyStoreTokenConnection createTokenWithPassword(Integer slotIndex, char[] password) {
        // TODO: shouldProvidePasswordForCkaAA happens to correlate with needsPassword() for now, because eID is different. Might be changed in the future.
        return new NativePkcs11SignatureToken(getPath().toString(), new PrefilledPasswordCallback(new KeyStore.PasswordProtection(password)), slotIndex, needsPassword());
    }
}
