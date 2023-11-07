package digital.slovensko.autogram.core.errors;

public class TransformationException extends AutogramException {
    public TransformationException(String message, String description) {
        super("Chyba  transformácie", message, description);
    }

    public TransformationException(String message, String description, Throwable e) {
        super("Chyba transformácie", message, description, e);
    }
}
