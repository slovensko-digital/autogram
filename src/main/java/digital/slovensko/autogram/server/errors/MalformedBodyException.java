package digital.slovensko.autogram.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class MalformedBodyException extends AutogramException {
    public MalformedBodyException(String message, Exception e) {
        super("Malformed request body", "JsonSyntaxException parsing request body.", message, e); // TODO make this nice
    }

    public MalformedBodyException(String message, String description) {
        super("Malformed request body", message, description);
    }
}
