package com.octosign.whitelabel.error_handling;

public class MalformedMimetypeException extends Exception {

    public MalformedMimetypeException(String message) {
        super(message);
    }

    public MalformedMimetypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
