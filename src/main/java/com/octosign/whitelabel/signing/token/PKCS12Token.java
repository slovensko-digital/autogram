package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.Token;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import java.security.KeyStore;

public class PKCS12Token extends Token {

    public PKCS12Token(String filepath, String password) {
        try {
            var passwordProtection = new KeyStore.PasswordProtection(password.toCharArray());
            dssToken = new Pkcs12SignatureToken(filepath, passwordProtection);
        } catch (Exception e) {
            throw new UserException("error.tokenAccessDenied.header", "error.tokenAccessDenied.description", e);
        }
    }
}
