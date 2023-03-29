package digital.slovensko.autogram.core;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

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

    public Map<String, String> getCertificateDetails() throws InvalidNameException {
        var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        var notBefore = dateFormat.format(privateKey.getCertificate().getNotBefore());
        var notAfter = dateFormat.format(privateKey.getCertificate().getNotAfter());

        var ldapDN = new LdapName(privateKey.getCertificate().getSubject().getRFC2253());
        var dnName = "";
        var dnCountry = "";
        var dnCity = "";
        var dnStreet = "";

        for (Rdn rdn : ldapDN.getRdns()) {
            if (rdn.getType().equalsIgnoreCase("CN"))
                dnName = rdn.getValue().toString();
            if (rdn.getType().equalsIgnoreCase("C"))
                dnCountry = rdn.getValue().toString();
            if (rdn.getType().equalsIgnoreCase("L"))
                dnCity = rdn.getValue().toString();
            if (rdn.getType().equalsIgnoreCase("STREET"))
                dnStreet = rdn.getValue().toString();
        }

        return Map.of(
                "name", dnName,
                "country", dnCountry,
                "city", dnCity,
                "street", dnStreet,
                "notBefore", notBefore,
                "notAfter", notAfter);
    }

    public CertificateToken[] getCertificateChain() {
        return privateKey.getCertificateChain();
    }
}
