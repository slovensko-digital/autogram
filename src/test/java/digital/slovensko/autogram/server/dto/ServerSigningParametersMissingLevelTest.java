package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.ui.SupportedLanguage;

class ServerSigningParametersMissingLevelTest {

    @Test
    void missingLevelIsReportedAsMissingField() {
        var document = new LegacyDocument("x.xml", "eA==");
        var legacyBody = new SignRequestBody(document, null, "application/xml;base64");

        var exception = assertThrows(RequestValidationException.class,
                () -> legacyBody.toVersionedBody());

        // Legacy /sign behavior: the level may only be omitted for an already signed document,
        // whose signature form is then reused.
        assertEquals("Parameters.Level can't be empty if document is not signed yet",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}
