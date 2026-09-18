package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;

/**
 * SigningInput's own job: hold a document list + already-resolved SigningParameters, expose
 * accessors. Document/parameter compatibility rules (force ASiC-E, reject invalid packaging,
 * adopt a signed document's signature, ...) are SigningParametersResolver's job and are tested
 * directly in SigningParametersResolverTests.
 */
public class SigningInputTests {
    @Test
    void singleDocumentInputKeepsDocumentAndParameters() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
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
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        var input = SigningInput.of(List.of(document1, document2), parameters);

        assertTrue(input.isMultiDocument());
        assertEquals(2, input.getDocumentCount());
        assertThrows(IllegalStateException.class, input::getSingleDocument);
    }

    @Test
    void fromDocumentKeepsEFormAttributesOnTheWrappedDocument() throws IOException {
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
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);
        var input = SigningInput.fromDocument(document, parameters);

        assertEquals(attributes.identifier(), input.getSingleDocument().getEFormAttributes().identifier());
    }

    @Test
    void getPreviewDocumentsCountReturnsOneForAPlainSingleDocument() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);
        var input = SigningInput.fromDocument(document, parameters);

        assertEquals(1, input.getPreviewDocumentsCount());
    }

    @Test
    void getPreviewDocumentsCountUnwrapsAnAsiceDocumentWithMultipleOriginalDocuments() {
        var document = AutogramDocument.build(createAsiceWithMultipleFiles(), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, false);
        var input = SigningInput.prepareForASiCWithXAdES(document, parameters);

        assertEquals(2, input.getPreviewDocumentsCount());
    }

    @Test
    void getNameAndGetFirstDocumentReturnTheOnlyDocumentForASingleDocumentInput() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);
        var input = SigningInput.fromDocument(document, parameters);

        assertEquals("test.pdf", input.getName());
        assertSame(document, input.getFirstDocument());
    }

    private InMemoryDocument createAsiceWithMultipleFiles() {
        var asiceContent = "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";
        return new InMemoryDocument(Base64.getDecoder().decode(asiceContent), "multi-document.asice", MimeTypeEnum.ASICE);
    }
}
