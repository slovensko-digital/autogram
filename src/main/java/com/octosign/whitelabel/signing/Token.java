package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.token.PKCS11Token;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.octosign.whitelabel.ui.Utils.isNullOrEmpty;

public abstract class Token {
    private SignatureTokenConnection dssToken;

    private List<Certificate> certificates;

    protected Driver driver;

    protected void initialize(SignatureTokenConnection dssToken) {
        this.dssToken = dssToken;
        this.certificates = buildCertificates();
    }

    protected SignatureTokenConnection getDssToken() {
        return dssToken;
    }

    protected List<Certificate> buildCertificates() {
        return getAllPrivateKeys().stream()
                                  .map(key -> new Certificate(key, this))
                                  .toList();
    }

    public List<Certificate> getCertificates() {
        if (isNullOrEmpty(certificates))
            certificates = buildCertificates();

        return certificates;
    }

    protected List<DSSPrivateKeyEntry> getAllPrivateKeys() {
        List<DSSPrivateKeyEntry> keys;
        try {
            keys = dssToken.getKeys();
        } catch (Exception e) {
            throw new UserException("error.tokenNotAvailable.header", "error.tokenNotAvailable.description", e);
        }

        return keys;
    }


    public static List<Driver> getAvailableDrivers() {
        return Token.getAllDrivers().stream()
                    .filter(Driver::isCompatible)
                    .filter(Driver::isInstalled)
                    .toList();
    }

    public static Collection<Driver> getAllDrivers() {
        var drivers = new ArrayList<>(PKCS11Token.getDrivers());

        //  TODO - what else belongs here?
        //  do the other implementations (PKCS12, MSCAPI, MOCCHA) also have some kind of Drivers,
        //  or not necessarily/not at all?
        return drivers;
    }

}
