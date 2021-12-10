package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.Token;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;


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
}
