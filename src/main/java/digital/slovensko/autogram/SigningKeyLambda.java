package digital.slovensko.autogram;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public interface SigningKeyLambda {
    public void call(SigningKey key);
}
