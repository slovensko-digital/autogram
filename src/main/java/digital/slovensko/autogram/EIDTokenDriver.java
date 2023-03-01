package digital.slovensko.autogram;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public class EIDTokenDriver extends TokenDriver {
    public AbstractKeyStoreTokenConnection createToken() {
        var pkcsPath = "/usr/lib/eID_klient/libpkcs11_x64.so"; // Slovak eID default installation directory
        PasswordInputCallback passwordCallback = new DummyPasswordCallback();
        return new Pkcs11SignatureToken(pkcsPath, passwordCallback, -1);
    }
}
