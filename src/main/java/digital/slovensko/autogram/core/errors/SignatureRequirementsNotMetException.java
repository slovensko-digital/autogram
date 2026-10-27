package digital.slovensko.autogram.core.errors;

public class SignatureRequirementsNotMetException extends AutogramException {
    public SignatureRequirementsNotMetException(String detail) {
        super(new IllegalStateException(detail));
    }

    public SignatureRequirementsNotMetException(Throwable cause) {
        super(cause);
    }
}
