package digital.slovensko.autogram.core.errors;

public class MultipleOriginalDocumentsFoundException extends RuntimeException {

    public MultipleOriginalDocumentsFoundException(String description) {
        super("Multiple original documents found");
        this.description = description;
    }

    private String description;

    public String getDescription() {
        return description;
    }
}
