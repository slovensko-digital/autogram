package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class RequestValidationException extends AutogramException {
    public RequestValidationException(String message, String description) {
        super("Request validation failed", message, description);
    }
}
