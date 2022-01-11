package com.octosign.whitelabel.error_handling;

import static com.octosign.whitelabel.error_handling.Code.Category.*;
import static java.net.HttpURLConnection.*;

public enum Code {
    UNEXPECTED_ERROR(GENERAL),
    TOKEN_INIT_FAILED(GENERAL),
    SIGNING_FAILED(GENERAL),

    UNSUPPORTED_OPERATION(UNKNOWN_ACTION),

    NOT_READY(CONFLICT),

    MISSING_INPUT(INPUT),
    MALFORMED_INPUT(INPUT),
    INVALID_CONTENT(INPUT),
    UNSUPPORTED_FORMAT(INPUT),
    MALFORMED_MIMETYPE(INPUT),
    INVALID_SCHEMA(INPUT),
    TRANSFORMATION_ERROR(INPUT),
    DECODING_FAILED(INPUT),

    UNEXPECTED_ORIGIN(SECURITY),

    CANCELLED_BY_USER(USER_ACTION);

    public static final int HTTP_CLIENT_CLOSED_REQUEST = 499;

    private final Category category;

    Code(Category category) {
        this.category = category;
    }

    public int toHttpCode() {
        return switch(this.category) {
            case GENERAL -> HTTP_INTERNAL_ERROR;
            case INPUT -> HTTP_BAD_REQUEST;
            case UNKNOWN_ACTION -> HTTP_BAD_METHOD;
            case CONFLICT -> HTTP_CONFLICT;
            case SECURITY -> HTTP_FORBIDDEN;
            case USER_ACTION -> HTTP_CLIENT_CLOSED_REQUEST;
        };
    }

    public enum Category {
        GENERAL,
        INPUT,
        UNKNOWN_ACTION,
        CONFLICT,
        SECURITY,
        USER_ACTION;
    }
}
