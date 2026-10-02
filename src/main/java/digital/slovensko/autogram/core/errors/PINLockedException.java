package digital.slovensko.autogram.core.errors;

public class PINLockedException extends AutogramException {
    public PINLockedException() {
        super();
    }

    @Override
    public boolean batchCanContinue() {
        return false;
    }
}
