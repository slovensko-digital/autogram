package digital.slovensko.autogram.core;

import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;

public final class DssSigningParametersFactory {
    private DssSigningParametersFactory() {
    }

    public static ASiCWithXAdESSignatureParameters createASiCWithXAdESSignatureParameters(SigningInput input) {
        var parameters = input.getParameters();
        var signatureParameters = new ASiCWithXAdESSignatureParameters();

        signatureParameters.aSiC().setContainerType(parameters.getContainer());
        signatureParameters.setSignatureLevel(parameters.getLevel());
        signatureParameters.setDigestAlgorithm(parameters.getDigestAlgorithm());
        signatureParameters.setSigningCertificateDigestMethod(parameters.getDigestAlgorithm());
        signatureParameters.setSignedInfoCanonicalizationMethod(parameters.getInfoCanonicalization());
        signatureParameters.setSignedPropertiesCanonicalizationMethod(parameters.getPropertiesCanonicalization());
        signatureParameters.setKeyInfoCanonicalizationMethod(parameters.getKeyInfoCanonicalization());
        signatureParameters.setEn319132(parameters.isEn319132());
        signatureParameters.setAddX509SubjectName(true);

        return signatureParameters;
    }

    public static XAdESSignatureParameters createXAdESSignatureParameters(SigningInput input) {
        var parameters = input.getParameters();
        var signatureParameters = new XAdESSignatureParameters();

        signatureParameters.setSignatureLevel(parameters.getLevel());
        signatureParameters.setDigestAlgorithm(parameters.getDigestAlgorithm());
        signatureParameters.setEn319132(parameters.isEn319132());
        signatureParameters.setSignedInfoCanonicalizationMethod(parameters.getInfoCanonicalization());
        signatureParameters.setSignedPropertiesCanonicalizationMethod(parameters.getPropertiesCanonicalization());
        signatureParameters.setSignaturePackaging(parameters.getSignaturePackaging());
        signatureParameters.setKeyInfoCanonicalizationMethod(parameters.getKeyInfoCanonicalization());
        signatureParameters.setAddX509SubjectName(true);

        return signatureParameters;
    }

    public static CAdESSignatureParameters createCAdESSignatureParameters(SigningInput input) {
        var parameters = input.getParameters();
        var signatureParameters = new CAdESSignatureParameters();

        signatureParameters.setSignatureLevel(parameters.getLevel());
        signatureParameters.setDigestAlgorithm(parameters.getDigestAlgorithm());
        signatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
        signatureParameters.setEn319122(parameters.isEn319132());

        return signatureParameters;
    }

    public static PAdESSignatureParameters createPAdESSignatureParameters(SigningInput input) {
        var parameters = input.getParameters();
        var signatureParameters = new PAdESSignatureParameters();

        signatureParameters.setSignatureLevel(parameters.getLevel());
        signatureParameters.setDigestAlgorithm(parameters.getDigestAlgorithm());
        signatureParameters.setEn319122(parameters.isEn319132());

        return signatureParameters;
    }

    public static ASiCWithCAdESSignatureParameters createASiCWithCAdESSignatureParameters(SigningInput input) {
        var parameters = input.getParameters();
        var signatureParameters = new ASiCWithCAdESSignatureParameters();

        signatureParameters.setSignatureLevel(parameters.getLevel());
        signatureParameters.setDigestAlgorithm(parameters.getDigestAlgorithm());
        signatureParameters.setEn319122(parameters.isEn319132());
        signatureParameters.aSiC().setContainerType(parameters.getContainer());

        return signatureParameters;
    }
}