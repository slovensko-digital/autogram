package digital.slovensko.autogram.core.errors;

public class EFormException extends AutogramException {
    public EFormException(Error error, Object... i18nArgs) {
        super(error.toErrorCode(), i18nArgs);
    }

    public enum Error {
        XSD, XSLT, MISSING_ID, META_XML, FS_FORM_ID, MANIFEST;

        private String toErrorCode() {
            return "EFormException." + this.name();
        }
    }

}