package digital.slovensko.autogram.core.errors;

public class XMLValidationException extends AutogramException {

    public XMLValidationException(Error error, Object... i18nArgs) {
        super(error.toErrorCode(), i18nArgs);
    }

    public XMLValidationException(Error error, Throwable cause) {
        super(error.toErrorCode(), cause);
    }

    public enum Error {
        FAILED_TO_LOAD_XML, FAILED_TO_LOAD_XML_DATA, EMPTY_XMLDATA, DIGEST, ELEMENT_NOT_FOUND, ATTRIBUTES_NOT_FOUND,
        DIGEST_NOT_FOUND, XMLDATA_INVALID_TEXT, FORM_ID_TO_XSD, XSLT_OR_XSD_NOT_FOUND,
        AUTO_XSD_DIGEST_MISMATCH, AUTO_XSLT_DIGEST_MISMATCH, XSD_DIGEST_MISMATCH, XSLT_DIGEST_MISMATCH,
        FORMS_NOT_FOUND, XSD_NOT_FOUND, XSLT_NOT_FOUND, MANIFEST_NOT_FOUND, DATACONTAINER_XSD_VIOLATION, XSD_VIOLATION;

        private String toErrorCode() {
            return "XMLValidationException." + this.name();
        }
    }
}
