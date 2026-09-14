package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import digital.slovensko.autogram.TestMethodSources;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

public class SigningInputTests {
    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void inputAcceptsMinimalXadesParameters(DSSDocument document) {
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 800, true);

        var attributes = EFormAttributes.build(parameters, AutogramMimeType.isAsice(document.getMimeType()));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> SigningInput.fromDocument(AutogramDocument.build(document, attributes), parameters));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void inputAcceptsMinimalXadesParametersWithAsiceContainer(DSSDocument document) {
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, null, false, null, null, null, false, 800, true);

        var attributes = EFormAttributes.build(parameters, AutogramMimeType.isAsice(document.getMimeType()));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> SigningInput.fromDocument(AutogramDocument.build(document, attributes), parameters));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void inputAcceptsExplicitEformResources(DSSDocument document) throws IOException {
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, null, false, null, null, null, false, 800, true);
        var attributes = new EFormAttributes(
                "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
                new String(TestMethodSources.loadContent("general_agenda.xslt"), StandardCharsets.UTF_8),
                new String(TestMethodSources.loadContent("general_agenda.xsd"), StandardCharsets.UTF_8),
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", null, null, false, null,
                AutogramMimeType.isAsice(document.getMimeType()), parameters.getPropertiesCanonicalization(),
                parameters.getDigestAlgorithm());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> SigningInput.fromDocument(AutogramDocument.build(document, attributes), parameters));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#generalAgendaProvider")
    void inputAcceptsAutomaticallyLoadedEformResources(DSSDocument document) {
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 800, true);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> SigningInput.fromDocument(
                AutogramDocument.build(document, EFormAttributes.build(parameters, true)), parameters));
    }

    @Test
    void plainXmlIsRejectedWhenDisabled() throws IOException {
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "test.xml", MimeTypeEnum.XML),
                null);
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);

        assertThrows(UnknownEformException.class, () -> SigningInput.fromDocument(document, parameters));
    }

    @Test
    void plainXmlIsAllowedWhenEnabled() {
        var document = AutogramDocument.build(
                new InMemoryDocument("<document xmlns=\"urn:unknown-eform\"/>".getBytes(), "test.xml", MimeTypeEnum.XML),
                null);
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, true);

        var input = SigningInput.fromDocument(document, parameters);

        assertFalse(input.getFirstDocument().isEForm());
    }

    @Test
    void singleDocumentInputKeepsDocumentAndParameters() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureLevel.PAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);
        var input = SigningInput.fromDocument(document, parameters);

        assertFalse(input.isMultiDocument());
        assertEquals(document.getName(), input.getSingleDocument().getName());
        assertEquals(1, input.getDocuments().size());
        assertSame(parameters, input.getParameters());
    }

    @Test
    void multipleDocumentInputRejectsSingleDocumentAccessor() {
        var document1 = AutogramDocument.build(new InMemoryDocument("test-1".getBytes(), "test-1.pdf", MimeTypeEnum.PDF), null);
        var document2 = AutogramDocument.build(new InMemoryDocument("test-2".getBytes(), "test-2.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureLevel.PAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var input = SigningInput.of(List.of(document1, document2), parameters);

        assertTrue(input.isMultiDocument());
        assertEquals(ASiCContainerType.ASiC_E, input.getParameters().getContainer());
        assertThrows(IllegalStateException.class, input::getSingleDocument);
    }

    @Test
    void documentPreparationKeepsEFormAttributesAndForcesAsice() throws IOException {
        var attributes = new EFormAttributes(
                "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
                new String(TestMethodSources.loadContent("general_agenda.xslt"), StandardCharsets.UTF_8),
                new String(TestMethodSources.loadContent("general_agenda.xsd"), StandardCharsets.UTF_8),
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null, null, false, null, false, null, DigestAlgorithm.SHA256);
        var document = AutogramDocument.build(
                new InMemoryDocument(TestMethodSources.loadContent("general_agenda.xml"), "general_agenda.xml", MimeTypeEnum.XML),
                attributes);
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);
        var input = SigningInput.fromDocument(document, parameters);

        assertEquals(attributes.identifier(), input.getSingleDocument().getEFormAttributes().identifier());
        assertEquals(ASiCContainerType.ASiC_E, input.getParameters().getContainer());
    }
}