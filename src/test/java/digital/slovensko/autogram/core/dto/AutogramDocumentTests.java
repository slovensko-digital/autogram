package digital.slovensko.autogram.core.dto;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutogramDocumentTests {

    /**
     * Verifies that '?' and '#' characters in a document filename are replaced with
     * underscores during AutogramDocument.build(). XML is intentionally avoided here
     * because it triggers XDC validation which requires non-null signing parameters;
     * PDF exercises the sanitize-then-normalize path without XDC processing.
     */
    @ParameterizedTest
    @CsvSource({
        "test?file.pdf,       test_file.pdf",
        "test#file.pdf,       test_file.pdf",
        "test?file#name.pdf,  test_file_name.pdf",
        "normal-name.pdf,     normal-name.pdf",
    })
    void shouldSanitizeDocumentName(String inputName, String expectedName) {
        var document = new InMemoryDocument("test".getBytes(), inputName.strip(), MimeTypeEnum.PDF);
        var autogramDocument = AutogramDocument.build(document, null);

        assertEquals(expectedName.strip(), autogramDocument.getName());
    }
}
