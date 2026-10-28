package digital.slovensko.autogram.core;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
    /** Far from both /slow (10 B/s) and /steady (about 1600 B/s), sleeps on CI machines can take much longer. */
    private static final long MIN_SPEED = 200;

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
    void downloadsThatDoNotStartOrAreTooSlowAreAborted() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var loader = fixture.trusting(new NetworkClients.SlowDownloadAbortingDataLoader(Duration.ofSeconds(1), MIN_SPEED));
            for (var url : List.of(fixture.httpUrl("/silent"), fixture.httpsUrl("/silent"),
                    fixture.httpUrl("/slow"), fixture.httpsUrl("/slow")))
                assertAbortedWithinSeconds(5, () -> loader.get(url), url);
            assertEquals("fixture-resource", new String(loader.get(fixture.httpsUrl("/trusted-list"))));
        }
    }

    @Test
    void slowButSteadyDownloadsFinishAfterTheGracePeriod() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var loader = fixture.trusting(new NetworkClients.SlowDownloadAbortingDataLoader(Duration.ofSeconds(1), MIN_SPEED));
            for (var url : List.of(fixture.httpUrl("/steady"), fixture.httpsUrl("/steady")))
                assertEquals(2500, loader.get(url).length, url);
        }
    }

    @Test
    void slowDownloadsAreAbortedThroughTheProxyToo() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var loader = NetworkClients.configure(
                    fixture.trusting(new NetworkClients.SlowDownloadAbortingDataLoader(Duration.ofSeconds(1), MIN_SPEED)),
                    ProxyConfiguration.parse(fixture.proxyUrl()));
            assertAbortedWithinSeconds(5, () -> loader.get(fixture.httpUrl("/slow")), "http");
            assertAbortedWithinSeconds(5, () -> loader.get(fixture.httpsUrl("/silent")), "https");
            assertEquals(2500, loader.get(fixture.httpUrl("/steady")).length);
        }
    }

    private static void assertAbortedWithinSeconds(int seconds, Executable download, String message) {
        var started = System.nanoTime();
        assertThrows(DSSExternalResourceException.class, download, message);
        assertTrue(Duration.ofNanos(System.nanoTime() - started).compareTo(Duration.ofSeconds(seconds)) < 0, message);
    }

    @Test
    void withoutProxyDssLoaderIsUntouched() {
        var loader = new CommonsDataLoader();
        assertSame(loader, NetworkClients.configure(loader, ProxyConfiguration.NONE));
    }
}
