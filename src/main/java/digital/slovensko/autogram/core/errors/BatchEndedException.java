package digital.slovensko.autogram.core.errors;

public class BatchEndedException extends AutogramException {
    public BatchEndedException(Error error) {
        super(error.toErrorCode());
    }

    public enum Error {
        CANNOT_RESTART, NOT_STARTED, ALREADY_ENDED;

        private String toErrorCode() {
            return "BatchEndedException." + this.name();
        }
    }
}
