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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/** Each scenario gets a fresh process and isolated Preferences: no real user settings are changed. */
public class StartupProxyTest {
    @TempDir Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"desktop", "invalid-saved", "cli", "cli-configured", "invalid-cli", "dss-sources"})
    void startupScenarios(String scenario) throws Exception {
        runScenario(scenario);
        if (scenario.equals("desktop"))
            runScenario("desktop-restarted");
    }

    private void runScenario(String scenario) throws Exception {
        var log = directory.resolve("process.log");
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Djava.util.prefs.userRoot=" + directory.resolve("prefs"),
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
        var prefs = Preferences.userNodeForPackage(UserSettings.class);
        var options = new Options().addOption(null, "proxy", true, "")
                .addOption(null, "tsa-server", true, "");
        switch (args[0]) {
            case "dss-sources" -> {
                try (var fixture = new LocalProxyFixture(Path.of(args[1]))) {
                    NetworkClients.initialize(ProxyConfiguration.parse(fixture.proxyUrl()));
                    var token = new eu.europa.esig.dss.model.x509.CertificateToken(fixture.certificate);
                    var verifier = NetworkClients.certificateVerifier();
                    assertEquals(1, verifier.getAIASource().getCertificatesByAIA(token).size());
                    // Intentionally malformed responses: routing must not imply successful validation.
                    var crl = new eu.europa.esig.dss.service.crl.OnlineCRLSource(NetworkClients.dataLoader());
                    assertThrows(DSSExternalResourceException.class, () -> crl.getRevocationToken(token, token));
                    var ocsp = new eu.europa.esig.dss.service.ocsp.OnlineOCSPSource(NetworkClients.ocspDataLoader());
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
                    System.setProperty("jpackage.app-version", "2.0.0");
                    assertTrue(Updater.newVersionAvailable(fixture.httpUrl("/update-json")));
                    assertEquals(9, fixture.destinationRequests.get());
                    assertEquals(9, fixture.requests.size());
                    assertTrue(fixture.requests.stream().anyMatch(r -> r.startsWith("GET " + fixture.httpUrl("/issuer"))));
                    assertTrue(fixture.requests.stream().anyMatch(r -> r.startsWith("GET " + fixture.httpUrl("/crl"))));
                    assertTrue(fixture.requests.stream().anyMatch(r -> r.startsWith("POST " + fixture.httpUrl("/ocsp"))));
                    assertTrue(fixture.contentTypes.contains("application/timestamp-query"));
                    assertTrue(fixture.contentTypes.contains("application/ocsp-request"));
                }
            }
            case "desktop" -> {
                try (var fixture = new LocalProxyFixture(Path.of(args[1]))) {
                    prefs.put("PROXY_URL", fixture.proxyUrl());
                    NetworkClients.initialize(ProxyConfiguration.fromSaved(UserSettings.savedProxyUrl()));
                    var settings = UserSettings.load();
                    assertEquals(fixture.proxyUrl(), settings.getProxyUrl());
                    assertThrows(IllegalArgumentException.class, () -> settings.save("https://bad:80"));
                    assertEquals(fixture.proxyUrl(), settings.getProxyUrl());
                    assertEquals(fixture.proxyUrl(), UserSettings.savedProxyUrl());
                    settings.setProxyUrl("");
                    assertEquals(fixture.proxyUrl(), UserSettings.savedProxyUrl()); // staged, not saved
                    settings.save();
                    assertEquals("", UserSettings.load().getProxyUrl());
                    assertEquals(ProxyConfiguration.State.CONFIGURED, NetworkClients.configuration().state());
                    assertEquals("fixture-resource", new String(NetworkClients.dataLoader().get(fixture.httpUrl("/resource"))));
                    settings.reset();
                    assertEquals("", UserSettings.savedProxyUrl());
                    assertEquals(ProxyConfiguration.State.CONFIGURED, NetworkClients.configuration().state());
                    assertThrows(IllegalStateException.class, () -> NetworkClients.initialize(ProxyConfiguration.parse("")));
                    assertEquals("fixture-resource", new String(NetworkClients.dataLoader().get(fixture.httpUrl("/resource"))));
                    assertEquals(2, fixture.destinationRequests.get());
                    assertEquals(2, fixture.requests.size());
                }
            }
            case "invalid-saved" -> {
                prefs.put("PROXY_URL", "invalid");
                NetworkClients.initialize(ProxyConfiguration.fromSaved(UserSettings.savedProxyUrl()));
                var settings = assertDoesNotThrow(UserSettings::load);
                assertEquals("invalid", settings.getProxyUrl());
                assertNotNull(settings.getTspSource());
                assertThrows(DSSExternalResourceException.class, () -> NetworkClients.dataLoader().get("http://127.0.0.1:1/test"));
                assertNull(new EFormResourceLoader().getResource("http://127.0.0.1:1/not-cached"));
                settings.setProxyUrl("http://localhost:8080");
                settings.save();
                assertEquals("http://localhost:8080", UserSettings.load().getProxyUrl());
                assertEquals(ProxyConfiguration.State.BLOCKED, NetworkClients.configuration().state());
                settings.reset();
                assertEquals("", UserSettings.savedProxyUrl());
                assertEquals(ProxyConfiguration.State.BLOCKED, NetworkClients.configuration().state());
                assertFalse(Updater.newVersionAvailable());
                assertNull(NetworkClients.signaturePolicyProvider().getSignaturePolicyByUrl("http://127.0.0.1:1/policy"));
            }
            case "desktop-restarted" -> {
                assertEquals("", UserSettings.savedProxyUrl());
                NetworkClients.initialize(ProxyConfiguration.fromSaved(UserSettings.savedProxyUrl()));
                assertEquals(ProxyConfiguration.State.UNCONFIGURED, NetworkClients.configuration().state());
                assertEquals("", UserSettings.load().getProxyUrl());
            }
            case "cli-configured" -> {
                try (var fixture = new LocalProxyFixture(Path.of(args[1]))) {
                    prefs.put("PROXY_URL", "invalid-desktop-setting");
                    var cmd = new DefaultParser().parse(options, new String[]{"--proxy", fixture.proxyUrl(),
                            "--tsa-server", fixture.httpUrl("/timestamp")});
                    CliApp.start(cmd); // no source: config and TSA construction still precede source processing
                    assertEquals(ProxyConfiguration.State.CONFIGURED, NetworkClients.configuration().state());
                    var settings = CliSettings.fromCmd(cmd);
                    assertThrows(eu.europa.esig.dss.model.DSSException.class, () -> settings.getTspSource()
                            .getTimeStampResponse(eu.europa.esig.dss.enumerations.DigestAlgorithm.SHA256, new byte[32]));
                    assertEquals("invalid-desktop-setting", UserSettings.savedProxyUrl());
                    assertEquals(1, fixture.requests.size());
                    assertEquals(1, fixture.destinationRequests.get());
                }
            }
            case "cli" -> {
                prefs.put("PROXY_URL", "invalid-desktop-setting");
                NetworkClients.initialize(ProxyConfiguration.parse(null));
                var settings = CliSettings.fromCmd(new DefaultParser().parse(options, new String[]{}));
                assertEquals("", settings.getProxyUrl());
                assertEquals(ProxyConfiguration.State.UNCONFIGURED, NetworkClients.configuration().state());
                settings = CliSettings.fromCmd(new DefaultParser().parse(options,
                        new String[]{"--proxy", "http://localhost:3128"}));
                assertEquals("http://localhost:3128", settings.getProxyUrl());
                assertEquals("invalid-desktop-setting", UserSettings.savedProxyUrl());
                // Language-related repeated loads must not activate desktop routing.
                UserSettings.load();
                assertEquals(ProxyConfiguration.State.UNCONFIGURED, NetworkClients.configuration().state());
                var validator = new RecordingDocumentValidator();
                NetworkClients.configureDocumentValidator(validator);
                assertNull(validator.policyProvider);
            }
            case "invalid-cli" -> {
                prefs.put("PROXY_URL", "http://desktop:8080");
                var output = new ByteArrayOutputStream();
                System.setErr(new PrintStream(output));
                CliApp.start(new DefaultParser().parse(options, new String[]{"--proxy", "https://invalid:80"}));
                assertTrue(output.toString().contains("Configuration error:"));
                assertEquals(ProxyConfiguration.State.UNCONFIGURED, NetworkClients.configuration().state());
                assertEquals("http://desktop:8080", UserSettings.savedProxyUrl());
                // An invalid argument is rejected before even initializing the process networking.
                NetworkClients.initialize(ProxyConfiguration.parse(null));
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
