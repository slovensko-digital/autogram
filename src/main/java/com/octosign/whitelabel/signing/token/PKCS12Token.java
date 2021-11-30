package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.Token;
import com.octosign.whitelabel.ui.PasswordCallback;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import java.security.KeyStore;

public class PKCS12Token extends Token {

    public PKCS12Token(String filepath) {
        this(filepath, null);
    }

    public PKCS12Token(String filepath, String password) {
        Pkcs12SignatureToken token;

        if (password == null) {
            var input = new PasswordCallback().getPassword();
            password = (input == null) ? "" : new String(input);
        }
        var passwordProtection = new KeyStore.PasswordProtection(password.toCharArray());

        try {
            token = new Pkcs12SignatureToken(filepath, passwordProtection);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
        initialize(token);
    }
}
