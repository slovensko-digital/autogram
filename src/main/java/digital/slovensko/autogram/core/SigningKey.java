package digital.slovensko.autogram.core;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public class SigningKey {
    final AbstractKeyStoreTokenConnection token;
    final DSSPrivateKeyEntry privateKey;

    public SigningKey(AbstractKeyStoreTokenConnection token, DSSPrivateKeyEntry privateKey) {
        this.token = token;
        this.privateKey = privateKey;
    }


    public SignatureValue sign(ToBeSigned dataToSign, DigestAlgorithm algo) {
        return token.sign(dataToSign, algo, privateKey);
    }

    public CertificateToken getCertificate() {
        return privateKey.getCertificate();
    }

    public CertificateToken[] getCertificateChain() {
        return privateKey.getCertificateChain();
    }
}
