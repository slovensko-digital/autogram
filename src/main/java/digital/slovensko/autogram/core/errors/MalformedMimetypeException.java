package digital.slovensko.autogram.core.errors;

public class MalformedMimetypeException extends Exception {

    public MalformedMimetypeException(String message) {
        super(message);
    }

    public MalformedMimetypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
