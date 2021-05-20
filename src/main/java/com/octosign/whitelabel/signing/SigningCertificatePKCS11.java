package com.octosign.whitelabel.signing;

import java.io.File;
import java.util.Locale;

import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public class SigningCertificatePKCS11 extends SigningCertificate {
    /**
     * Tries to find one of the known PKCS11 DLLs
     *
     * @param passwordCallback Callback that should gather password from the user
     * @return SigningCertificatePKCS11 that uses found PKCS11 DLL or null if none found.
     */
    public static SigningCertificatePKCS11 createFromDetected(PasswordInputCallback passwordCallback) {
        String[] windowsPkcsDlls = {
                // Slovak eID default installation directory
                "C:\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll" };
        String[] linuxPkcsDlls = {
                // Slovak eID default installation directory
                "/usr/lib/eac_mw_klient/libpkcs11_x64.so" };
        String[] darwinPkcsDlls = {
                // Slovak eID default installation directory
                "/Applications/Aplikacia_pre_eID.app/Contents/pkcs11/libPkcs11.dylib" };

        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        String[] paths;
        if ((osName.indexOf("mac") >= 0) || (osName.indexOf("darwin") >= 0)) {
            paths = darwinPkcsDlls;
        } else if (osName.indexOf("win") >= 0) {
            paths = windowsPkcsDlls;
        } else if (osName.indexOf("nux") >= 0) {
            paths = linuxPkcsDlls;
        } else {
            return null;
        }

        for (String path: paths) {
            if ((new File(path)).exists()) {
                return new SigningCertificatePKCS11(path, passwordCallback);
            }
        }

        return null;
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
            throw new RuntimeException("Cannot initialize MSCAPI", e);
        }
    }
}
