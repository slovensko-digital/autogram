package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.ui.CertificateSelect;
import com.octosign.whitelabel.ui.PasswordCallback;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public class CertificateFactory {
    public static SigningCertificatePKCS11 create(Driver driver) {
        var token = new Pkcs11SignatureToken(driver.getPath(), new PasswordCallback(), -1);
        var keys = SigningCertificate.getAvailablePrivateKeys(token);

        DSSPrivateKeyEntry privateKey = (keys.size() > 1)
                                                ? new CertificateSelect(keys).select()
                                                : keys.get(0);

        return new SigningCertificatePKCS11(token, privateKey);
    }
}
