package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.signing.certificate.PKCS11Certificate;
import com.octosign.whitelabel.signing.token.PKCS11Token;
import com.octosign.whitelabel.ui.PasswordCallback;

import java.util.List;

public class TokenFactory {

    public static List<Driver> getDrivers() {
        return getAllDrivers().stream()
                           .filter(Driver::isCompatible)
                           .filter(Driver::isInstalled)
                           .toList();
    }

    public static List<Driver> getAllDrivers() {
        return PKCS11Certificate.getDrivers();
    }

    public static Token getToken(Driver driver) {
        return new PKCS11Token(driver.getPath(), new PasswordCallback(), -1);
    }
}
