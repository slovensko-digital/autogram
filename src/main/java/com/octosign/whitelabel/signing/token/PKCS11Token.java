package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.Driver;
import com.octosign.whitelabel.signing.Token;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import static com.octosign.whitelabel.signing.KeystoreType.*;
import static com.octosign.whitelabel.ui.I18n.*;
import static com.octosign.whitelabel.signing.OperatingSystem.*;

public class PKCS11Token extends Token {
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

    static {
        registerDriver(Driver.name(translate("btn.eID"))
                             .file(WINDOWS, "C:\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll")
                             .file(LINUX, "/usr/lib/eac_mw_klient/libpkcs11_x64.so")
                             .file(MAC, "/Applications/Aplikacia_pre_eID.app/Contents/pkcs11/libPkcs11.dylib")
                             .keystore(PKCS11));

        registerDriver(Driver.name(translate("btn.mandateCert"))
                             .file(WINDOWS, "C:\\Program Files\\I.CA SecureStore\\ICASecureStorePkcs11.dll")
                             .file(LINUX, "/usr/lib/pkcs11/libICASecureStorePkcs11.so")
                             .keystore(PKCS11));
    }
}
