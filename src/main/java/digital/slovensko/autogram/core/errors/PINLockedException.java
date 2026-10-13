package digital.slovensko.autogram.core.errors;

public class PINLockedException extends AutogramException {
    public PINLockedException() {
        super();
    }

    /** Nothing more can be signed with the card, the rest of the batch would only fail on it again. */
    @Override
    public boolean batchCanContinue() {
        return false;
    }
}
