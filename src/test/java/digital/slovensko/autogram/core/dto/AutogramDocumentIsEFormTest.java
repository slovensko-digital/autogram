package digital.slovensko.autogram.core.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.core.SigningParametersResolver;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;

/**
 * Demonstrates that a plain (non-XDC, non-XML) document given an eForm identifier but no
 * xmldatacontainer containerXmlns is wrongly classified as an eForm.
 */
class AutogramDocumentIsEFormTest {
    private static final String IDENTIFIER = "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9";

    @Test
    void plainDocumentWithIdentifierButNoXdcContainerIsNotAnEForm() {
        var document = new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT);
        var attributes = new EFormAttributes(IDENTIFIER, null, null, null, null, null, false, null, false, null, null);

        var autogramDocument = AutogramDocument.build(document, attributes);

        assertFalse(autogramDocument.isEForm());
    }

    @Test
    void plainDocumentWithIdentifierDoesNotForceAsicContainer() {
        var document = new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT);
        var attributes = new EFormAttributes(IDENTIFIER, null, null, null, null, null, false, null, false, null, null);
        var autogramDocument = AutogramDocument.build(document, attributes);
        var requested = SigningParametersResolver.buildRequested(SignatureProfile.BASELINE_B, SignatureForm.PAdES,
                DigestAlgorithm.SHA256, null, null, false, null, null, null, false, 640, false);

        var resolved = SigningParametersResolver.resolveStrict(requested, List.of(autogramDocument));

        assertNull(resolved.getContainer());
    }
}
