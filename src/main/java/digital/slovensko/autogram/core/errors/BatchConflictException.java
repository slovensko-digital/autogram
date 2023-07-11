package digital.slovensko.autogram.core.errors;

public class BatchConflictException extends AutogramException {
    public BatchConflictException(String message) {
        super("Iné hromadné podpisovanie už prebieha", "", message);
    }
}
