package digital.slovensko.autogram.core.errors;

public class BatchExpiredException extends AutogramException {
    public BatchExpiredException(String message) {
        super("Hromadné podpisovanie bolo ukončené", "", message);
    }
}
