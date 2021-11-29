package com.octosign.whitelabel.signing.certificate;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.Certificate;
import com.octosign.whitelabel.signing.Driver;
import com.octosign.whitelabel.ui.OS;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;

public class PKCS11Certificate extends Certificate {

    // TODO replace with properties
    private static final String eID = "eID";
    private static final String mandateCert = "Mandatny certifikat";

    private static final List<Driver> DRIVERS = List.of(
            new Driver(eID, OS.WINDOWS, "C:\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll"),
            new Driver(eID, OS.LINUX, "/usr/lib/eac_mw_klient/libpkcs11_x64.so"),
            new Driver(eID, OS.DARWIN, "/Applications/Aplikacia_pre_eID.app/Contents/pkcs11/libPkcs11.dylib"),

            new Driver(mandateCert, OS.WINDOWS, "C:\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll"),
            new Driver(mandateCert, OS.LINUX, "/usr/lib/pkcs11/libICASecureStorePkcs11.so")
//           , new Driver(mandateCert, OS.DARWIN, "")
    );

    /**
     * Creates signing certificate that will use given PKCS11
     */
    public PKCS11Certificate(DSSPrivateKeyEntry privateKey) {
        try {
            this.dssPrivateKey = privateKey;
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
    }

    public static List<Driver> getDrivers() {
        return DRIVERS;
    }
}
