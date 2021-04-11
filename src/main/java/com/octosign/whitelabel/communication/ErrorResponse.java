package com.octosign.whitelabel.communication;

public class ErrorResponse {
    public enum Code {
        NOT_READY,
        MALFORMED_INPUT,
        UNSUPPORTED_FORMAT,
        SIGNING_FAILED
    };

    public final Code code;
    public final String message;
    public String details;

    public ErrorResponse(Code code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(Code code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}
