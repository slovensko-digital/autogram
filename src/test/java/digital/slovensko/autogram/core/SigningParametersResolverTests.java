package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.AutogramMimeType;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

/**
 * Tests for SigningParametersResolver's decision rules directly, rather than indirectly through
 * SigningInput's factory methods. resolveStrict is the API contract (rejects an incompatible
 * combination); resolveLenient/resolveLenientFromFile are the standalone-defaults contract
 * (corrects an incompatible combination instead of failing).
 */
public class SigningParametersResolverTests {

    // --- resolveStrict: happy path for eform documents (moved from SigningInputTests) ---

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void resolveStrictAcceptsMinimalXadesParameters(DSSDocument document) {
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 800, true);
        var attributes = EFormAttributes.build(parameters, AutogramMimeType.isAsice(document.getMimeType()));
        var autogramDocument = AutogramDocument.build(document, attributes);

        assertDoesNotThrow(() -> SigningParametersResolver.resolveStrict(parameters, List.of(autogramDocument)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void resolveStrictAcceptsMinimalXadesParametersWithAsiceContainer(DSSDocument document) {
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, null, false, null, null, null, false, 800, true);
        var attributes = EFormAttributes.build(parameters, AutogramMimeType.isAsice(document.getMimeType()));
        var autogramDocument = AutogramDocument.build(document, attributes);

        assertDoesNotThrow(() -> SigningParametersResolver.resolveStrict(parameters, List.of(autogramDocument)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void resolveStrictAcceptsExplicitEformResources(DSSDocument document) throws IOException {
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, null, false, null, null, null, false, 800, true);
        var attributes = new EFormAttributes(
                "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
                new String(TestMethodSources.loadContent("general_agenda.xslt"), StandardCharsets.UTF_8),
                new String(TestMethodSources.loadContent("general_agenda.xsd"), StandardCharsets.UTF_8),
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", null, null, false, null,
                AutogramMimeType.isAsice(document.getMimeType()), parameters.getPropertiesCanonicalization(),
                parameters.getDigestAlgorithm());
        var autogramDocument = AutogramDocument.build(document, attributes);

        assertDoesNotThrow(() -> SigningParametersResolver.resolveStrict(parameters, List.of(autogramDocument)));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void resolveStrictAcceptsAutomaticallyLoadedEformResources(DSSDocument document) {
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 800, true);
        var autogramDocument = AutogramDocument.build(document, EFormAttributes.build(parameters, true));

        assertDoesNotThrow(() -> SigningParametersResolver.resolveStrict(parameters, List.of(autogramDocument)));
    }

    // --- unknown plain XML: always rejected, both modes (no sane correction exists) ---

    @Test
    void resolveStrictRejectsUnknownPlainXmlWhenDisabled() throws IOException {
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "test.xml", MimeTypeEnum.XML), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        assertThrows(UnknownEformException.class,
                () -> SigningParametersResolver.resolveStrict(parameters, List.of(document)));
    }

    @Test
    void resolveLenientAlsoRejectsUnknownPlainXmlWhenDisabled() throws IOException {
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "test.xml", MimeTypeEnum.XML), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        assertThrows(UnknownEformException.class,
                () -> SigningParametersResolver.resolveLenient(parameters, List.of(document)));
    }

    @Test
    void resolveStrictAllowsUnrecognizedPlainXmlWhenEnabled() {
        var document = AutogramDocument.build(
                new InMemoryDocument("<document xmlns=\"urn:unknown-eform\"/>".getBytes(), "test.xml", MimeTypeEnum.XML), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, true);

        assertDoesNotThrow(() -> SigningParametersResolver.resolveStrict(parameters, List.of(document)));
    }

    // --- XAdES packaging rule: strict rejects, lenient corrects ---

    @Test
    void resolveStrictRejectsEnvelopedXadesForNonXmlDocumentOutsideContainer() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        assertThrows(SigningParametersException.class,
                () -> SigningParametersResolver.resolveStrict(parameters, List.of(document)));
    }

    @Test
    void resolveLenientCorrectsEnvelopedXadesForNonXmlDocumentOutsideContainerInsteadOfThrowing() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = assertDoesNotThrow(() -> SigningParametersResolver.resolveLenient(parameters, List.of(document)));

        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    @Test
    void resolveStrictAcceptsEnvelopingXadesForNonXmlDocumentOutsideContainer() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        assertDoesNotThrow(() -> SigningParametersResolver.resolveStrict(parameters, List.of(document)));
    }

    // --- force ASiC-E container: multi-doc / eform / asice content ---

    @Test
    void resolveStrictForcesAsiceContainerForMultipleDocuments() {
        var document1 = AutogramDocument.build(new InMemoryDocument("test-1".getBytes(), "test-1.pdf", MimeTypeEnum.PDF), null);
        var document2 = AutogramDocument.build(new InMemoryDocument("test-2".getBytes(), "test-2.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveStrict(parameters, List.of(document1, document2));

        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    @Test
    void resolveStrictForcesAsiceContainerForAsiceContent() throws IOException {
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.asice"), "document.asice", MimeTypeEnum.ASICE),
                null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveStrict(parameters, List.of(document));

        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    @Test
    void resolveStrictForcesAsiceContainerForEformContent() throws IOException {
        var attributes = new EFormAttributes(
                "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
                new String(TestMethodSources.loadContent("general_agenda.xslt"), StandardCharsets.UTF_8),
                new String(TestMethodSources.loadContent("general_agenda.xsd"), StandardCharsets.UTF_8),
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null, null, false, null, false, null, DigestAlgorithm.SHA256);
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "general_agenda.xml", MimeTypeEnum.XML),
                attributes);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveStrict(parameters, List.of(document));

        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    // --- identity preservation: no correction needed -> same reference returned ---

    @Test
    void resolveStrictReturnsSameReferenceWhenNoCorrectionIsNeeded() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveStrict(parameters, List.of(document));

        assertSame(parameters, resolved);
    }

    @Test
    void resolveLenientReturnsSameReferenceWhenNoCorrectionIsNeeded() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenient(parameters, List.of(document));

        assertSame(parameters, resolved);
    }

    // --- resolveLenientFromFile: PDF + requested level routing ---

    @Test
    void resolveLenientFromFileKeepsRawPdfForPadesLevel() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.PAdES, resolved.getSignatureForm());
        assertNull(resolved.getContainer());
    }

    @Test
    void resolveLenientFromFileWrapsPdfInAsiceWithCadesWhenRequested() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.CAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.CAdES, resolved.getSignatureForm());
        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    @Test
    void resolveLenientFromFileWrapsPdfInAsiceWithXadesWhenRequested() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.XAdES, resolved.getSignatureForm());
        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    @Test
    void resolveLenientFromFileWrapsNonPdfContentInAsiceWithXadesByDefault() throws IOException {
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "general_agenda.xml", MimeTypeEnum.XML),
                null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, true);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.XAdES, resolved.getSignatureForm());
        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    // --- resolveLenientFromFile: already-signed document adoption ---

    @Test
    void resolveLenientFromFileAdoptsSignedPdfFormAndKeepsRequestedPackagingWhenSignatureHasNone() throws IOException {
        var document = AutogramDocument.build(new InMemoryDocument(
                TestMethodSources.loadContent("sample_signed.pdf"), "sample_signed.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.PAdES, resolved.getSignatureForm());
        assertNull(resolved.getContainer());
        assertEquals(SignaturePackaging.ENVELOPING, resolved.getSignaturePackaging());
    }

    @Test
    void resolveLenientFromFileAdoptsXadesInAsiceContainerFromAnAlreadySignedDocument() throws IOException {
        var document = AutogramDocument.build(new InMemoryDocument(
                TestMethodSources.loadContent("sample_pdf_xades.asice"), "sample_pdf_xades.asice", MimeTypeEnum.ASICE), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.XAdES, resolved.getSignatureForm());
        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    @Test
    void resolveLenientFromFileAdoptsCadesInAsiceContainerFromAnAlreadySignedDocument() throws IOException {
        var document = AutogramDocument.build(new InMemoryDocument(
                TestMethodSources.loadContent("sample_pdf_cades.asice"), "sample_pdf_cades.asice", MimeTypeEnum.ASICE), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveLenientFromFile(parameters, document);

        assertEquals(SignatureForm.CAdES, resolved.getSignatureForm());
        assertEquals(ASiCContainerType.ASiC_E, resolved.getContainer());
    }

    // --- resolveLenientFromFile: independent results from a shared input across a batch ---

    @Test
    void resolveLenientFromFileProducesIndependentResultsForDifferentDocumentsFromSharedInput() throws IOException {
        // Mirrors CliApp/BatchFileResponder: one SigningParameters instance built once from user
        // defaults and reused for every file in a batch. Since SigningParameters is immutable this
        // can no longer literally mutate, but resolution must still be independent per document.
        var sharedDefaults = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        var eformDocument = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "general_agenda.xml", MimeTypeEnum.XML),
                EFormAttributes.build(sharedDefaults, true));
        var eformResolved = SigningParametersResolver.resolveLenientFromFile(sharedDefaults, eformDocument);

        assertEquals(SignatureForm.XAdES, eformResolved.getSignatureForm());
        assertSame(SignatureForm.PAdES, sharedDefaults.getSignatureForm(),
                "the shared input instance must be unaffected by resolving the eform document");
        assertEquals(ASiCContainerType.ASiC_E, sharedDefaults.getContainer());

        var pdfDocument = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var pdfResolved = SigningParametersResolver.resolveLenientFromFile(sharedDefaults, pdfDocument);

        assertEquals(SignatureProfile.BASELINE_B, pdfResolved.getSignatureProfile());
        assertEquals(SignatureForm.PAdES, pdfResolved.getSignatureForm());
    }

    // --- withFormAndContainer / withSignedDocumentSignature / buildRequested ---

    @Test
    void withFormAndContainerOverridesFormAndContainerButKeepsOtherFields() {
        var requested = SigningParameters.buildParameters(SignatureProfile.BASELINE_T, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, true, null, null, null, true, 640, true);

        var result = SigningParametersResolver.withFormAndContainer(requested, SignatureForm.CAdES, ASiCContainerType.ASiC_E);

        assertEquals(SignatureForm.CAdES, result.getSignatureForm());
        assertEquals(ASiCContainerType.ASiC_E, result.getContainer());
        assertEquals(SignatureProfile.BASELINE_T, result.getSignatureProfile());
        assertEquals(SignaturePackaging.ENVELOPING, result.getSignaturePackaging());
        assertTrue(result.getCheckPDFACompliance());
        assertTrue(result.isPlainXmlEnabled());
    }

    @Test
    void buildRequestedProducesParametersMatchingTheGivenLevel() {
        var result = SigningParametersResolver.buildRequested(SignatureProfile.BASELINE_T, SignatureForm.XAdES,
                DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, true, null, null, null, true, 640, true);

        assertEquals(SignatureLevel.XAdES_BASELINE_T, result.getLevel());
        assertEquals(ASiCContainerType.ASiC_E, result.getContainer());
    }

    @Test
    void plainDocumentWithIdentifierDoesNotForceAsicContainer() {
        var document = new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT);
        var attributes = new EFormAttributes("http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9", null, null, null, null, null, false, null, false, null, null);
        var autogramDocument = AutogramDocument.build(document, attributes);
        var requested = SigningParametersResolver.buildRequested(SignatureProfile.BASELINE_B, SignatureForm.PAdES,
                DigestAlgorithm.SHA256, null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveStrict(requested, List.of(autogramDocument));

        assertNull(resolved.getContainer());
    }

    @Test
    void pdfForPadesMustNotKeepAsicEContainer() throws IOException {
        var params = SigningParametersResolver.buildRequested(
                SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                false, null, null, null, false, 640, false);
        var resolved = SigningParametersResolver.resolveLenientFromFile(
                params, AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "sample.pdf", MimeTypeEnum.PDF), null));

        assertEquals(SignatureForm.PAdES, resolved.getSignatureForm());
        assertNull(resolved.getContainer(),
                "A PDF auto-detected for PAdES must resolve to a raw PDF (container == null), not ASiC-E");
    }

    @Test
    void pdfForPadesWithTsaMustStayPades() throws IOException {
        var params = SigningParametersResolver.buildRequested(
                SignatureProfile.BASELINE_T, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                false, null, null, null, false, 640, false);
        var resolved = SigningParametersResolver.resolveLenientFromFile(
                params, AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "sample.pdf", MimeTypeEnum.PDF), null));

        assertEquals(SignatureForm.PAdES, resolved.getSignatureForm(),
                "A PDF with TSA enabled (BASELINE_T) must stay PAdES, not silently become XAdES");
    }
}
