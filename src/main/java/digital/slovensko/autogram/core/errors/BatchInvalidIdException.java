package digital.slovensko.autogram.core.errors;

public class BatchInvalidIdException extends AutogramException {
    public BatchInvalidIdException(String message) {
        super("Batch session ID does not match", "", message);
    }
}
