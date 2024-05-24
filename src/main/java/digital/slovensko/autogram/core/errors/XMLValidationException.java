package digital.slovensko.autogram.core.errors;

public class XMLValidationException extends AutogramException {
    public XMLValidationException(String message, String description) {
        super("Chyba XML dát", message, description);
    }

    public XMLValidationException(String message, String description, Throwable e) {
        super("Chyba XML dát", message, description, e);
    }
}
