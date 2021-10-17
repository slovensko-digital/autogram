package com.octosign.whitelabel.communication;

public class CommunicationError {
    public enum Code {
        NOT_READY,
        UNSUPPORTED_OPERATION,
        MALFORMED_INPUT,
        UNSUPPORTED_FORMAT,
        SIGNING_FAILED,
        UNEXPECTED_ORIGIN,
        MALFORMED_MIMETYPE,
        UNEXPECTED_ERROR
    }

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
