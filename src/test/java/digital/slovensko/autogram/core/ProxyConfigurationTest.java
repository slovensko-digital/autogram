package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.InvalidProxyConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ProxyConfigurationTest {
    @ParameterizedTest
    @ValueSource(strings = {"http://proxy.example.com:8080", "http://127.0.0.1:1", "http://[::1]:65535",
            "http://[2001:db8::1]:3128", "HTTP://localhost:80", "http://localhost:80/"})
    void acceptsExplicitHttpEndpoints(String url) {
        assertTrue(ProxyConfiguration.parse(url).isConfigured());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost", "http://localhost:0", "http://localhost:65536",
            "http://localhost:-1", "http://localhost:+80", "http://localhost:abc", "http://localhost:999999999999",
            "https://localhost:80", "socks://localhost:80", "http://user:pass@localhost:80", "http://user@localhost:80",
            "http://localhost:80//", "http://localhost:80/path", "http://localhost:80?x", "http://localhost:80#x",
            "http://::1:80", "http://:80", "http://bad host:80", " http://localhost:80", " ", "localhost:80"})
    void rejectsInvalidInputAndIgnoresInvalidSavedSettings(String url) {
        assertThrows(InvalidProxyConfigurationException.class, () -> ProxyConfiguration.parse(url));
        assertFalse(ProxyConfiguration.fromSaved(url).isConfigured());
    }

    @Test
    void emptyDisablesAndIpv6HostIsUnbracketed() {
        assertFalse(ProxyConfiguration.parse("").isConfigured());
        assertFalse(ProxyConfiguration.parse(null).isConfigured());
        assertEquals("::1", ProxyConfiguration.parse("http://[::1]:8080").host());
    }
}
