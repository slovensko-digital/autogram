package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.signing.token.MSCAPIToken;
import com.octosign.whitelabel.signing.token.PKCS11Token;
import com.octosign.whitelabel.signing.token.PKCS12Token;
import com.octosign.whitelabel.ui.PasswordCallback;

import java.util.function.Function;

public enum KeystoreType {
    PKCS11 ((driver) -> new PKCS11Token(driver.path(), new PasswordCallback())),
    PKCS12 ((driver) -> new PKCS12Token(driver.path(), new PasswordCallback())),
    MSCAPI ((__) -> new MSCAPIToken());

    private Function<Driver, Token> instantiation;

    KeystoreType(final Function<Driver, Token> instantiation) {
        this.instantiation = instantiation;
    }

    public Token createToken(Driver driver) {
        return instantiation.apply(driver);
    }
}
