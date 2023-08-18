package digital.slovensko.autogram.core.errors;

public class XMLException extends Exception {

    private final String description;

    public XMLException(String message, String description) {
        super(message);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
