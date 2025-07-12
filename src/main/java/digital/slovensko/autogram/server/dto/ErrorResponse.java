package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ErrorResponse {
    private final int statusCode;
    private final ErrorResponseBody body;

    private ErrorResponse(int statusCode, ErrorResponseBody body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    private ErrorResponse(int statusCode, String code, AutogramException e) {
        this(statusCode, new ErrorResponseBody(code, e.getSubheading(), e.getDescription()));
    }

    private ErrorResponse(int statusCode, String code, String message, String details) {
        this(statusCode, new ErrorResponseBody(code, message, details));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ErrorResponseBody getBody() {
        return body;
    }

    public static ErrorResponse buildFromException(Exception e) {
        // TODO maybe replace with pattern matching someday
        return switch (e.getClass().getSimpleName()) {
            case "SigningCanceledByUserException" -> new ErrorResponse(204, "USER_CANCELLED", (AutogramException) e);
            case "PasswordNotProvidedException" -> new ErrorResponse(204, "PASSWORD_NOT_PROVIDED", (AutogramException) e);
            case "ServiceUnavailableException" -> new ErrorResponse(503, "SERVICE_UNAVAILABLE", (AutogramException) e);
            case "UnrecognizedException" -> new ErrorResponse(502, "UNRECOGNIZED_DSS_ERROR", (AutogramException) e);
            case "InitializationFailedException" -> new ErrorResponse(500, "INITIALIZATION_FAILED", (AutogramException) e);
            case "PINIncorrectException" -> new ErrorResponse(500, "PIN_INCORRECT", (AutogramException) e);
            case "FunctionCanceledException" -> new ErrorResponse(500, "FUNCTION_CANCELED", (AutogramException) e);
            case "UnsupportedSignatureLevelException" -> new ErrorResponse(422, "UNSUPPORTED_SIGNATURE_LEVEL", (AutogramException) e);
            case "RequestValidationException",
                "XMLValidationException",
                "SigningParametersException",
                "EFormException",
                "TransformationException",
                "TransformationParsingErrorException" -> new ErrorResponse(422, "UNPROCESSABLE_INPUT", (AutogramException) e);
            case "MultipleOriginalDocumentsFoundException" -> new ErrorResponse(422, "MULTIPLE_ORIGINAL_DOCUMENTS", (AutogramException) e);
            case "OriginalDocumentNotFoundException" -> new ErrorResponse(422, "ORIGINAL_DOCUMENT_NOT_FOUND", (AutogramException) e);
            case "MalformedBodyException" -> new ErrorResponse(400, "MALFORMED_INPUT", (AutogramException) e);
            case "UnknownEformException" -> new ErrorResponse(400, "UNKNOWN_EFORM", (AutogramException) e);
            case "AutogramException" -> new ErrorResponse(502, "SIGNING_FAILED", (AutogramException) e);
            case "BatchCanceledException" -> new ErrorResponse(502, "BATCH_CANCELED", (AutogramException) e);
            case "EmptyBodyException" -> new ErrorResponse(400, "EMPTY_BODY", (AutogramException) e);
            case "BatchEndedException" -> new ErrorResponse(400, "BATCH_ENDED", (AutogramException) e);
            case "BatchExpiredException" -> new ErrorResponse(400, "BATCH_EXPIRED", (AutogramException) e);
            case "BatchNotStartedException" -> new ErrorResponse(400, "BATCH_NOT_STARTED", (AutogramException) e);
            case "BatchInvalidIdException" -> new ErrorResponse(404, "BATCH_NOT_FOUND", (AutogramException) e);
            case "NoDriversDetectedException" -> new ErrorResponse(404, "DRIVER_NOT_FOUND", (AutogramException) e);
            case "BatchConflictException" -> new ErrorResponse(400, "BATCH_CONFLICT", (AutogramException) e);
            case "InvalidUrlParamException" -> new ErrorResponse(400, "INVALID_URL_PARAM", (AutogramException) e);
            case "CertificatesReadingConsentRejectedException" -> new ErrorResponse(403, "CERTIFICATES_READING_CONSENT_REJECTED", (AutogramException) e);
            default -> new ErrorResponse(500, "INTERNAL_ERROR", "Unexpected exception signing document", e.getMessage());
        };
    }
}
