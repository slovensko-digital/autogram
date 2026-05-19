package digital.slovensko.autogram.core.errors;

public class PINIncorrectException extends AutogramException {
    public PINIncorrectException() {
        super();
    }

    @Override
    public boolean batchCanContinue() {
        return false;
    }
}
