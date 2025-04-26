package digital.slovensko.autogram.core.errors;

public class BatchCloseException extends AutogramException {
    public BatchCloseException(String heading, String subheading, String description) {
        super(heading, subheading, description);
    }
}