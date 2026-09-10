package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class RequestValidationException extends AutogramException {

    public RequestValidationException(Error error, Object... i18nArgs) {
        super(error.toErrorCode(), i18nArgs);
    }

    public enum Error {
        EMPTY_PARAMS_LEVEL, UNSUPPORTED_SIGN_LEVEL, MISSING_PARAMS, MISSING_FIELD, MIME_TYPE_MISMATCH,
        CONTAINER_UNSUPPORTED, INVALID_PACKAGING, TRANSFORMATION_MISSING, SCHEMA_MISSING, ID_MISSING,
        CONTAINER_MISMATCH, MULTI_DOCUMENT_BATCH_UNSUPPORTED, MULTI_DOCUMENT_CONTAINER_UNSUPPORTED,
        MULTI_DOCUMENT_FORMAT_UNSUPPORTED, MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED;

        private String toErrorCode() {
            return "RequestValidationException." + this.name();
        }
    }
}
