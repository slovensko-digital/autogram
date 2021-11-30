package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.Driver;
import com.octosign.whitelabel.signing.Token;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.util.List;

import static com.octosign.whitelabel.signing.API.*;
import static com.octosign.whitelabel.ui.I18n.*;
import static com.octosign.whitelabel.signing.OperatingSystem.*;

public class PKCS11Token extends Token {

    private static final String eID = translate("btn.eID");
    private static final String mandateCertificate = translate("btn.mandateCert");

    private static final List<Driver> DRIVERS = List.of(
            Driver.of(eID)
                  .file(WINDOWS, "C:\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll")
                  .file(LINUX, "/usr/lib/eac_mw_klient/libpkcs11_x64.so")
                  .file(MAC, "/Applications/Aplikacia_pre_eID.app/Contents/pkcs11/libPkcs11.dylib")
                  .with(PKCS11),

            Driver.of(mandateCertificate)
                    .file(WINDOWS, "???")
                    .file(LINUX, "/usr/lib/pkcs11/libICASecureStorePkcs11.so")
                    .with(PKCS11)
    );

    public PKCS11Token(String path, PasswordInputCallback passwordInputCallback, int slotId) {
        Pkcs11SignatureToken token;
        try {
            token = new Pkcs11SignatureToken(path, passwordInputCallback, slotId, -1, null);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
        initialize(token);
    }

    public PKCS11Token(String path, PasswordInputCallback passwordInputCallback) {
        this(path, passwordInputCallback, -1);
    }

    public static List<Driver> getDrivers() {
        return DRIVERS;
    }
}
