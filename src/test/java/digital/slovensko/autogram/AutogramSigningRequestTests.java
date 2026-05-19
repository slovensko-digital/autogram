package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.AutogramSigningRequest;
import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class AutogramSigningRequestTests {
    @Test
    void singleDocumentRequestKeepsDocumentAndParameters() {
        var document = AutogramDocument.fromContent("test".getBytes(), "test.pdf", MimeTypeEnum.PDF);
        var parameters = SigningParameters.buildForPDF(document.toDssDocument(), false, false, null);

        var request = AutogramSigningRequest.forSingleDocument(document, parameters);

        assertFalse(request.isMultiDocument());
        assertSame(document, request.getSingleDocument());
        assertSame(parameters, request.getParameters());
        assertEquals(1, request.getDocuments().size());
    }

    @Test
    void multipleDocumentRequestRejectsSingleDocumentAccessor() {
        var document1 = AutogramDocument.fromContent("test-1".getBytes(), "test-1.pdf", MimeTypeEnum.PDF);
        var document2 = AutogramDocument.fromContent("test-2".getBytes(), "test-2.pdf", MimeTypeEnum.PDF);
        var parameters = SigningParameters.buildForPDF(document1.toDssDocument(), false, false, null);

        var request = AutogramSigningRequest.of(List.of(document1, document2), parameters);

        assertTrue(request.isMultiDocument());
        assertThrows(IllegalStateException.class, request::getSingleDocument);
    }
}