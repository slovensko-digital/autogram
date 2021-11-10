package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.octosign.whitelabel.signing.SigningCertificatePKCS11.*;
import static java.util.stream.Stream.of;

/**
 * Holds currently used certificate and takes care of picking
 */
public class CertificateManager {
    /**
     * Currently selected certificate
     */
    private SigningCertificate certificate;


    public static final String[] MANDATE_PATHS = { "/etc/hosts/somedir", "/home/michal/nonexistentdir" };

    // dummy method somehow returning required paths
    public String[] getAllMandatePaths() {
        return MANDATE_PATHS;
    }

    /**
     * Loads and sets default signing certificate
     */

    public SigningCertificate resolveCertificate() {
//        var eidPaths = resolvePkcsDriverPath();
        List<File> drivers = Utils.toFlattenedList(getAllPkcs11DriverPaths(), getAllMandatePaths())
                .stream()
                .map(File::new)
                .filter(File::exists)
                .toList();

        if (drivers.isEmpty()) throw new UserException("");
        if (drivers.size() == 1) return useDriver(drivers.get(0));

        var path = (validPaths.length == 1) ? validPaths[0] : "";

        if  {
            return useDriverPath();
        } else {
            return useDriverPath(eidPaths[0]);
        }

        certificate = getDefaulCertificate();
        return certificate;
    }

    private File[] getValidDriverPaths(String[]... paths) {
        return of(paths)
                .flatMap(Stream::of)
                .map(File::new)
                .filter(File::exists)
                .toArray(File[]::new);
    }

    // dummy
    private void selectDriver() {
        // TODO
    }

    // dummy
    private void useDriver(File driver) {
        // TODO
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
}
