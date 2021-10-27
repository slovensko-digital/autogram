package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.CommunicationError;

public class SignerException extends Exception {
    private CommunicationError.Code code;

    public SignerException(String message) {
        super(message);
    }

    public SignerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignerException(String message, CommunicationError.Code code) {
        super(message);
        this.code = code;
    }

    public SignerException(String message, CommunicationError.Code code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public CommunicationError.Code getCode() {
        return code;
    }
}
