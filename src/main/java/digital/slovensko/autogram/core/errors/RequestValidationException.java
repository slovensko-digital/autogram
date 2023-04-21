package digital.slovensko.autogram.core.errors;

public class RequestValidationException extends AutogramException {
    public RequestValidationException(String message, String description) {
        super("Request validation failed", message, description);
    }
}
