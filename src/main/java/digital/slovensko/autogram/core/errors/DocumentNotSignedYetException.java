package digital.slovensko.autogram.core.errors;

public class DocumentNotSignedYetException extends AutogramException {
    public DocumentNotSignedYetException() {
        super("Document not signed", "Document is not signed yet", "The provided document is not eligible for signature validation because the document is not signed yet.");
    }
}
