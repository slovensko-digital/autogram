package digital.slovensko.autogram.core;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import com.sun.net.httpserver.HttpServer;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.InetSocketAddress;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void validationRequestsHaveExplicitLimitsWithoutChangingTsaOrTrustedListDefaults() {
        var loader = NetworkClients.withValidationTimeouts(new CommonsDataLoader());
        assertEquals(5_000, loader.getTimeoutConnection());
        assertEquals(5_000, loader.getTimeoutConnectionRequest());
        assertEquals(10_000, loader.getTimeoutResponse());
        assertEquals(10_000, loader.getTimeoutSocket());
        assertEquals(60_000, ((CommonsDataLoader) NetworkClients.dataLoader()).getTimeoutResponse());
        assertEquals(60_000, ((CommonsDataLoader) NetworkClients.timestampDataLoader()).getTimeoutResponse());
        assertSame(NetworkClients.crlSource(), NetworkClients.crlSource());
        assertSame(NetworkClients.ocspSource(), NetworkClients.ocspSource());
    }

    @Test
    void getAndOcspPostTimeOutWithoutRetrying() throws Exception {
        var release = new CountDownLatch(1);
        var requests = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            server.setExecutor(threads);
            server.createContext("/", exchange -> {
                requests.incrementAndGet();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    exchange.close();
                }
            });
            server.start();
            var url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            try {
                for (var loader : List.of(new CommonsDataLoader(), new OCSPDataLoader())) {
                    NetworkClients.withValidationTimeouts(loader);
                    loader.setTimeoutResponse(100);
                    assertTimeoutPreemptively(Duration.ofSeconds(3), () -> assertThrows(DSSExternalResourceException.class, () -> {
                        if (loader instanceof OCSPDataLoader)
                            loader.post(url, new byte[]{1});
                        else
                            loader.get(url);
                    }));
                }
                assertEquals(2, requests.get());
            } finally {
                release.countDown();
                server.stop(0);
            }
        }
    }
}
