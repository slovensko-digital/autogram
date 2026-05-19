package digital.slovensko.autogram.server;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.*;
import digital.slovensko.autogram.server.errors.*;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.dto.ErrorResponseBody;

import java.util.ResourceBundle;

public class ErrorResponseBuilder {
    private static ErrorResponseBuilder instance;
    private final ResourceBundle languageResources;

    private ErrorResponseBuilder(ResourceBundle languageResources) {
        this.languageResources = languageResources;
    }

    public synchronized static void init(ResourceBundle languageResources) {
        if (instance == null) {
            instance = new ErrorResponseBuilder(languageResources);
        }
    }

    private ErrorResponseBody buildResponseWithTranslations(String code, AutogramException e) {
        return new ErrorResponseBody(code, e.getSubheading(languageResources), e.getDescription(languageResources));
    }

    public synchronized static ErrorResponse buildFromException(Exception e) {
        return switch (e) {
            case SigningCanceledByUserException ex -> new ErrorResponse(204, instance.buildResponseWithTranslations("USER_CANCELLED", ex));
            case PasswordNotProvidedException ex -> new ErrorResponse(204, instance.buildResponseWithTranslations("PASSWORD_NOT_PROVIDED", ex));
            case ServiceUnavailableException ex -> new ErrorResponse(503, instance.buildResponseWithTranslations("SERVICE_UNAVAILABLE", ex));
            case UnrecognizedException ex -> new ErrorResponse(502, instance.buildResponseWithTranslations("UNRECOGNIZED_DSS_ERROR", ex));
            case InitializationFailedException ex -> new ErrorResponse(500, instance.buildResponseWithTranslations("INITIALIZATION_FAILED", ex));
            case PINIncorrectException ex -> new ErrorResponse(500, instance.buildResponseWithTranslations("PIN_INCORRECT", ex));
            case FunctionCanceledException ex -> new ErrorResponse(500, instance.buildResponseWithTranslations("FUNCTION_CANCELED", ex));
            case UnsupportedSignatureLevelException ex -> new ErrorResponse(422, instance.buildResponseWithTranslations("UNSUPPORTED_SIGNATURE_LEVEL", ex));
            case RequestValidationException _,
                 XMLValidationException _,
                 SigningParametersException _,
                 EFormException _,
                 TransformationException _,
                 TransformationParsingErrorException _ ->
                    new ErrorResponse(422, instance.buildResponseWithTranslations("UNPROCESSABLE_INPUT", (AutogramException) e));
            case MultipleOriginalDocumentsFoundException ex ->
                    new ErrorResponse(422, instance.buildResponseWithTranslations("MULTIPLE_ORIGINAL_DOCUMENTS", ex));
            case OriginalDocumentNotFoundException ex -> new ErrorResponse(422, instance.buildResponseWithTranslations("ORIGINAL_DOCUMENT_NOT_FOUND", ex));
            case MalformedBodyException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("MALFORMED_INPUT", ex));
            case UnknownEformException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("UNKNOWN_EFORM", ex));
            case BatchCanceledException ex -> new ErrorResponse(502, instance.buildResponseWithTranslations("BATCH_CANCELED", ex));
            case EmptyBodyException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("EMPTY_BODY", ex));
            case BatchEndedException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("BATCH_ENDED", ex));
            case BatchExpiredException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("BATCH_EXPIRED", ex));
            case BatchNotStartedException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("BATCH_NOT_STARTED", ex));
            case BatchInvalidIdException ex -> new ErrorResponse(404, instance.buildResponseWithTranslations("BATCH_NOT_FOUND", ex));
            case NoDriversDetectedException ex -> new ErrorResponse(404, instance.buildResponseWithTranslations("DRIVER_NOT_FOUND", ex));
            case BatchConflictException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("BATCH_CONFLICT", ex));
            case InvalidUrlParamException ex -> new ErrorResponse(400, instance.buildResponseWithTranslations("INVALID_URL_PARAM", ex));
            case CertificatesReadingConsentRejectedException ex ->
                    new ErrorResponse(403, instance.buildResponseWithTranslations("CERTIFICATES_READING_CONSENT_REJECTED", ex));
            case AutogramException ex -> new ErrorResponse(502, instance.buildResponseWithTranslations("SIGNING_FAILED", ex));
            default ->
                    new ErrorResponse(500, "INTERNAL_ERROR", "Unexpected exception signing document", e.getMessage());
        };
    }

}
