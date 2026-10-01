package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormResourceLoader;
import eu.europa.esig.dss.service.http.commons.*;
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
    void redirectsStayProxiedAndContentTypesArePreserved() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var configuration = ProxyConfiguration.parse(fixture.proxyUrl());
            var loader = NetworkClients.configure(new CommonsDataLoader(), configuration);
            loader.get(fixture.httpUrl("/redirect"));
            try (var client = NetworkClients.httpClientBuilder(configuration)
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build()) {
                client.send(HttpRequest.newBuilder(URI.create(fixture.httpUrl("/redirect"))).build(),
                        HttpResponse.BodyHandlers.ofString());
            }
            NetworkClients.configure(new TimestampDataLoader(), configuration)
                    .post(fixture.httpUrl("/timestamp"), new byte[]{1});
            NetworkClients.configure(new OCSPDataLoader(), configuration)
                    .post(fixture.httpUrl("/ocsp"), new byte[]{1});
            assertEquals(6, fixture.destinationRequests.get());
            assertEquals(6, fixture.requests.size());
            assertTrue(fixture.contentTypes.contains("application/timestamp-query"));
            assertTrue(fixture.contentTypes.contains("application/ocsp-request"));
        }
    }

    @Test
    void redirectFromHttpToHttpsStillUsesTheSameProxy() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var configuration = ProxyConfiguration.parse(fixture.proxyUrl());
            var loader = NetworkClients.configure(fixture.trustedLoader(), configuration);
            assertEquals("fixture-resource", new String(loader.get(fixture.httpUrl("/redirect-https"))));
            try (var client = NetworkClients.httpClientBuilder(configuration).sslContext(fixture.tls)
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build()) {
                assertEquals("fixture-resource", client.send(
                        HttpRequest.newBuilder(URI.create(fixture.httpUrl("/redirect-https"))).build(),
                        HttpResponse.BodyHandlers.ofString()).body());
            }
            assertEquals(4, fixture.destinationRequests.get());
            assertEquals(4, fixture.requests.size());
            assertEquals(2, fixture.requests.stream().filter(r -> r.startsWith("CONNECT localhost:")).count());
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
    void restrictsProtocolsAndInvalidConfigurationButAllowsLocalFiles() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            for (var configuration : List.of(ProxyConfiguration.parse(fixture.proxyUrl()),
                    ProxyConfiguration.fromSaved("invalid"))) {
                var loader = NetworkClients.configure(new CommonsDataLoader(), configuration);
                for (var scheme : List.of("ldap", "ftp")) {
                    assertThrows(DSSExternalResourceException.class,
                            () -> loader.get(fixture.forbiddenUrl(scheme)));
                    assertThrows(DSSExternalResourceException.class,
                            () -> loader.get(List.of(fixture.forbiddenUrl(scheme))));
                }
                var file = directory.resolve("local.xml");
                Files.writeString(file, "local");
                assertEquals("local", new String(loader.get(file.toUri().toString())));
            }
            var blocked = ProxyConfiguration.fromSaved("invalid");
            assertThrows(DSSExternalResourceException.class, () -> NetworkClients.configure(new CommonsDataLoader(), blocked)
                    .get(fixture.httpUrl("/resource")));
            try (var client = NetworkClients.httpClientBuilder(blocked).build()) {
                assertThrows(Exception.class, () -> client.send(
                        HttpRequest.newBuilder(URI.create(fixture.httpUrl("/resource"))).build(),
                        HttpResponse.BodyHandlers.ofString()));
            }
            assertEquals(0, fixture.destinationRequests.get());
            assertEquals(0, fixture.forbiddenConnections.get());
            assertTrue(fixture.requests.isEmpty());
        }
    }

    @Test
    void eformAndTrustedListStyleCacheRemainsUsableWhenNetworkingIsBlocked() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var cache = new FileCacheDataLoader();
            cache.setFileCacheDirectory(directory.resolve("cache").toFile());
            cache.setCacheExpirationTime(21600000);
            cache.setDataLoader(NetworkClients.configure(new CommonsDataLoader(), ProxyConfiguration.parse(fixture.proxyUrl())));
            var eforms = new EFormResourceLoader(cache);
            var url = fixture.httpUrl("/form/manifest.xml");
            assertEquals("fixture-resource", new String(eforms.getResource(url)));
            cache.setDataLoader(NetworkClients.configure(new CommonsDataLoader(), ProxyConfiguration.fromSaved("invalid")));
            assertEquals("fixture-resource", new String(eforms.getResource(url)));
            assertEquals(1, fixture.requests.size());
            assertEquals(1, fixture.destinationRequests.get());
        }
    }

    @Test
    void unconfiguredLeavesDssLoaderUntouchedAndUsesItsExistingDirectBehavior() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var original = new CommonsDataLoader();
            var loader = NetworkClients.configure(original, ProxyConfiguration.parse(null));
            assertSame(original, loader);
            assertEquals("fixture-resource", new String(loader.get(fixture.httpUrl("/resource"))));
            assertEquals(1, fixture.destinationRequests.get());
            assertTrue(fixture.requests.isEmpty());
        }
    }

    @Test
    void unconfiguredJavaClientRetainsTheExistingDefaultSelector() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var previous = java.net.ProxySelector.getDefault();
            var address = URI.create(fixture.proxyUrl());
            var selector = java.net.ProxySelector.of(new java.net.InetSocketAddress(address.getHost(), address.getPort()));
            try {
                java.net.ProxySelector.setDefault(selector);
                try (var client = NetworkClients.httpClientBuilder(ProxyConfiguration.parse(null)).build()) {
                    assertTrue(client.proxy().isEmpty()); // No newly installed explicit selector.
                    assertEquals("fixture-resource", client.send(
                            HttpRequest.newBuilder(URI.create(fixture.httpUrl("/resource"))).build(),
                            HttpResponse.BodyHandlers.ofString()).body());
                }
                assertSame(selector, java.net.ProxySelector.getDefault());
                assertEquals(1, fixture.destinationRequests.get());
                assertEquals(1, fixture.requests.size());
            } finally {
                java.net.ProxySelector.setDefault(previous);
            }
        }
    }
}
