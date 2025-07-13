package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class MalformedBodyException extends AutogramException {
    public MalformedBodyException(Error error) {
        super(error.toErrorCode());
    }

    public MalformedBodyException(Error error, Exception e) {
        super(error.toErrorCode(), e, e.getMessage());
    }

    public enum Error {
        JSON_PARSING_FAILED, INVALID_XSD, INVALID_XSLT, BASE64_DECODING_FAILED;

        private String toErrorCode() {
            return "MalformedBodyException." + this.name();
        }
    }
}
