package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ErrorResponse {
    private final int statusCode;
    private final ErrorResponseBody body;

    public ErrorResponse(int statusCode, ErrorResponseBody body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public ErrorResponse(int statusCode, String code, AutogramException e) {
        this(statusCode, new ErrorResponseBody(code, e.getSubheading(), e.getDescription()));
    }

    public ErrorResponse(int statusCode, String code, String message, String details) {
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
            case "UnrecognizedException" -> new ErrorResponse(502, "UNRECOGNIZED_DSS_ERROR", (AutogramException) e);
            case "UnsupportedSignatureLevelExceptionError" -> new ErrorResponse(422, "UNSUPPORTED_SIGNATURE_LEVEL", (AutogramException) e);
            case "RequestValidationException" -> new ErrorResponse(422, "UNPROCESSABLE_INPUT", (AutogramException) e);
            case "MalformedBodyException" -> new ErrorResponse(400, "MALFORMED_INPUT", (AutogramException) e);
            case "TransformationException" -> new ErrorResponse(400, "TRANSFORMATION_FAILED", (AutogramException) e);
            case "AutogramException" -> new ErrorResponse(502, "SIGNING_FAILED", (AutogramException) e);
            case "EmptyBodyException" -> new ErrorResponse(400, "EMPTY_BODY", (AutogramException) e);
            case "BatchEndedException" -> new ErrorResponse(400, "BATCH_ENDED", (AutogramException) e);
            case "BatchNotStartedException" -> new ErrorResponse(400, "BATCH_NOT_STARTED", (AutogramException) e);
            case "BatchNotFoundException" -> new ErrorResponse(404, "BATCH_NOT_FOUND", (AutogramException) e);
            case "BatchConflictException" -> new ErrorResponse(400, "BATCH_CONFLICT", (AutogramException) e);
            default -> new ErrorResponse(500, "INTERNAL_ERROR", "Unexpected exception signing document", e.getMessage());
        };
    }
}
