package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;
import com.octosign.whitelabel.signing.SigningCertificatePKCS12;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Holds currently used certificate and takes care of picking
 */
public class CertificateManager {
    /**
     * Currently selected certificate
     */
    private SigningCertificate certificate;

    /**
     * Loads and sets default signing certificate
     */
    public SigningCertificate useDefault() {
        certificate = getDefaulCertificate();
        return certificate;
    }

    /**
     * Currently uses certificate
     */
    public SigningCertificate getCertificate() {
        return certificate;
    }

    /**
     * Tries to automatically choose the most appropriate token and private key
     */
    private static SigningCertificate getDefaulCertificate() {
        SigningCertificate signingCertificate;

        try {
            signingCertificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback());
        } catch (Exception e) {
            throw new IntegrationException(Code.PKCS11_INIT_FAILED, e);
        }

        // Try to fallback to MSCAPI on Windows
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if (signingCertificate == null && osName.contains("win")) {
            try {
                signingCertificate = new SigningCertificateMSCAPI();
            } catch (Exception e) {
                throw new IntegrationException(Code.MSCAPI_INIT_FAILED, e);
            }
        }

        if (signingCertificate == null)
            throw new UserException("error.tokenNotFound.header",
                    "error.tokenNotFound.description");

        List<DSSPrivateKeyEntry> keys;
        try {
            keys = signingCertificate.getAvailablePrivateKeys();
        } catch (Exception e) {
            throw new UserException("error.tokenNotAvailable.header",
                    "error.tokenNotAvailable.description",
                    e);
        }

        if (keys.size() == 0)
            throw new UserException("error.tokenEmpty.header", "error.tokenEmpty.description");

        // Use the first available key
        signingCertificate.setPrivateKey(keys.get(0));

        return signingCertificate;
    }

    public static List<DSSPrivateKeyEntry> getFakeSigner() {
        var p11 = SigningCertificatePKCS11.getTemp();
        var p12 = SigningCertificatePKCS12.createTemp();
        System.out.println();
        return Arrays.asList(p11, p12);
    }
}
