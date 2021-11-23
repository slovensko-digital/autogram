package com.octosign.whitelabel.error_handling;

public class SignerException extends RuntimeException {

    public SignerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignerException() {
        this(null, null);
    }

    public SignerException(String message) {
        this(message, null);
    }

    public SignerException(Throwable cause) {
        this(null, cause);
    }
}
