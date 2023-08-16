package digital.slovensko.autogram.core.errors;

public class OriginalDocumentNotFoundException extends RuntimeException {

    public OriginalDocumentNotFoundException(String description) {
        super("Original document not found");
        this.description = description;
    }

    private String description;

    public String getDescription() {
        return description;
    }
}
