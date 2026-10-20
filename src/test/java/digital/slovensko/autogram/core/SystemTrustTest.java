package digital.slovensko.autogram.core;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemTrustTest {
    @TempDir Path directory;

    @Test
    void loadsSystemCertificatesOnThisOperatingSystem() {
        assertFalse(SystemTrust.systemCertificates().isEmpty());
    }

    @Test
    void dssAndJavaClientsTrustSystemCertificatesBesidesTheJdkOnes() throws Exception {
        try (var fixture = new LocalProxyFixture(directory)) {
            var url = fixture.httpsUrl("/resource");
            assertThrows(DSSExternalResourceException.class, () -> new CommonsDataLoader().get(url));

            var trustManager = SystemTrust.trustManager(List.of(fixture.certificate));
            var jdk = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            jdk.init((KeyStore) null);
            var jdkCertificates = ((X509TrustManager) jdk.getTrustManagers()[0]).getAcceptedIssuers();
            assertEquals(jdkCertificates.length + 1, trustManager.getAcceptedIssuers().length);

            var loader = new CommonsDataLoader();
            loader.setTrustStrategy(SystemTrust.trustStrategy(trustManager));
            assertEquals("fixture-resource", new String(loader.get(url)));
            try (var client = HttpClient.newBuilder().sslContext(SystemTrust.sslContext(trustManager)).build()) {
                assertEquals("fixture-resource", client.send(HttpRequest.newBuilder(URI.create(url)).build(),
                        HttpResponse.BodyHandlers.ofString()).body());
            }
        }
    }
}
