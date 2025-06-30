package digital.slovensko.autogram.drivers;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

public class PKCS12KeystoreTokenDriver extends TokenDriver {
    public PKCS12KeystoreTokenDriver(String name, Path path, String shortname, String noKeysHelperText) {
        super(name, path, shortname, noKeysHelperText);
    }

    @Override
    public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
        try {
            var password = pm.getPassword();
            if (password == null)
                throw new PasswordNotProvidedException();

            return new Pkcs12SignatureToken(getPath().toString(), new KeyStore.PasswordProtection(password));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DSSException e) {
            if (e.getCause().getMessage().equals("keystore password was incorrect"))
                throw new PINIncorrectException();

            throw e;
        }
    }
}
