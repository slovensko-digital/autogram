package digital.slovensko.autogram.core.errors;

public class TransformationParsingErrorException extends AutogramException {
    public TransformationParsingErrorException(Error error, Object... i18nArgs) {
        super(error.toErrorCode(), i18nArgs);
    }

    public enum Error {
        MISSING_OUTPUT_ELEMENT, MISSING_OUTPUT_METHOD_ATTRIBUTE, UNSUPPORTED_OUTPUT_METHOD, FAILED_PARSING;

        private String toErrorCode() {
            return "TransformationParsingErrorException." + this.name();
        }
    }
}
