package digital.slovensko.autogram.core.errors;

public class ResponseNetworkErrorException extends AutogramException {
    public ResponseNetworkErrorException(Exception e) {
        super(e);
    }
}
