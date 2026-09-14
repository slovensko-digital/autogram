package digital.slovensko.autogram.core;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.junit.jupiter.api.Test;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

class SigningParametersTests {
    @Test
    void rejectsMissingSignatureLevel() {
        assertThrows(SigningParametersException.class, () -> SigningParameters.buildParameters(
                null,
                DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING,
                false,
                CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE,
                false,
                800,
                true));
    }
}