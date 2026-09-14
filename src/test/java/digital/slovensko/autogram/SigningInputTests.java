package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.SigningInput;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;

public class SigningInputTests {
    @Test
    void singleDocumentInputKeepsDocumentAndParameters() {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParameters.buildParameters(SignatureLevel.PAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640);
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
                null, null, false, null, null, null, false, 640);

        var input = SigningInput.of(List.of(document1, document2), parameters);

        assertTrue(input.isMultiDocument());
        assertEquals(ASiCContainerType.ASiC_E, input.getParameters().getContainer());
        assertThrows(IllegalStateException.class, input::getSingleDocument);
    }

    @Test
    void documentPreparationKeepsEFormAttributesAndForcesAsice() throws IOException {
        var attributes = new EFormAttributes(
                "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
                new String(getClass().getResourceAsStream("general_agenda.xslt").readAllBytes(), StandardCharsets.UTF_8),
                new String(getClass().getResourceAsStream("general_agenda.xsd").readAllBytes(), StandardCharsets.UTF_8),
                "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                null, null, false, null, false, null, DigestAlgorithm.SHA256);
        var document = AutogramDocument.build(
                new InMemoryDocument(getClass().getResourceAsStream("general_agenda.xml"), "general_agenda.xml", MimeTypeEnum.XML),
                attributes);
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, false, null, null, null, false, 640);
        var input = SigningInput.fromDocument(document, parameters);

        assertEquals(attributes.identifier(), input.getSingleDocument().getEFormAttributes().identifier());
        assertEquals(ASiCContainerType.ASiC_E, input.getParameters().getContainer());
    }
}