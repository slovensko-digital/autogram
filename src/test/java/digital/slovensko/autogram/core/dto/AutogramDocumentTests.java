package digital.slovensko.autogram.core.dto;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutogramDocumentTests {

    @Test
    void shouldSanitizeDocumentName() {
        var document = new InMemoryDocument("test".getBytes(), "test?file#name.xml", MimeTypeEnum.XML);
        var autogramDocument = AutogramDocument.build(document, null);

        assertEquals("test_file_name.xdcf", autogramDocument.getName());
    }
}
