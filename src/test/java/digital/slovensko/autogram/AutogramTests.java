package digital.slovensko.autogram;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.CertificatesResponder;
import digital.slovensko.autogram.server.dto.CertificatesResponse;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutogramTests {
    private static final Path tempTestsPath = Path.of(System.getProperty("java.io.tmpdir"), "autogram-tests");

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#generalAgendaProvider",
            "digital.slovensko.autogram.TestMethodSources#unsetXdcfMimetypeProvider",
            "digital.slovensko.autogram.TestMethodSources#orsrDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#validOtherDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#validXadesDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#fsDPFOProvider" })
    void testSignAsiceXadesHappyScenario(InMemoryDocument document) {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);

        var parameters = SigningParameters.buildForASiCWithXAdES(document, false, false, null, false);
        var responder = mock(Responder.class);

        autogram.pickSigningKeyAndThen(
                key -> autogram.sign(SigningJob.buildFromRequest(document, parameters, responder), key));

        verify(responder).onDocumentSigned(any());
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#nonEformXmlProvider"})
    void testSignNonEformHappyScenario(InMemoryDocument document) {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);

        var parameters = SigningParameters.buildForASiCWithXAdES(document, false, false, null, true);
        var responder = mock(Responder.class);

        autogram.pickSigningKeyAndThen(
                key -> autogram.sign(SigningJob.buildFromRequest(document, parameters, responder), key));

        verify(responder).onDocumentSigned(any());
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#nonEformXmlProvider"})
    void testSignNonEformNegativeScenario(InMemoryDocument document) {
        Assertions.assertThrows(UnknownEformException.class, () -> SigningParameters.buildForASiCWithXAdES(document, false, false, null, false));
    }

    @ParameterizedTest
    @MethodSource({"digital.slovensko.autogram.TestMethodSources#validOtherDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#validCadesDocumentsProvider"})
    void testSignAsiceCadesHappyScenario(InMemoryDocument document) {
        var newUI = new FakeUI();
        var settings = new TestSettings();
        var autogram = new Autogram(newUI, settings);

        var parameters = SigningParameters.buildForASiCWithCAdES(document, false, false, null, false);
        var responder = mock(Responder.class);

        autogram.pickSigningKeyAndThen(
                key -> autogram.sign(SigningJob.buildFromRequest(document, parameters, responder), key));
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#pdfForPadesProvider" })
    void testSignPadesHappyScenario(InMemoryDocument document) {
        var newUI = new FakeUI();
        var settings = new TestSettings();
        var autogram = new Autogram(newUI, settings);

        var parameters = SigningParameters.buildForPDF(document, false, false, null);
        var responder = mock(Responder.class);

        autogram.pickSigningKeyAndThen(
                key -> autogram.sign(SigningJob.buildFromRequest(document, parameters, responder), key));

        verify(responder).onDocumentSigned(any());
    }

    @BeforeAll
    public static void setupTempTestDirectory() {
        tempTestsPath.toFile().mkdirs();
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#generalAgendaProvider",
            "digital.slovensko.autogram.TestMethodSources#unsetXdcfMimetypeProvider",
            "digital.slovensko.autogram.TestMethodSources#validOtherDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#validXadesDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#validCadesDocumentsProvider",
            "digital.slovensko.autogram.TestMethodSources#pdfForPadesProvider",
            "digital.slovensko.autogram.TestMethodSources#fsDPFOProvider"})
    void testSignBuildFromFileHappyScenario(InMemoryDocument document) throws IOException {
        var newUI = new FakeUI();
        var settings = new TestSettings();
        var autogram = new Autogram(newUI, settings);

        var file = new File(Path.of(tempTestsPath.toString(), document.getName()).toString());
        var outputStream = new FileOutputStream(file);
        outputStream.write(document.getBytes());
        outputStream.close();

        var responder = mock(Responder.class);

        autogram.pickSigningKeyAndThen(
                key -> autogram.sign(SigningJob.buildFromFile(file, responder, false, SignatureLevel.XAdES_BASELINE_B, false, null, false), key));

        verify(responder).onDocumentSigned(any());
    }

    @Test
    void testSignCertificatePickFailed() {

    }

    @Test
    void testSignFailedAfterCertificatePick() {

    }

    @Test
    void testGetCertificatesThrowsOnNonexistentDriver() {
        var settings = new TestSettings();
        var newUI = mock(UI.class);
        var autogram = new Autogram(newUI, settings);

        var responder = mock(CertificatesResponder.class);
        List<String> drivers = List.of("nonexistent-driver");
        autogram.consentCertificateReadingAndThen(responder, drivers);
        verify(responder).onError(any(NoDriversDetectedException.class));
    }

    @Test
    void testGetCertificatesThrowsOnConsentRejected() {
        var settings = new TestSettings();
        var newUI = new FakeUI() {
                    @Override
                    public void consentCertificateReadingAndThen(Consumer<Runnable> callback, Runnable onCancel) {
                        onCancel.run();
                    }
        };
        var autogram = new Autogram(newUI, settings);

        var responder = mock(CertificatesResponder.class);
        List<String> drivers = List.of();
        autogram.consentCertificateReadingAndThen(responder, drivers);
        verify(responder).onError(any(CertificatesReadingConsentRejectedException.class));
    }

    @Test
    void testGetCertificatesHappyScenario() {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);

        var responder = mock(CertificatesResponder.class);
        List<String> drivers = List.of();
        autogram.consentCertificateReadingAndThen(responder, drivers);

        verify(responder).onSuccess(any());
    }

    @Test
    void testGetCertificatesHappyScenarioWithResponse() throws IOException {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);

        var exchange = mock(HttpExchange.class);
        var responseBody = mock(java.io.OutputStream.class);
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));
        when(exchange.getRequestURI()).thenReturn(mock(java.net.URI.class));
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getResponseBody()).thenReturn(responseBody);

        var responder = new CertificatesResponder(exchange);
        List<String> drivers = List.of();
        autogram.consentCertificateReadingAndThen(responder, drivers);

        verify(exchange).sendResponseHeaders(200, 0);

        var expected = new Gson().fromJson("{\"certificates\":[{\"subject\":\"CN\u003dJano Suchal, O\u003dSolver IT\",\"issuedBy\":\"CN\u003dJano Suchal, O\u003dSolver IT\"}]}", CertificatesResponse.class);
        verify(responseBody).write(new Gson().toJson(expected).getBytes());
    }

    @Test
    void testGetCertificatesHappyScenarioWithDriverSelectorAndResponse() throws IOException {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);

        var exchange = mock(HttpExchange.class);
        var responseBody = mock(java.io.OutputStream.class);
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));
        when(exchange.getRequestURI()).thenReturn(mock(java.net.URI.class));
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getResponseBody()).thenReturn(responseBody);

        var responder = new CertificatesResponder(exchange);
        List<String> drivers = List.of("fake", "non-existent-driver");
        autogram.consentCertificateReadingAndThen(responder, drivers);

        verify(exchange).sendResponseHeaders(200, 0);

        var expected = new Gson().fromJson("{\"certificates\":[{\"subject\":\"CN\u003dJano Suchal, O\u003dSolver IT\",\"issuedBy\":\"CN\u003dJano Suchal, O\u003dSolver IT\"}]}", CertificatesResponse.class);
        verify(responseBody).write(new Gson().toJson(expected).getBytes());
    }

    private record FakeDriverDetector(List<TokenDriver> drivers) implements DriverDetector {
        @Override
        public List<TokenDriver> getAvailableDrivers() {
            return drivers;
        }
    }

    private static class FakeTokenDriver extends TokenDriver {
        public FakeTokenDriver(String name) {
            super(name, Path.of(""), "fake", "");
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
            try {
                var keystore = Objects.requireNonNull(this.getClass().getResource("test.keystore")).getFile();
                return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("".toCharArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FakeTokenDriverWithExpiredCertificate extends TokenDriver {

        public FakeTokenDriverWithExpiredCertificate() {
            super("fake-token-driver-with-expired-certificate", Path.of(""), "fake", "");
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
            try {
                var keystore = Objects.requireNonNull(this.getClass().getResource("expired_certificate.keystore"))
                        .getFile();
                return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("test123".toCharArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FakeUI implements UI {
        @Override
        public void startSigning(SigningJob signingJob, Autogram autogram) {

        }

        @Override
        public void startBatch(Batch batch, Autogram autogram, BatchStartCallback callback) {
        }

        @Override
        public void cancelBatch(Batch batch) {
        }

        @Override
        public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback, Runnable onCancel) {
            callback.accept(drivers.get(0));
        }

        @Override
        public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback) {
            callback.accept(keys.get(0));
        }

        @Override
        public void onWorkThreadDo(Runnable callback) {
            callback.run();
        }

        @Override
        public void onUIThreadDo(Runnable callback) {
            callback.run();
        }

        @Override
        public void onUpdateAvailable() {

        }

        @Override
        public void onAboutInfo() {

        }

        @Override
        public void onPDFAComplianceCheckFailed(SigningJob job) {

        }

        @Override
        public void showVisualization(Visualization visualization, Autogram autogram) {

        }

        @Override
        public void showIgnorableExceptionDialog(IgnorableException exception) {

        }

        @Override
        public void showError(AutogramException exception) {

        }

        @Override
        public char[] getKeystorePassword() {
            return null;
        }

        @Override
        public char[] getContextSpecificPassword() {
            return null;
        }

        @Override
        public void onSigningSuccess(SigningJob signingJob) {

        }

        @Override
        public void onSigningFailed(AutogramException e, SigningJob job) {
            throw e;
        }

        @Override
        public void onSigningFailed(AutogramException e) {
            throw e;
        }

        @Override
        public void onDocumentSaved(File targetFiles) {

        }

        @Override
        public void onDocumentBatchSaved(BatchUiResult result) {

        }

        @Override
        public void onPickSigningKeyFailed(AutogramException ae) {
            throw new RuntimeException();
        }

        @Override
        public void onSignatureValidationCompleted(ValidationReports wrapper) {

        }

        @Override
        public void onSignatureCheckCompleted(ValidationReports wrapper) {

        }

        @Override
        public void updateBatch() {

        }

        @Override
        public void resetSigningKey() {

        }

        @Override
        public void consentCertificateReadingAndThen(Consumer<Runnable> callback, Runnable onCancel) {
            callback.accept(() -> {});
        }
    }

    private class TestSettings extends UserSettings {
        @Override
        public DriverDetector getDriverDetector() {
            List<TokenDriver> drivers = List.of(new FakeTokenDriver("fake"));
            return new FakeDriverDetector(drivers);
        }
    }
}
