package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.server.errors.UnsupportedSignatureLevelExceptionError;

public class ErrorResponse {
    private final int statusCode;
    private final ErrorResponseBody body;

    public ErrorResponse(int statusCode, ErrorResponseBody body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    
    public int getStatusCode() {
        return statusCode;
    }

    public ErrorResponseBody getBody() {
        return body;
    }

    public static ErrorResponse buildFromException(Exception error) {
        switch (error) {
            case AutogramException e -> {
                var statusCode = getStatusCodeFromException(e);
                var code = getErrorCodeFromException(e);
                var body = new ErrorResponseBody(code, e.getSubheading(), e.getDescription());
                return new ErrorResponse(statusCode, body);
            }
            case Exception e -> {
                var statusCode = 500;
                var response = new ErrorResponseBody("INTERNAL_ERROR", "Unexpected exception signing document", e.getMessage());
                return new ErrorResponse(statusCode, response);
            }
        }
    }

    private static int getStatusCodeFromException(AutogramException e) {
        return switch (e) {
            case SigningCanceledByUserException x -> 204;
            case UnrecognizedException x -> 502;
            case UnsupportedSignatureLevelExceptionError x -> 422;
            case RequestValidationException x -> 422;
            case MalformedBodyException x -> 400;
            default -> 502;
        };
    }

    private static String getErrorCodeFromException(AutogramException e) {
        return switch (e) {
            case SigningCanceledByUserException x -> "USER_CANCELLED";
            case UnrecognizedException x -> "UNRECOGNIZED_DSS_ERROR";
            case UnsupportedSignatureLevelExceptionError x -> "UNSUPPORTED_SIGNATURE_LEVEL";
            case RequestValidationException x -> "UNPROCESSABLE_INPUT";
            case MalformedBodyException x -> "MALFORMED_INPUT";
            default -> "SIGNING_FAILED";
        };
    }
}
