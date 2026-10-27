package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormResourceLoader;
import digital.slovensko.autogram.ui.cli.CliApp;
import digital.slovensko.autogram.ui.cli.CliSettings;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/** Each scenario gets a fresh process with in-memory Preferences: no real user settings are changed. */
public class StartupProxyTest {
    @TempDir Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"desktop", "cli", "dss-sources"})
    void startupScenarios(String scenario) throws Exception {
        var log = directory.resolve("process.log");
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Djava.util.prefs.PreferencesFactory=" + InMemoryPreferencesFactory.class.getName(),
                "-Djava.io.tmpdir=" + directory,
                "-cp", System.getProperty("java.class.path"), StartupProxyTest.class.getName(), scenario,
                directory.toString()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(40, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Startup fixture timed out: " + java.nio.file.Files.readString(log));
        }
        assertEquals(0, process.exitValue(), () -> {
            try { return java.nio.file.Files.readString(log); }
            catch (Exception e) { return e.toString(); }
        });
    }

    static void main(String[] args) throws Exception {
        // Never touch the real user settings, fail before anything is saved.
        assertEquals(InMemoryPreferencesFactory.class.getName() + "$InMemoryPreferences",
                Preferences.userRoot().getClass().getName());
        switch (args[0]) {
            case "dss-sources" -> {
                try (var fixture = new LocalProxyFixture(Path.of(args[1]))) {
                    NetworkClients.initialize(ProxyConfiguration.parse(fixture.proxyUrl()));
                    var token = new eu.europa.esig.dss.model.x509.CertificateToken(fixture.certificate);
                    var verifier = NetworkClients.certificateVerifier();
                    assertEquals(1, verifier.getAIASource().getCertificatesByAIA(token).size());
                    // Intentionally malformed responses: routing must not imply successful validation.
                    var crl = NetworkClients.crlSource();
                    assertThrows(DSSExternalResourceException.class, () -> crl.getRevocationToken(token, token));
                    var ocsp = NetworkClients.ocspSource();
                    assertThrows(DSSExternalResourceException.class, () -> ocsp.getRevocationToken(token, token));
                    var settings = UserSettings.load();
                    settings.setTsaServer(fixture.httpUrl("/timestamp"));
                    assertThrows(eu.europa.esig.dss.model.DSSException.class, () -> settings.getTspSource()
                            .getTimeStampResponse(eu.europa.esig.dss.enumerations.DigestAlgorithm.SHA256, new byte[32]));
                    assertEquals("fixture-resource", new String(new EFormResourceLoader()
                            .getResource(fixture.httpUrl("/form/manifest.xml"))));
                    var cache = NetworkClients.fileCacheDataLoader(21600000);
                    assertEquals("fixture-resource", new String(cache.get(fixture.httpUrl("/trusted-list.xml"))));
                    assertEquals("fixture-resource", new String(cache.get(fixture.httpUrl("/trusted-list.xml"))));
                    var documentValidator = new RecordingDocumentValidator();
                    NetworkClients.configureDocumentValidator(documentValidator);
                    assertNotNull(documentValidator.policyProvider);
                    try (var policyStream = documentValidator.policyProvider
                            .getSignaturePolicyByUrl(fixture.httpUrl("/policy")).openStream()) {
                        assertEquals("fixture-resource", new String(policyStream.readAllBytes()));
                    }
                    var job = new eu.europa.esig.dss.tsl.job.TLValidationJob();
                    var tl = new eu.europa.esig.dss.tsl.source.TLSource();
                    tl.setUrl(fixture.httpUrl("/job-trusted-list.xml"));
                    var trustedCertificates = new eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource();
                    job.setTrustedListSources(tl);
                    job.setTrustedListCertificateSource(trustedCertificates);
                    job.setOfflineDataLoader(NetworkClients.fileCacheDataLoader(21600000));
                    try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
                        job.setExecutorService(executor);
                        job.offlineRefresh();
                    }
                    assertTrue(trustedCertificates.getCertificates().isEmpty());
                    assertEquals(8, fixture.destinationRequests.get());
                    assertEquals(8, fixture.requests.size());
                    assertTrue(fixture.requests.stream().anyMatch(r -> r.startsWith("GET " + fixture.httpUrl("/issuer"))));
                    assertTrue(fixture.requests.stream().anyMatch(r -> r.startsWith("GET " + fixture.httpUrl("/crl"))));
                    assertTrue(fixture.requests.stream().anyMatch(r -> r.startsWith("POST " + fixture.httpUrl("/ocsp"))));
                    assertTrue(fixture.contentTypes.contains("application/timestamp-query"));
                    assertTrue(fixture.contentTypes.contains("application/ocsp-request"));
                }
            }
            case "desktop" -> {
                try (var fixture = new LocalProxyFixture(Path.of(args[1]))) {
                    Preferences.userNodeForPackage(UserSettings.class).put("PROXY_URL", fixture.proxyUrl());
                    var settings = UserSettings.load();
                    NetworkClients.initialize(ProxyConfiguration.fromSaved(settings.getProxyUrl()));
                    // A changed proxy is saved, but used only after restart.
                    settings.setProxyUrl("");
                    settings.save();
                    assertEquals("", UserSettings.load().getProxyUrl());
                    assertThrows(IllegalStateException.class, () -> NetworkClients.initialize(ProxyConfiguration.NONE));
                    assertEquals("fixture-resource", new String(NetworkClients.dataLoader().get(fixture.httpUrl("/resource"))));
                    assertEquals(1, fixture.destinationRequests.get());
                    assertEquals(1, fixture.requests.size());
                }
            }
            case "cli" -> {
                try (var fixture = new LocalProxyFixture(Path.of(args[1]))) {
                    var options = new Options().addOption(null, "proxy", true, "")
                            .addOption(null, "tsa-server", true, "");
                    var cmd = new DefaultParser().parse(options, new String[]{"--proxy", fixture.proxyUrl(),
                            "--tsa-server", fixture.httpUrl("/timestamp")});
                    CliApp.start(cmd); // no source: networking is still initialized before the source is checked
                    assertEquals(ProxyConfiguration.parse(fixture.proxyUrl()), NetworkClients.configuration());
                    var settings = CliSettings.fromCmd(cmd);
                    assertThrows(eu.europa.esig.dss.model.DSSException.class, () -> settings.getTspSource()
                            .getTimeStampResponse(eu.europa.esig.dss.enumerations.DigestAlgorithm.SHA256, new byte[32]));
                    assertEquals(1, fixture.destinationRequests.get());
                    assertEquals(1, fixture.requests.size());
                }
            }
            default -> throw new IllegalArgumentException(args[0]);
        }
    }

    private static class RecordingDocumentValidator extends eu.europa.esig.dss.xades.validation.XMLDocumentValidator {
        eu.europa.esig.dss.spi.policy.SignaturePolicyProvider policyProvider;

        RecordingDocumentValidator() {
            super(new eu.europa.esig.dss.model.InMemoryDocument("<document/>".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }

        @Override
        public void setSignaturePolicyProvider(eu.europa.esig.dss.spi.policy.SignaturePolicyProvider provider) {
            policyProvider = provider;
            super.setSignaturePolicyProvider(provider);
        }
    }
}
