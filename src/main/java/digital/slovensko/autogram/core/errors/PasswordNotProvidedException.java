package digital.slovensko.autogram.core.errors;

public class PasswordNotProvidedException extends AutogramException {
    public PasswordNotProvidedException() {
        super();
    }

    @Override
    public boolean shouldReturnToSigning() {
        return true;
    }
}
