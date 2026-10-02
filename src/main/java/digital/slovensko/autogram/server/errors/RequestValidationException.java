package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class RequestValidationException extends AutogramException {

    public RequestValidationException(Error error, Object... i18nArgs) {
        super(error.toErrorCode(), i18nArgs);
    }

    public enum Error {
        EMPTY_PARAMS_LEVEL, UNSUPPORTED_SIGN_LEVEL, MISSING_PARAMS, MISSING_FIELD, MIME_TYPE_MISMATCH,
        CONTAINER_UNSUPPORTED, CONTAINER_MISMATCH, MULTI_DOCUMENT_BATCH_UNSUPPORTED,
        MULTI_DOCUMENT_CONTAINER_UNSUPPORTED, MULTI_DOCUMENT_FORMAT_UNSUPPORTED, MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED,
        UNSUPPORTED_SIGNATURE_FORMAT, SIGNED_DOCUMENT_FORM_MISMATCH, SIGNED_DOCUMENT_CONTAINER_MISMATCH,
        SIGNED_DOCUMENT_PACKAGING_MISMATCH;

        private String toErrorCode() {
            return "RequestValidationException." + this.name();
        }
    }
}
