package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

public class SigningParametersTests {

    @Test()
    void testInvalidTransformation() {

        var transformation = "invalid transformation";
        var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
                ASiCContainerType.ASiC_E, null, SignaturePackaging.ENVELOPING,
                DigestAlgorithm.SHA256, false, CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE, null,
                transformation, "id1/asa", false, 800);

        params.getTransformationOutputMimeType();
        assertEquals(params.getTransformationException().getClass(), SAXParseException.class);
    }
}
