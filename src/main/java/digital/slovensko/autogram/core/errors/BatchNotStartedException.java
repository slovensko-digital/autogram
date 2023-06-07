package digital.slovensko.autogram.core.errors;

public class BatchNotStartedException extends AutogramException {
    public BatchNotStartedException(String message) {
        super("Batch is not started", "Batch is not started.", message);
    }
}
