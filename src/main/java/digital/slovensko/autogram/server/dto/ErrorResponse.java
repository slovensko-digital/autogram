package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.EFormException;
import digital.slovensko.autogram.core.errors.FunctionCanceledException;
import digital.slovensko.autogram.core.errors.InitializationFailedException;
import digital.slovensko.autogram.core.errors.MultipleOriginalDocumentsFoundException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import digital.slovensko.autogram.core.errors.ServiceUnavailableException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.server.errors.EmptyBodyException;
import digital.slovensko.autogram.server.errors.InvalidUrlParamException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.server.errors.UnsupportedSignatureLevelException;
import digital.slovensko.autogram.ui.SupportedLanguage;

public class ErrorResponse {
    private final int statusCode;
    private final ErrorResponseBody body;

    private ErrorResponse(int statusCode, ErrorResponseBody body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    private ErrorResponse(int statusCode, String code, AutogramException e) {
        this(statusCode, buildResponseWithTranslations(code, e));
    }
    private static ErrorResponseBody buildResponseWithTranslations(String code, AutogramException e) {
        // FIXME dependency direction from server to ui. Consider passing resources as parameter.
        var resources = SupportedLanguage.loadResources(UserSettings.load());
        return new ErrorResponseBody(code, e.getSubheading(resources), e.getDescription(resources));
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
        return switch (e) {
            case SigningCanceledByUserException ex -> new ErrorResponse(204, "USER_CANCELLED", ex);
            case PasswordNotProvidedException ex -> new ErrorResponse(204, "PASSWORD_NOT_PROVIDED", ex);
            case ServiceUnavailableException ex -> new ErrorResponse(503, "SERVICE_UNAVAILABLE", ex);
            case UnrecognizedException ex -> new ErrorResponse(502, "UNRECOGNIZED_DSS_ERROR", ex);
            case InitializationFailedException ex -> new ErrorResponse(500, "INITIALIZATION_FAILED", ex);
            case PINIncorrectException ex -> new ErrorResponse(500, "PIN_INCORRECT", ex);
            case FunctionCanceledException ex -> new ErrorResponse(500, "FUNCTION_CANCELED", ex);
            case UnsupportedSignatureLevelException ex -> new ErrorResponse(422, "UNSUPPORTED_SIGNATURE_LEVEL", ex);
            case RequestValidationException _,
                 XMLValidationException _,
                 SigningParametersException _,
                 EFormException _,
                 TransformationException _,
                 TransformationParsingErrorException _ ->
                    new ErrorResponse(422, "UNPROCESSABLE_INPUT", (AutogramException) e);
            case MultipleOriginalDocumentsFoundException ex ->
                    new ErrorResponse(422, "MULTIPLE_ORIGINAL_DOCUMENTS", ex);
            case OriginalDocumentNotFoundException ex -> new ErrorResponse(422, "ORIGINAL_DOCUMENT_NOT_FOUND", ex);
            case MalformedBodyException ex -> new ErrorResponse(400, "MALFORMED_INPUT", ex);
            case UnknownEformException ex -> new ErrorResponse(400, "UNKNOWN_EFORM", ex);
            case BatchCanceledException ex -> new ErrorResponse(502, "BATCH_CANCELED", ex);
            case EmptyBodyException ex -> new ErrorResponse(400, "EMPTY_BODY", ex);
            case BatchEndedException ex -> new ErrorResponse(400, "BATCH_ENDED", ex);
            case BatchExpiredException ex -> new ErrorResponse(400, "BATCH_EXPIRED", ex);
            case BatchNotStartedException ex -> new ErrorResponse(400, "BATCH_NOT_STARTED", ex);
            case BatchInvalidIdException ex -> new ErrorResponse(404, "BATCH_NOT_FOUND", ex);
            case NoDriversDetectedException ex -> new ErrorResponse(404, "DRIVER_NOT_FOUND", ex);
            case BatchConflictException ex -> new ErrorResponse(400, "BATCH_CONFLICT", ex);
            case InvalidUrlParamException ex -> new ErrorResponse(400, "INVALID_URL_PARAM", ex);
            case CertificatesReadingConsentRejectedException ex ->
                    new ErrorResponse(403, "CERTIFICATES_READING_CONSENT_REJECTED", ex);
            case AutogramException ex -> new ErrorResponse(502, "SIGNING_FAILED", ex);
            default ->
                    new ErrorResponse(500, "INTERNAL_ERROR", "Unexpected exception signing document", e.getMessage());
        };
    }
}
