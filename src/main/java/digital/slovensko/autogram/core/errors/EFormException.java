package digital.slovensko.autogram.core.errors;

public class EFormException extends AutogramException {
    public EFormException(String message, String description) {
        super("Problém s elektronickým formulárom", message, description);
    }

    public EFormException(String message, String description, Throwable cause) {
        super("Problém s elektronickým formulárom", message, description, cause);
    }
}
