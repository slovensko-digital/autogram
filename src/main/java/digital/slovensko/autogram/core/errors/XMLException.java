package digital.slovensko.autogram.core.errors;

public class XMLException extends AutogramException {

    private final String description;

    public XMLException(String message, String description) {
        super("Chyba XML dát", message, description);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
