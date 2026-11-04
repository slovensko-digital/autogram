package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.SigningParametersException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.junit.jupiter.api.Test;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;

class SigningParametersTests {
    @Test
    void rejectsMissingSignatureProfile() {
        assertThrows(SigningParametersException.class, () -> SigningParameters.buildParameters(
                null,
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

    @Test
    void nullDigestAlgorithmDefaultsToSha256AtConstructionTime() {
        var parameters = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, null,
                null, null, false, null, null, null, false, 640, false);

        assertEquals(DigestAlgorithm.SHA256, parameters.getDigestAlgorithm());
    }

    @Test
    void getLevelDefaultsFormToXadesWhenOnlyProfileIsSet() {
        var parameters = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_T, null, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        assertEquals(SignatureLevel.XAdES_BASELINE_T, parameters.getLevel());
    }

    @Test
    void unsetPackagingDefaultsToEnveloped() {
        var parameters = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        assertEquals(SignaturePackaging.ENVELOPED, parameters.getSignaturePackaging());
    }

    @Test
    void unsetCanonicalizationMethodsDefaultToInclusive() {
        var parameters = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        assertEquals(CanonicalizationMethod.INCLUSIVE, parameters.getInfoCanonicalization());
        assertEquals(CanonicalizationMethod.INCLUSIVE, parameters.getPropertiesCanonicalization());
        assertEquals(CanonicalizationMethod.INCLUSIVE, parameters.getKeyInfoCanonicalization());
    }

    @Test
    void unsetOrNonPositiveVisualizationWidthDefaultsTo768() {
        var unset = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 0, false);
        var negative = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, -10, false);

        assertEquals(768, unset.getVisualizationWidth());
        assertEquals(768, negative.getVisualizationWidth());
    }

    @Test
    void positiveVisualizationWidthIsKeptAsGiven() {
        var parameters = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 1024, false);

        assertEquals(1024, parameters.getVisualizationWidth());
    }

    @Test
    void anyCertificateIsAllowedWithoutRequiredTypes() {
        var parameters = SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false);

        assertTrue(parameters.isSigningCertificateAllowed(SigningCertificateType.ADESIG));
        assertTrue(parameters.isSigningCertificateAllowed(null));
    }

    @Test
    void onlyRequiredTypesAreAllowed() {
        var parameters = SigningParametersResolver.withSigningCertificateTypes(SigningParameters.buildParameters(
                SignatureProfile.BASELINE_B, SignatureForm.XAdES, DigestAlgorithm.SHA256,
                null, null, false, null, null, null, false, 640, false),
                List.of(SigningCertificateType.QESIG, SigningCertificateType.QESIG_MANDATE));

        assertTrue(parameters.isSigningCertificateAllowed(SigningCertificateType.QESIG_MANDATE));
        assertFalse(parameters.isSigningCertificateAllowed(SigningCertificateType.ADESIG_QC));
        assertFalse(parameters.isSigningCertificateAllowed(null));
    }
}