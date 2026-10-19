package digital.slovensko.autogram.core.errors;

public class PasswordNotProvidedException extends AutogramException {
    public PasswordNotProvidedException() {
        super();
    }

    @Override
    public boolean shouldReturnToSigning() {
        return true;
    }

    /** In bulk signing, where the user can't return to a document, closing the PIN dialog cancels the batch. */
    @Override
    public boolean batchCanContinue() {
        return false;
    }
}
