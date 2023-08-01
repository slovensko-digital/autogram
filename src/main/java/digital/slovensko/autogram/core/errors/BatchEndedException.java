package digital.slovensko.autogram.core.errors;

public class BatchEndedException extends AutogramException {
    public BatchEndedException(String message) {
        super("Hromadné podpisovanie bolo ukončené", "", message);
    }
}
