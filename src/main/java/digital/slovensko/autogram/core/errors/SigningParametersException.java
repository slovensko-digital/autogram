package digital.slovensko.autogram.core.errors;

public class SigningParametersException extends AutogramException {
    public SigningParametersException(String message, String description) {
        super("Neplatné parametre", message, description);
    }

    public SigningParametersException(String message, String description, Throwable cause) {
        super("Neplatné parametre", message, description, cause);
    }
}
