package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.signing.certificate.PKCS11Certificate;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import static com.octosign.whitelabel.ui.Utils.isNullOrEmpty;

public class Token {

    protected AbstractKeyStoreTokenConnection dssToken;

    protected Driver driver;

    protected List<? extends Certificate> certificates = new ArrayList<>();


    public List<? extends Certificate> getCertificates() {
        if (isNullOrEmpty(certificates))
            loadCertificates();

        return certificates;
    }

    private void loadCertificates() {
        certificates = dssToken.getKeys().stream().map(PKCS11Certificate::new).toList();
    }

    public boolean containsMultipleCertificates() {
        return (certificates.size() > 1) && (dssToken.getKeys().size() > 1);
    }
}
