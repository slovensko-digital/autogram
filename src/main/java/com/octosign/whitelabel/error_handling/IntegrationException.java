package com.octosign.whitelabel.error_handling;

public class IntegrationException extends SignerException {

    private boolean display = true;
    private final Code code;

    public IntegrationException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public IntegrationException(Code code) {
        this(code, null, null);
    }

    public IntegrationException(Code code, String message) {
        this(code, message, null);
    }

    public IntegrationException(Code code, Throwable cause) {
        this(code, null, cause);
    }

    public IntegrationException(String message) {
        this(null, message, null);
    }

    public Code getCode() {
        return code;
    }

    public IntegrationException noDisplay() {
        this.display = false;
        return this;
    }

    public boolean shouldDisplay() {
        return display;
    }
}
