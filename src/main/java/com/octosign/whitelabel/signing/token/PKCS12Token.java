package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.Token;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import java.security.KeyStore;

public class PKCS12Token extends Token {

    public PKCS12Token(String path, PasswordInputCallback passwordInputCallback) {
        // Find out if password is required with empty password
        // This way user isn't prompted for password if not required
        try {
            var passwordProtection = new KeyStore.PasswordProtection("".toCharArray());
            var token = new Pkcs12SignatureToken(path, passwordProtection);
            initialize(token);
        } catch (Exception e) {
            var cause = e.getCause();
            if (cause != null && cause.getMessage() != null && cause.getMessage().contains("password was incorrect")) {
                createToken(path, passwordInputCallback.getPassword());
            } else {
                throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
            }
        }
    }

    public PKCS12Token(String path, String password) {
        createToken(path, password.toCharArray());
    }

    private void createToken(String path, char[] password) {
        Pkcs12SignatureToken token;

        try {
            var passwordProtection = new KeyStore.PasswordProtection(password);
            token = new Pkcs12SignatureToken(path, passwordProtection);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }

        initialize(token);
    }
}
