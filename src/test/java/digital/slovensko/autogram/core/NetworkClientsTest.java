package digital.slovensko.autogram.core;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkClientsTest {
    @TempDir Path directory;

    @Test
    void httpAndHttpsAreActuallyForwardedForBothStacks() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var configuration = ProxyConfiguration.parse(fixture.proxyUrl());
            var loader = NetworkClients.configure(fixture.trustedLoader(), configuration);
            assertEquals("fixture-resource", new String(loader.get(fixture.httpUrl("/trusted-list"))));
            assertEquals("fixture-resource", new String(loader.get(fixture.httpsUrl("/aia"))));
            try (var client = NetworkClients.httpClientBuilder(configuration).sslContext(fixture.tls).build()) {
                for (var url : List.of(fixture.httpUrl("/update"), fixture.httpsUrl("/update"))) {
                    var response = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());
                    assertEquals("fixture-resource", response.body());
                }
            }
            assertEquals(4, fixture.destinationRequests.get());
            assertEquals(2, fixture.requests.stream().filter(r -> r.startsWith("CONNECT localhost:")).count());
            assertTrue(fixture.requests.contains("GET " + fixture.httpUrl("/trusted-list") + " HTTP/1.1"));
            assertTrue(fixture.requests.contains("GET " + fixture.httpUrl("/update") + " HTTP/1.1"));
        }
    }

    @Test
    void otherSchemesAreBlockedWithoutBreakingHttpAndLocalFiles() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var loader = NetworkClients.configure(new CommonsDataLoader(), ProxyConfiguration.parse(fixture.proxyUrl()));
            for (var scheme : List.of("ldap", "ftp"))
                assertThrows(DSSExternalResourceException.class, () -> loader.get(fixture.forbiddenUrl(scheme)));
            // Not a valid URI (an LDAP CRL distribution point): blocked, but the remaining URLs are still tried.
            var ldapUrl = "ldap://ldap.example.com/CN=Example CA,O=Example?certificateRevocationList";
            assertThrows(DSSExternalResourceException.class, () -> loader.get(List.of(ldapUrl)));
            var result = loader.get(List.of(ldapUrl, fixture.httpUrl("/crl-fallback")));
            assertEquals(fixture.httpUrl("/crl-fallback"), result.getUrlString());
            var file = directory.resolve("local.xml");
            Files.writeString(file, "local");
            assertEquals("local", new String(loader.get(file.toUri().toString())));
            assertEquals(1, fixture.destinationRequests.get());
            assertEquals(0, fixture.forbiddenConnections.get());
        }
    }

    @Test
    void unavailableAndAuthRequiredProxyNeverFallBackToDestination() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var configuration = ProxyConfiguration.parse(fixture.proxyUrl());
            fixture.requireAuthentication = true;
            var loader = NetworkClients.configure(new CommonsDataLoader(), configuration);
            assertThrows(DSSExternalResourceException.class, () -> loader.get(fixture.httpUrl("/crl")));
            assertThrows(DSSExternalResourceException.class, () -> loader.get(fixture.httpsUrl("/crl")));
            try (var client = NetworkClients.httpClientBuilder(configuration).connectTimeout(Duration.ofSeconds(2)).build()) {
                assertEquals(407, client.send(HttpRequest.newBuilder(URI.create(fixture.httpUrl("/update"))).build(),
                        HttpResponse.BodyHandlers.ofString()).statusCode());
                assertEquals(407, client.send(
                        HttpRequest.newBuilder(URI.create(fixture.httpsUrl("/update"))).build(),
                        HttpResponse.BodyHandlers.ofString()).statusCode());
            }
            assertEquals(0, fixture.destinationRequests.get());
            assertTrue(fixture.requests.size() >= 2);
            fixture.stopProxy();
            assertThrows(DSSExternalResourceException.class, () -> loader.get(fixture.httpUrl("/crl")));
            assertThrows(DSSExternalResourceException.class, () -> loader.get(fixture.httpsUrl("/crl")));
            try (var client = NetworkClients.httpClientBuilder(configuration).connectTimeout(Duration.ofSeconds(2)).build()) {
                assertThrows(java.io.IOException.class, () -> client.send(
                        HttpRequest.newBuilder(URI.create(fixture.httpUrl("/update"))).build(),
                        HttpResponse.BodyHandlers.ofString()));
                assertThrows(java.io.IOException.class, () -> client.send(
                        HttpRequest.newBuilder(URI.create(fixture.httpsUrl("/update"))).build(),
                        HttpResponse.BodyHandlers.ofString()));
            }
            assertEquals(0, fixture.destinationRequests.get());
        }
    }

    @Test
    void withoutProxyDssLoaderIsUntouched() {
        var loader = new CommonsDataLoader();
        assertSame(loader, NetworkClients.configure(loader, ProxyConfiguration.NONE));
    }
}
