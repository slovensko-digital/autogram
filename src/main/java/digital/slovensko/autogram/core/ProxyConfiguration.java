package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.InvalidProxyConfigurationException;

import java.net.URI;
import java.net.URISyntaxException;

/** Immutable startup routing policy. */
public record ProxyConfiguration(String host, int port) {
    public static final ProxyConfiguration NONE = new ProxyConfiguration(null, -1);

    public boolean isConfigured() {
        return host != null;
    }

    public static ProxyConfiguration parse(String value) {
        if (value == null || value.isEmpty())
            return NONE;

        try {
            var uri = new URI(value);
            if (!"http".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || !(uri.getRawPath().isEmpty() || uri.getRawPath().equals("/"))
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || uri.getPort() < 1 || uri.getPort() > 65535
                    || !uri.getRawAuthority().matches("(?:\\[[^]]+]|[^:]+):[0-9]+"))
                throw new IllegalArgumentException();

            var host = uri.getHost();
            if (host.startsWith("["))
                host = host.substring(1, host.length() - 1);
            return new ProxyConfiguration(host, uri.getPort());
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new InvalidProxyConfigurationException();
        }
    }

    // The settings dialog saves only valid values, an invalid one must have been edited outside of Autogram.
    public static ProxyConfiguration fromSaved(String value) {
        try {
            return parse(value);
        } catch (InvalidProxyConfigurationException e) {
            return NONE;
        }
    }
}
