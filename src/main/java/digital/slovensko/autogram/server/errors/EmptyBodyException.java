package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class EmptyBodyException extends AutogramException {
    public EmptyBodyException(String message) {
        super("Empty body", "JsonSyntaxException parsing request body.", message);
    }
}
