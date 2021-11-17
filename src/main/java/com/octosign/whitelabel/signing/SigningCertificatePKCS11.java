package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.io.File;
import java.util.Locale;
import java.util.stream.Stream;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getPropertyArray;
import static com.octosign.whitelabel.ui.I18n.translate;

public class SigningCertificatePKCS11 extends SigningCertificate {
    public static final String[] WINDOWS_PKCS_DLLS = getPropertyArray("[].drivers.pkcs11.windows"); // Slovak eID default installation directory

    public static final String[] LINUX_PKCS_DLLS = getPropertyArray("[].drivers.pkcs11.linux");

    public static final String[] DARWIN_PKCS_DLLS = getPropertyArray("[].drivers.pkcs11.darwin"); // Slovak eID default installation directory

    /**
     * Tries to find one of the known PKCS11 DLLs
     *
     * @param passwordCallback Callback that should gather password from the user
     * @return SigningCertificatePKCS11 that uses found PKCS11 DLL or null if none found.
     */
    public static SigningCertificatePKCS11 createFromDetected(PasswordInputCallback passwordCallback) {
        String[] paths = resolvePkcsDriverPath();

        for (String path: paths) {
            if ((new File(path)).exists()) {
                return new SigningCertificatePKCS11(path, passwordCallback);
            }
        }
        return null;
    }

    public static String[] resolvePkcsDriverPath() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return DARWIN_PKCS_DLLS;
        } else if (osName.contains("win")) {
            return WINDOWS_PKCS_DLLS;
        } else if (osName.contains("nux")) {
            return LINUX_PKCS_DLLS;
        } else {
            throw new UserException("error.unknownOS.header", translate("error.unknownOS.description", osName));
        }
    }

    public static String[] getAllPkcs11DriverPaths() {
        return Stream.of(WINDOWS_PKCS_DLLS, LINUX_PKCS_DLLS, DARWIN_PKCS_DLLS)
                .flatMap(Stream::of)
                .toArray(String[]::new);
    }

    /**
     * Creates signing certificate that will use given PKCS11
     */
    public SigningCertificatePKCS11(String pkcsPath, PasswordInputCallback passwordCallback) {
        try {
            // TODO: Get a list of slots with tokens and let user choose
            // Currently, default slot should be used if the int is negative
            // We probably have to use reflection (.getClass().getMethod())
            // using method C_GetSlotList with true as parameter to get slots with tokens
            // and C_GetSlotInfo/C_GetTokenInfo for info about these slots
            token = new Pkcs11SignatureToken(pkcsPath, passwordCallback, -1);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
    }
}
