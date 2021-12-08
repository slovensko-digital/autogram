package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import java.util.*;

import static com.octosign.whitelabel.ui.Utils.isNullOrEmpty;

public abstract class Token {
    private static List<Driver> DRIVERS;

    private SignatureTokenConnection dssToken;

    private List<Certificate> certificates;

    protected Driver driver;

    protected void initialize(SignatureTokenConnection dssToken) {
        this.dssToken = dssToken;
        this.certificates = buildCertificates();
    }

    public static Token fromDriver(Driver driver) {
        return driver.getKeystoreType().createToken(driver);
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
        return Token.getDrivers().stream()
                    .filter(Driver::isCompatible)
                    .filter(Driver::isInstalled)
                    .toList();
    }

    public static Collection<Driver> getDrivers() {
        if (DRIVERS == null)
            DRIVERS = new ArrayList<>();

        return DRIVERS;
    }

    protected static void registerDriver(Driver driver) {
        getDrivers().add(driver);
    }
}
