package digital.slovensko.autogram.drivers;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Objects;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

public class FakeTokenDriver extends TokenDriver {
    public FakeTokenDriver(String name, Path path, String shortname, String noKeysHelperText) {
        super(name, path, shortname, noKeysHelperText);
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
        try {
            var keystore = Objects.requireNonNull(this.getClass().getResource("FakeTokenDriver.keystore")).getFile();
            return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("".toCharArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
