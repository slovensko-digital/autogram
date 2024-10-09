package digital.slovensko.autogram.core.errors;

public class InvalidPasswordException extends AutogramException {
    public InvalidPasswordException(String message) {
        super("Nesprávne heslo", "Zadali ste nesprávne heslo", message);
    }
}
