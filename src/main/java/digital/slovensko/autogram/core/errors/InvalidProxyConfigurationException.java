package digital.slovensko.autogram.core.errors;

public class InvalidProxyConfigurationException extends IllegalArgumentException {
    public InvalidProxyConfigurationException() {
        super("Proxy must be http://host:port (port 1–65535), without credentials, path, query or fragment.");
    }
}
