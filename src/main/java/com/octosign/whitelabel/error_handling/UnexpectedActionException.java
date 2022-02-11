package com.octosign.whitelabel.error_handling;

public class UnexpectedActionException extends SignerException {
    private final Code code;

    public UnexpectedActionException(Code code, String message) {
        super(message, null);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
