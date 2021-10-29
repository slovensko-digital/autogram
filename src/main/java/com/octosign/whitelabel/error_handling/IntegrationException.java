package com.octosign.whitelabel.error_handling;

public class IntegrationException extends SignerException {

    private Code code;

    public IntegrationException(Code code) {
        super();
        this.code = code;
    }
    public IntegrationException(Code code, String message) {
        super(message);
        this.code = code;
    }
    public IntegrationException(Code code, Throwable cause) {
        super(cause);
        this.code = code;
    }
    public IntegrationException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code getCode() { return code; }

    public IntegrationException(String message) {
        super(message);
        this.code = null;
    }
    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }
}
