package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class InvalidUrlParamException extends AutogramException {

    public InvalidUrlParamException(Error error) {
        super(error.toErrorCode());
    }

    public enum Error {
        MISSING_ASSET_NAME, ASSET_NOT_FOUND;

        private String toErrorCode() {
            return "InvalidUrlParamException." + this.name();
        }
    }
}
