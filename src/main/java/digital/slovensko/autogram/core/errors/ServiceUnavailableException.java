package digital.slovensko.autogram.core.errors;

public class ServiceUnavailableException extends AutogramException {
    public ServiceUnavailableException(String url) {
        super(new Object[]{url});
    }

    public ServiceUnavailableException(String url, Throwable e) {
        super(e, url);
    }
}
