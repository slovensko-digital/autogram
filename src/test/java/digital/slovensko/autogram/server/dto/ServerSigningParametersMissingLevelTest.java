package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;

class ServerSigningParametersMissingLevelTest {

    @Test
    void missingLevelIsReportedAsMissingField() {
        var params = new ServerSigningParameters();

        var exception = assertThrows(RequestValidationException.class,
                () -> params.validate(new InMemoryDocument("x".getBytes(), "x.xml", MimeTypeEnum.XML)));

        // Legacy /sign behavior: a missing level is a MISSING_FIELD "Parameters.Level" error,
        // not an UNSUPPORTED_SIGN_LEVEL error.
        assertEquals("Parameters.Level is required",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}
