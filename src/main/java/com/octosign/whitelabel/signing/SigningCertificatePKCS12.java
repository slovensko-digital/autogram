package com.octosign.whitelabel.signing;

import java.security.KeyStore.PasswordProtection;

import com.octosign.whitelabel.error_handling.*;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import java.security.KeyStore;
import java.util.Locale;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getPropertyArray;
import static com.octosign.whitelabel.ui.I18n.translate;

public class SigningCertificatePKCS12 extends SigningCertificate {

    public static final String[] LINUX_DLLS = getPropertyArray("[].drivers.pkcs12.linux");

    public static final String[] WINDOWS_DLLS = null;   //getPropertyArray("[].drivers.pkcs12.win");
    public static final String[] DARWIN_DLLS = null;    //getPropertyArray("[].drivers.pkcs12.mac");

    public static SigningCertificatePKCS12 create(String driverPath, String password) {
        var passwordProtection = new KeyStore.PasswordProtection(password.toCharArray());
        return new SigningCertificatePKCS12(driverPath, passwordProtection);
    }

    // TODO delete, or extract to superclass
    public static String[] resolvePkcsDriverPath() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            throw new UnsupportedOperationException(); // not implemented
        } else if (osName.contains("win")) {
            throw new UnsupportedOperationException(); // not implemented
        } else if (osName.contains("nux")) {
            return LINUX_DLLS;
        } else {
            throw new UserException("error.unknownOS.header", translate("error.unknownOS.description", osName));
        }
    }

    public SigningCertificatePKCS12(String driverPath, PasswordProtection passwordProtection) {
        try {
            token = new Pkcs12SignatureToken(driverPath, passwordProtection);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
    }
}
