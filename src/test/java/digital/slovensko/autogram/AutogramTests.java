package digital.slovensko.autogram;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.PDFAComplianceException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.CertificatesResponder;
import digital.slovensko.autogram.server.dto.CertificatesResponse;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.SupportedLanguage;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
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
        var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document), parameters);

        autogram.pickSigningKeyAndThen(
            key -> autogram.sign(SigningJob.fromInput(input, responder), key));

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
        var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document), parameters);

        autogram.pickSigningKeyAndThen(
            key -> autogram.sign(SigningJob.fromInput(input, responder), key));

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
        var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document), parameters);

        autogram.pickSigningKeyAndThen(
            key -> autogram.sign(SigningJob.fromInput(input, responder), key));
    }

        @Test
        void testSignMultipleDocumentsAsiceXadesHappyScenario() {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);
        var responder = mock(Responder.class);

        var firstDocument = AutogramDocument.fromContent("first".getBytes(), "first.txt",
            AutogramMimeType.fromMimeTypeString("text/plain"));
        var secondDocument = AutogramDocument.fromContent("second".getBytes(), "second.txt",
            AutogramMimeType.fromMimeTypeString("text/plain"));
        var parameters = SigningParameters.buildForASiCWithXAdES(firstDocument.toDssDocument(), false, false, null, true);

        autogram.pickSigningKeyAndThen(key -> autogram.sign(
            SigningJob.fromInput(SigningInput.of(List.of(firstDocument, secondDocument), parameters), responder),
            key));

        verify(responder).onDocumentSigned(any());
        }

        @Test
        void testSignedMultiDocumentAsiceSignatureCoversAllDocuments() {
        var settings = new TestSettings();
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI, settings);
        var responder = mock(Responder.class);

        var firstDocument = AutogramDocument.fromContent("first".getBytes(), "first.txt",
            AutogramMimeType.fromMimeTypeString("text/plain"));
        var secondDocument = AutogramDocument.fromContent("second".getBytes(), "second.txt",
            AutogramMimeType.fromMimeTypeString("text/plain"));
        var parameters = SigningParameters.buildForASiCWithXAdES(firstDocument.toDssDocument(), false, false, null, true);

        autogram.pickSigningKeyAndThen(key -> autogram.sign(
            SigningJob.fromInput(SigningInput.of(List.of(firstDocument, secondDocument), parameters), responder),
            key));

        var signedDocumentCaptor = org.mockito.ArgumentCaptor.forClass(SignedDocument.class);
        verify(responder).onDocumentSigned(signedDocumentCaptor.capture());

        var signedDocument = signedDocumentCaptor.getValue();
        var validationParameters = SigningParameters.buildForASiCWithXAdES(signedDocument.getDocument(), false, false,
            null, true);
        var validationDocument = AutogramDocument.fromDssDocument(signedDocument.getDocument());
        var validationInput = SigningInput.fromDocument(validationDocument, validationParameters);
        var validationJob = SigningJob.fromInput(validationInput, mock(Responder.class));
        var reports = SignatureValidator.getSignatureCheckReport(validationJob);

        Assertions.assertEquals(1, reports.getDocumentReports().size());

        var documentReport = reports.getDocumentReports().get(0);
        var signatureId = documentReport.reports().getSimpleReport().getSignatureIdList().get(0);

        Assertions.assertTrue(documentReport.hasMultipleContainerDocuments());
        Assertions.assertEquals(List.of("first.txt", "second.txt"), documentReport.getContainerContentFiles());
        Assertions.assertEquals(List.of("first.txt", "second.txt"), documentReport.getSignatureScopeDocumentNames(signatureId));
        Assertions.assertTrue(documentReport.signatureCoversAllDocuments(signatureId));
        Assertions.assertFalse(reports.hasIncompleteContainerCoverage());

        var previewDocuments = AsicContainerUtils.getOriginalDocuments(signedDocument.getDocument());
        Assertions.assertEquals(1, reports.getSignaturesForPreviewDocument(previewDocuments.get(0), 0).size());
        Assertions.assertEquals(1, reports.getSignaturesForPreviewDocument(previewDocuments.get(1), 1).size());
        }

    @Test
    void testStartVisualizationThrowsWhenLaterBundleDocumentIsLockedPdf() throws IOException {
        var settings = new TestSettings();
        var newUI = new FakeUI() {
            @Override
            public void showVisualization(Visualization visualization, Autogram autogram) {
                Assertions.fail("Visualization should not start for a bundle containing a locked PDF");
            }

            @Override
            public void showError(AutogramException exception) {
                throw exception;
            }
        };
        var autogram = new Autogram(newUI, settings);
        var job = createMultiDocumentJob(false,
                createTextDocument("first.txt", "first"),
                AutogramDocument.fromContent(createPasswordProtectedPdf(), "locked.pdf", MimeTypeEnum.PDF));

        var exception = Assertions.assertThrows(AutogramException.class, () -> autogram.startVisualization(job));

        Assertions.assertEquals("The document is password protected",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }

    @Test
    void testCheckPDFAComplianceFailsWhenLaterBundleDocumentIsNotPdfa() throws IOException {
        var settings = new TestSettings();
        var newUI = new FakeUI() {
            @Override
            public void onPDFAComplianceCheckFailed(SigningJob job) {
                throw new PDFAComplianceException();
            }
        };
        var autogram = new Autogram(newUI, settings);
        var job = createMultiDocumentJob(true,
                createTextDocument("first.txt", "first"),
                loadDocument("sample.pdf", MimeTypeEnum.PDF));

        Assertions.assertThrows(PDFAComplianceException.class, () -> autogram.checkPDFACompliance(job));
    }

    @Test
    void testSignatureCheckReportUsesLaterBundleDocument() throws IOException {
        var job = createMultiDocumentJob(false,
                createTextDocument("first.txt", "first"),
                loadDocument("sample_signed.pdf", MimeTypeEnum.PDF));

        var reports = SignatureValidator.getSignatureCheckReport(job);

        Assertions.assertTrue(reports.haveSignatures());
        Assertions.assertTrue(reports.getReports().getSimpleReport().getSignaturesCount() > 0);
    }

    @Test
    void testSignatureCheckReportIncludesAllSignedDocumentsInBundle() throws IOException {
        var job = createMultiDocumentJob(false,
                loadDocument("sample_signed.pdf", MimeTypeEnum.PDF),
                loadDocument("sample_pdf_xades.asice", MimeTypeEnum.ASICE));

        var reports = SignatureValidator.getSignatureCheckReport(job);

        Assertions.assertTrue(reports.haveSignatures());
        Assertions.assertEquals(2, reports.getDocumentReports().size());
        Assertions.assertEquals("sample_signed.pdf", reports.getDocumentReports().get(0).document().getName());
        Assertions.assertEquals("sample_pdf_xades.asice", reports.getDocumentReports().get(1).document().getName());
        Assertions.assertEquals(reports.getDocumentReports().get(0).getSignatureCount(),
            reports.getSignaturesForPreviewDocument(reports.getDocumentReports().get(0).document(), 0).size());
        Assertions.assertEquals(reports.getDocumentReports().get(1).getSignatureCount(),
            reports.getSignaturesForPreviewDocument(reports.getDocumentReports().get(1).document(), 1).size());
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.TestMethodSources#pdfForPadesProvider" })
    void testSignPadesHappyScenario(InMemoryDocument document) {
        var newUI = new FakeUI();
        var settings = new TestSettings();
        var autogram = new Autogram(newUI, settings);

        var parameters = SigningParameters.buildForPDF(document, false, false, null);
        var responder = mock(Responder.class);
        var input = SigningInput.fromDocument(AutogramDocument.fromDssDocument(document), parameters);

        autogram.pickSigningKeyAndThen(
            key -> autogram.sign(SigningJob.fromInput(input, responder), key));

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

        var input = SigningInput.fromFile(file, false, SignatureLevel.XAdES_BASELINE_B, false, null, false);
        autogram.pickSigningKeyAndThen(key -> autogram.sign(SigningJob.fromInput(input, responder), key));

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

    @SuppressWarnings("unused")
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

    private static AutogramDocument createTextDocument(String filename, String content) {
        return AutogramDocument.fromContent(content.getBytes(StandardCharsets.UTF_8), filename, MimeTypeEnum.TEXT);
    }

    private static AutogramDocument loadDocument(String resourceName, MimeTypeEnum mimeType) throws IOException {
        var content = Objects.requireNonNull(AutogramTests.class.getResourceAsStream(resourceName)).readAllBytes();
        return AutogramDocument.fromContent(content, resourceName, mimeType);
    }

    private static byte[] createPasswordProtectedPdf() throws IOException {
        try (var document = new PDDocument(); var outputStream = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());

            var permissions = new AccessPermission();
            var protectionPolicy = new StandardProtectionPolicy("owner-password", "user-password", permissions);
            protectionPolicy.setEncryptionKeyLength(128);
            protectionPolicy.setPermissions(permissions);

            document.protect(protectionPolicy);
            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    private static SigningJob createMultiDocumentJob(boolean checkPDFACompliance, AutogramDocument... documents) {
        var parameters = SigningParameters.buildForASiCWithXAdES(documents[0].toDssDocument(), checkPDFACompliance,
                false, null, true);
        return SigningJob.fromInput(SigningInput.of(List.of(documents), parameters), mock(Responder.class));
    }
}
