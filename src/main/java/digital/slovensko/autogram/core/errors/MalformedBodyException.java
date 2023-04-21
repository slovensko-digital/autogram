package digital.slovensko.autogram.core.errors;

public class MalformedBodyException extends AutogramException {
    public MalformedBodyException(String message, Exception e) {
        super("Malformed request body", "JsonSyntaxException parsing request body.", message, e); // TODO make this nice
    }
}
