package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getPropertyArray;
import static com.octosign.whitelabel.ui.I18n.translate;

public class SigningCertificatePKCS12 extends SigningCertificate {


    public static SigningCertificatePKCS12 create(String driverPath, String password) {
        var passwordProtection = new KeyStore.PasswordProtection(password.toCharArray());
        return new SigningCertificatePKCS12(driverPath, passwordProtection);
    }
    /**
     * Creates signing certificate that will use given PKCS12 file
     *
     * @param pkcsPath Absolute path to PKCS12 file (.p12 or .pfx)
     * @param passwordCallback Callback that should gather password from the user
     */
    public SigningCertificatePKCS12(String pkcsPath, PasswordInputCallback passwordCallback) {
        // TODO: Can we check if file has no password so we don't ask for empty
        // password?
        PasswordProtection password = new PasswordProtection(passwordCallback.getPassword());

        try {
            // TODO: Get a list of slots with tokens and let user choose
            // Currently, default slot should be used if the int is negative
            // We probably have to use reflection (.getClass().getMethod())
            // using method C_GetSlotList with true as parameter to get slots with tokens
            // and C_GetSlotInfo/C_GetTokenInfo for info about these slots
            token = new Pkcs12SignatureToken(pkcsPath, password);
        } catch (Exception e) {
            throw new UserException(translate("error.pkcs12InitFailed", e));
        }
    }

    public static DSSPrivateKeyEntry createTemp() {
        String[] paths = getPropertyArray("[].driver.mandate.linux");

        var signingCertificate = SigningCertificatePKCS12.create( paths[0], "null");
        return signingCertificate.token.getKeys().get(0);

    }

    public SigningCertificatePKCS12(String driverPath, PasswordProtection passwordProtection) {
        try {
            token = new Pkcs12SignatureToken(driverPath, passwordProtection);
        } catch (Exception e) {
            throw new UserException(translate("error.pkcs12InitFailed", e));
        }
    }
}
