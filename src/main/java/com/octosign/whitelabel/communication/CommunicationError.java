package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.error_handling.Code;

public class CommunicationError {
    public final Code code;
    public final String message;
    public String details;

    public CommunicationError(Code code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommunicationError(Code code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}
