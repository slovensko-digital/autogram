package com.octosign.whitelabel.error_handling;

public enum Code {
        DEFAULT,
        NOT_READY,
        UNSUPPORTED_OPERATION,
        ATTRIBUTE_MISSING,
        MALFORMED_INPUT,
        UNSUPPORTED_FORMAT,

        XSD_SCHEMA_INVALID,
        XSLT_ERROR,
        VALIDATION_FAILED,

        HTTP_EXCHANGE_FAILED,
        FXML_LOADER_ERROR,
        SIGNING_FAILED,
        RESPONSE_FAILED,
        NULL_RESPONSE,
        DECODING_FAILED,
        UNEXPECTED_ORIGIN,
        UNEXPECTED_ERROR
    }