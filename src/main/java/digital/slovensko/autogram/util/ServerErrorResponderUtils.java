package digital.slovensko.autogram.util;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ServerErrorResponderUtils {
    public static String getErrorCodeFromException(AutogramException e) {
        return switch (e.getClass().getSimpleName()) {
            case "SigningCanceledByUserException" -> "USER_CANCELLED";
            case "UnrecognizedException" -> "UNRECOGNIZED_DSS_ERROR";
            case "UnsupportedSignatureLevelExceptionError" -> "UNSUPPORTED_SIGNATURE_LEVEL";
            case "RequestValidationException" -> "UNPROCESSABLE_INPUT";
            case "MalformedBodyException" -> "MALFORMED_INPUT";
            default -> "SIGNING_FAILED";
        };
    }

    public static int getStatusCodeFromException(AutogramException e) {
        return switch (e.getClass().getSimpleName()) {
            case "SigningCanceledByUserException" -> 204;
            case "UnrecognizedException" -> 502;
            case "UnsupportedSignatureLevelExceptionError" -> 422;
            case "RequestValidationException" -> 422;
            case "MalformedBodyException" -> 400;
            default -> 502;
        };
    }
}
