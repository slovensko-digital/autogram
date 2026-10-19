package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.SigningParametersException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_LEVEL;

public class SigningParameters {
    private final SignatureProfile signatureProfile;
    private final SignatureForm signatureForm;
    private final ASiCContainerType container;
    private final DigestAlgorithm digestAlgorithm;
    private final SignaturePackaging packaging;
    private final boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;
    private final boolean checkPDFACompliance;
    private final int visualizationWidth;
    private final boolean plainXmlEnabled;

    private SigningParameters(
            SignatureProfile signatureProfile, SignatureForm signatureForm, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging signaturePackaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, boolean plainXmlEnabled) {

        this.signatureProfile = signatureProfile;
        this.signatureForm = signatureForm;
        this.digestAlgorithm = digestAlgorithm;
        this.container = container;
        this.packaging = signaturePackaging;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.checkPDFACompliance = checkPDFACompliance;
        this.visualizationWidth = preferredPreviewWidth;
        this.plainXmlEnabled = plainXmlEnabled;
    }

    static SigningParameters buildParameters(
            SignatureProfile profile, SignatureForm form, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, boolean plainXmlEnabled) throws SigningParametersException {

        if (profile == null)
            throw new SigningParametersException(NO_LEVEL);

        if (digestAlgorithm == null)
            digestAlgorithm = DigestAlgorithm.SHA256;

        var signingParameters = new SigningParameters(
                profile, form, digestAlgorithm, container, packaging, en319132, infoCanonicalization, propertiesCanonicalization,
            keyInfoCanonicalization, checkPDFACompliance, preferredPreviewWidth, plainXmlEnabled);
        return signingParameters;
    }

    public SignatureForm getSignatureForm() {
        return signatureForm;
    }

    public SignatureProfile getSignatureProfile() {
        return signatureProfile;
    }

    public ASiCContainerType getContainer() {
        return container;
    }

    public SignatureLevel getLevel() {
        if (signatureForm == null && signatureProfile == null)
            return SignatureLevel.XAdES_BASELINE_B;

        var form = signatureForm != null ? signatureForm : SignatureForm.XAdES;
        var profile = signatureProfile != null ? signatureProfile : SignatureProfile.BASELINE_B;

        return SignatureLevel.getSignatureLevel(form, profile);
    }

    public SignaturePackaging getSignaturePackaging() {
        return packaging != null ? packaging : SignaturePackaging.ENVELOPED;
    }

    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
    }

    public Boolean isEn319132() {
        return en319132;
    }

    public String getInfoCanonicalization() {
        return infoCanonicalization != null ? infoCanonicalization : CanonicalizationMethod.INCLUSIVE;
    }

    public String getPropertiesCanonicalization() {
        return propertiesCanonicalization != null ? propertiesCanonicalization : CanonicalizationMethod.INCLUSIVE;
    }

    public String getKeyInfoCanonicalization() {
        return keyInfoCanonicalization != null ? keyInfoCanonicalization : CanonicalizationMethod.INCLUSIVE;
    }

    public boolean getCheckPDFACompliance() {
        return checkPDFACompliance;
    }

    public int getVisualizationWidth() {
        return (visualizationWidth > 0) ? visualizationWidth : 768;
    }

    public boolean isPlainXmlEnabled() {
        return plainXmlEnabled;
    }
}
