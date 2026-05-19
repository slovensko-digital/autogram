package digital.slovensko.autogram.core.errors;

public class TransformationException extends AutogramException {
    public TransformationException(Error error, Object... i18nArgs) {
        super(error.toErrorCode(), i18nArgs);
    }

    public TransformationException(Error error, Throwable e) {
        super(error.toErrorCode(), e);
    }

    public enum Error {
        FAILED, XSLT_FAILED, MISSING_SLASH;

        private String toErrorCode() {
            return "TransformationException." + this.name();
        }
    }
}
