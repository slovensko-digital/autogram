package digital.slovensko.autogram.core.errors;

public class BatchConflictException extends AutogramException {
    public BatchConflictException(String message) {
        super("Batch ", "", message);
    }
}
