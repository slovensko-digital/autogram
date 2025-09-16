package digital.slovensko.autogram.core.errors;

public class NoKeysDetectedException extends AutogramException {
    public NoKeysDetectedException(String helperText) {
        super(new Object[]{helperText});
    }
}
