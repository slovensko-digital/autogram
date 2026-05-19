package digital.slovensko.autogram.core.errors;

public class OriginalDocumentNotFoundException extends AutogramException {

    public OriginalDocumentNotFoundException(Error error) {
        super(error.toErrorCode());
    }

    public enum Error {
        FILE_NOT_FOUND, NO_SIGNATURE, NO_DOCUMENTS, NO_SIGNED_DOCUMENTS;

        private String toErrorCode() {
            return "OriginalDocumentNotFoundException." + this.name();
        }
    }
}
