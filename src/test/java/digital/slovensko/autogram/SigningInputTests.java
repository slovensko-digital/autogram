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
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class SigningInputTests {
    @Test
    void singleDocumentInputKeepsDocumentAndParameters() {
        var document = AutogramDocument.fromContent("test".getBytes(), "test.pdf", MimeTypeEnum.PDF);
        var parameters = SigningParameters.buildForPDF(document.toDssDocument(), false, false, null);

        var input = SigningInput.fromDocument(document, parameters);

        assertFalse(input.isMultiDocument());
        assertEquals(document.getName(), input.getSingleDocument().getName());
        assertSame(parameters, input.getParameters());
        assertEquals(1, input.getDocuments().size());
    }

    @Test
    void multipleDocumentInputRejectsSingleDocumentAccessor() {
        var document1 = AutogramDocument.fromContent("test-1".getBytes(), "test-1.pdf", MimeTypeEnum.PDF);
        var document2 = AutogramDocument.fromContent("test-2".getBytes(), "test-2.pdf", MimeTypeEnum.PDF);
        var parameters = SigningParameters.buildForPDF(document1.toDssDocument(), false, false, null);

        var input = SigningInput.of(List.of(document1, document2), parameters);

        assertTrue(input.isMultiDocument());
        assertThrows(IllegalStateException.class, input::getSingleDocument);
    }

    @Test
    void documentPreparationKeepsEFormAttributes() {
        var attributes = new EFormAttributes("identifier", null, null, null, null, null, false);
        var document = AutogramDocument.fromContent("test".getBytes(), "test.txt", MimeTypeEnum.TEXT)
                .withEFormAttributes(attributes);
        var parameters = SigningParameters.buildForASiCWithXAdES(document.toDssDocument(), false, false, null, true);

        var input = SigningInput.fromDocument(document, parameters);

        assertSame(attributes, input.getSingleDocument().getEFormAttributes());
    }
}