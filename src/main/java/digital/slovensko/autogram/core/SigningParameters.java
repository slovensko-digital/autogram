package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_LEVEL;

public class SigningParameters {
    // private final SignatureLevel level;
    private SignatureProfile signatureProfile;
    private SignatureForm signatureForm;
    private ASiCContainerType container;
    private final DigestAlgorithm digestAlgorithm;
    private final SignaturePackaging packaging;
    private final boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;
    private final boolean checkPDFACompliance;
    private final int visualizationWidth;
    private final TSPSource tspSource;

    private SigningParameters(
            SignatureProfile signatureProfile, SignatureForm signatureForm, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging signaturePackaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, TSPSource tspSource) {

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
        this.tspSource = tspSource;
    }

    public static SigningParameters buildParameters(
            SignatureProfile profile, SignatureForm form, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, TSPSource tspSource) throws AutogramException {

        if (profile == null)
            throw new SigningParametersException(NO_LEVEL);

        if (digestAlgorithm == null)
            digestAlgorithm = DigestAlgorithm.SHA256;

        var signingParameters = new SigningParameters(
                profile, form, digestAlgorithm, container, packaging, en319132, infoCanonicalization, propertiesCanonicalization,
            keyInfoCanonicalization, checkPDFACompliance, preferredPreviewWidth, tspSource);
        return signingParameters;
    }

    public static SigningParameters buildParameters(
            SignatureLevel level, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, TSPSource tspSource) throws AutogramException {

        return buildParameters(
                level.getSignatureProfile(),
                level.getSignatureForm(),
                digestAlgorithm,
                container,
                packaging,
                en319132,
                infoCanonicalization,
                propertiesCanonicalization,
                keyInfoCanonicalization,
                checkPDFACompliance,
                preferredPreviewWidth,
                tspSource
        );
    }

    public void setSignatureForm(SignatureForm signatureForm) {
        this.signatureForm = signatureForm;
    }

    public void setContainer(ASiCContainerType container) {
        this.container = container;
    }

    public SignatureForm getSignatureForm() {
        return signatureForm;
    }

    public ASiCContainerType getContainer() {
        return container;
    }

    public SignatureLevel getLevel() {
        if (signatureForm == null && signatureProfile == null)
            return SignatureLevel.XAdES_BASELINE_B;
        
        if (signatureForm == null)
            signatureForm = SignatureForm.XAdES;

        if (signatureProfile == null)
            signatureProfile = SignatureProfile.BASELINE_B;

        return SignatureLevel.getSignatureLevel(signatureForm, signatureProfile);
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

    public TSPSource getTspSource() {
        return tspSource;
    }

}
