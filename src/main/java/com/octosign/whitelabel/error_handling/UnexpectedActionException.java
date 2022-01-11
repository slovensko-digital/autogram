package com.octosign.whitelabel.error_handling;

public class UnexpectedActionException extends SignerException {
    private final Code code;

    public UnexpectedActionException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public UnexpectedActionException(Code code) {
        this(code, null);
    }

    public Code getCode() {
        return code;
    }
}
