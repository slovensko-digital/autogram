package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class SigningInputTests {
    @Test
    void singleDocumentInputKeepsDocumentAndParameters() {
        var document = AutogramDocument.fromContent("test".getBytes(), "test.pdf", MimeTypeEnum.PDF);
        var input = SigningInput.prepareForPDF(document, false, false, null);

        assertFalse(input.isMultiDocument());
        assertEquals(document.getName(), input.getSingleDocument().getName());
        assertEquals(1, input.getDocuments().size());
    }

    @Test
    void multipleDocumentInputRejectsSingleDocumentAccessor() {
        var document1 = AutogramDocument.fromContent("test-1".getBytes(), "test-1.pdf", MimeTypeEnum.PDF);
        var document2 = AutogramDocument.fromContent("test-2".getBytes(), "test-2.pdf", MimeTypeEnum.PDF);
        var preparedInput = SigningInput.prepareForPDF(document1, false, false, null);

        var input = SigningInput.of(List.of(document1, document2), preparedInput.getParameters());

        assertTrue(input.isMultiDocument());
        assertThrows(IllegalStateException.class, input::getSingleDocument);
    }

    @Test
    void documentPreparationKeepsEFormAttributes() {
        var attributes = new EFormAttributes("identifier", null, null, null, null, null, false);
        var document = AutogramDocument.fromContent("test".getBytes(), "test.txt", MimeTypeEnum.TEXT)
                .withEFormAttributes(attributes);
        var parameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, DigestAlgorithm.SHA256,
            ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, false, null, null, null, false, false, 640,
            null);
        var input = SigningInput.fromDocument(document, parameters);

        assertSame(attributes, input.getSingleDocument().getEFormAttributes());
    }
}