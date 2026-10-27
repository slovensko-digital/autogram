package digital.slovensko.autogram.core.errors;

public class TsaServerMisconfiguredException extends AutogramException {
    public TsaServerMisconfiguredException(Error error, Throwable cause) {
        super(error.toErrorCode(), cause);
    }

    public enum Error {
        REFUSED, MISSING_HOST_NAME;

        private String toErrorCode() {
            return "TsaServerMisconfiguredException." + this.name();
        }
    }
}
