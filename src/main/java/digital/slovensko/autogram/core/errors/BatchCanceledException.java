package digital.slovensko.autogram.core.errors;

public class BatchCanceledException extends AutogramException {
    public BatchCanceledException() {
        super("Batch canceled", "", "Batch canceled");
    }
}
