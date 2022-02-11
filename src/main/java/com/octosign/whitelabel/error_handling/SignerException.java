package com.octosign.whitelabel.error_handling;

public class SignerException extends RuntimeException {

    public SignerException(String message, Throwable cause) {
        super(message, cause);
    }

}
