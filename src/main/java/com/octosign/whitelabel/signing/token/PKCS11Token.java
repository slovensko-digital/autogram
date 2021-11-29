package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.Token;
import com.octosign.whitelabel.ui.PasswordCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public class PKCS11Token extends Token {

    public PKCS11Token(String path, PasswordCallback callback, int slotId) {
        try {
            dssToken = new Pkcs11SignatureToken(path, callback, slotId);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
    }
}
