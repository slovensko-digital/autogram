package digital.slovensko.autogram.core.errors;

public class SigningCanceledByUserException extends AutogramException {
    public SigningCanceledByUserException() {
        super("Podpisovanie zrušené", "Používateľ zrušil podpisovanie", null); // TODO make this nice
    }
}
