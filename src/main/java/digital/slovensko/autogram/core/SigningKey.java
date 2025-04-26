package digital.slovensko.autogram.core;

import java.util.HashMap;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

public class SigningKey {
    final AbstractKeyStoreTokenConnection token;
    final DSSPrivateKeyEntry privateKey;

    public SigningKey(AbstractKeyStoreTokenConnection token, DSSPrivateKeyEntry privateKey) {
        this.token = token;
        this.privateKey = privateKey;
    }

    public SignatureValue sign(ToBeSigned dataToSign, DigestAlgorithm algo) {
        if (algo == null)
            algo = DigestAlgorithm.SHA256;

        return token.sign(dataToSign, algo, privateKey);
    }

    public CertificateToken getCertificate() {
        return privateKey.getCertificate();
    }

    public String prettyPrintCertificateDetails() throws InvalidNameException {
        var map = new HashMap<String, String>();
        var ldapn = new LdapName(privateKey.getCertificate().getSubject().getRFC2253());
        for (Rdn rdn : ldapn.getRdns())
            map.put(rdn.getType(), rdn.getValue().toString());
        
        return "SN=" + privateKey.getCertificate().getSerialNumber().toString()
            + ",CN=" + map.get("CN") + ",C=" + map.get("C") + ",L=" + map.get("L");
    }

    public CertificateToken[] getCertificateChain() {
        return privateKey.getCertificateChain();
    }

    public void close() {
        token.close();
    }
}
