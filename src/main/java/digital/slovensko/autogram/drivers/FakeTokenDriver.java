package digital.slovensko.autogram.drivers;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Objects;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

public class FakeTokenDriver extends TokenDriver {
    public FakeTokenDriver(String name, Path path, boolean needsPassword) {
        super(name, path, needsPassword);
    }


    @Override
    public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
        try {
            var keystore = Objects.requireNonNull(this.getClass().getResource("FakeTokenDriver.keystore")).getFile();
            return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("".toCharArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}