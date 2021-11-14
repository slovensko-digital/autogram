package com.octosign.whitelabel.error_handling;

import static com.octosign.whitelabel.error_handling.Code.Category.*;
import static java.net.HttpURLConnection.*;

public enum Code {
    UNEXPECTED_ERROR(GENERAL),
    TOKEN_INIT_FAILED(GENERAL),
    SIGNING_FAILED(GENERAL),

    NOT_READY(INPUT),
    UNSUPPORTED_OPERATION(INPUT),
    MISSING_INPUT(INPUT),
    MALFORMED_INPUT(INPUT),
    INVALID_CONTENT(INPUT),
    UNSUPPORTED_FORMAT(INPUT),
    MALFORMED_MIMETYPE(INPUT),
    INVALID_SCHEMA(INPUT),
    TRANSFORMATION_ERROR(INPUT),
    DECODING_FAILED(INPUT),

    UNEXPECTED_ORIGIN(SECURITY);

    private final Category category;

    Code(Category category) {
        this.category = category;
    }

    public int toHttpCode() {
        return switch(this.category) {
            case GENERAL -> HTTP_INTERNAL_ERROR;
            case INPUT -> HTTP_BAD_REQUEST;
            case SECURITY -> HTTP_FORBIDDEN;
        };
    }

    public enum Category {
        GENERAL,
        INPUT,
        SECURITY;
    }
}
