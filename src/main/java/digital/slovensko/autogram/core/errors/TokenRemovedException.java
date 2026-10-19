package digital.slovensko.autogram.core.errors;

public class TokenRemovedException extends AutogramException {
    public TokenRemovedException() {
        super();
    }

    /** The connection can't sign anymore, the rest of a bulk batch would fail on it too. */
    @Override
    public boolean batchCanContinue() {
        return false;
    }
}
