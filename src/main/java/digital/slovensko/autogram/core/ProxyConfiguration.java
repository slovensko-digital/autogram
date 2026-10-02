package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.InvalidProxyConfigurationException;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/** Immutable startup routing policy. Invalid saved settings fail closed. */
public record ProxyConfiguration(State state, String host, int port) implements Serializable {
    public enum State { UNCONFIGURED, CONFIGURED, BLOCKED }

    public static ProxyConfiguration parse(String value) {
        if (value == null || value.isEmpty())
            return new ProxyConfiguration(State.UNCONFIGURED, null, -1);

        try {
            var uri = new URI(value);
            if (!"http".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || !uri.getRawPath().isEmpty()
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || uri.getPort() < 1 || uri.getPort() > 65535
                    || !uri.getRawAuthority().matches("(?:\\[[^]]+]|[^:]+):[0-9]+"))
                throw new IllegalArgumentException();

            var host = uri.getHost();
            if (host.startsWith("["))
                host = host.substring(1, host.length() - 1);
            return new ProxyConfiguration(State.CONFIGURED, host, uri.getPort());
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new InvalidProxyConfigurationException();
        }
    }

    public static ProxyConfiguration fromSaved(String value) {
        try {
            return parse(value);
        } catch (IllegalArgumentException e) {
            return new ProxyConfiguration(State.BLOCKED, null, -1);
        }
    }
}
