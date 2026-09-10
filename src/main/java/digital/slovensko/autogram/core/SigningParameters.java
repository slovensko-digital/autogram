package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.EMPTY_DOCUMENT;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_LEVEL;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_MIME_TYPE;

public class SigningParameters {
    private final SignatureLevel level;
    private final DigestAlgorithm digestAlgorithm;
    private final ASiCContainerType container;
    private final SignaturePackaging packaging;
    private final boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;
    private final boolean checkPDFACompliance;
    private final int visualizationWidth;
    private final TSPSource tspSource;

    private SigningParameters(
            SignatureLevel level, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging signaturePackaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, TSPSource tspSource) {

        this.level = level;
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
            SignatureLevel level, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            EFormAttributes eFormAttributes, MimeType documentMimeType, boolean checkPDFACompliance,
            int preferredPreviewWidth, TSPSource tspSource) throws AutogramException {

        if (level == null)
            throw new SigningParametersException(NO_LEVEL);

        if (documentMimeType == null)
            throw new SigningParametersException(NO_MIME_TYPE);

        if (digestAlgorithm == null)
            digestAlgorithm = DigestAlgorithm.SHA256;

        if (eFormAttributes.containerXmlns() != null && eFormAttributes.containerXmlns().contains("xmldatacontainer")) {
            if (container == null) container = ASiCContainerType.ASiC_E;

            if (packaging == null) packaging = SignaturePackaging.ENVELOPING;
        }

        var signingParameters = new SigningParameters(
                level, digestAlgorithm, container, packaging, en319132, infoCanonicalization, propertiesCanonicalization,
            keyInfoCanonicalization, checkPDFACompliance, preferredPreviewWidth, tspSource);
        return signingParameters;
    }

        public record PreparedParameters(SigningParameters signingParameters, EFormAttributes eFormAttributes) {
        }

    public static SigningParameters buildForPDF(DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132, TSPSource tspSource) throws AutogramException {
        return prepareForPDF(document, checkPDFACompliance, signAsEn319132, tspSource).signingParameters();
        }

        public static PreparedParameters prepareForPDF(DSSDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource) throws AutogramException {
        return prepareParameters(
                (tspSource == null) ? SignatureLevel.PAdES_BASELINE_B : SignatureLevel.PAdES_BASELINE_T, DigestAlgorithm.SHA256,
                null, null, signAsEn319132, null, null, null, null, false,
            null, checkPDFACompliance, 640, document, tspSource, true);
    }

    public static SigningParameters buildForASiCWithXAdES(DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {
        return prepareForASiCWithXAdES(document, checkPDFACompliance, signAsEn319132, tspSource, plainXmlEnabled)
            .signingParameters();
        }

        public static PreparedParameters prepareForASiCWithXAdES(DSSDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {
        return prepareParameters(
                (tspSource == null) ? SignatureLevel.XAdES_BASELINE_B : SignatureLevel.XAdES_BASELINE_T, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, signAsEn319132, null, null, null,  null, true,
                EFormUtils.getFsFormIdFromFilename(document.getName()), checkPDFACompliance, 640, document, tspSource,
            plainXmlEnabled);
    }

    public static SigningParameters buildForASiCWithCAdES(DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {
        return prepareForASiCWithCAdES(document, checkPDFACompliance, signAsEn319132, tspSource, plainXmlEnabled)
            .signingParameters();
        }

        public static PreparedParameters prepareForASiCWithCAdES(DSSDocument document, boolean checkPDFACompliance,
            boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {
        return prepareParameters(
                (tspSource == null) ? SignatureLevel.CAdES_BASELINE_B : SignatureLevel.CAdES_BASELINE_T, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, signAsEn319132, null, null, null, null, true,
                EFormUtils.getFsFormIdFromFilename(document.getName()), checkPDFACompliance, 640, document, tspSource,
            plainXmlEnabled);
    }

    public static PreparedParameters prepareParameters(
            SignatureLevel level, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            EFormAttributes eFormAttributes, boolean autoLoadEform, String fsFormId, boolean checkPDFACompliance,
            int preferredPreviewWidth, DSSDocument document, TSPSource tspSource, boolean plainXmlEnabled)
            throws AutogramException {
        if (document == null)
            throw new SigningParametersException(EMPTY_DOCUMENT);

        var resolvedDigestAlgorithm = digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
        var preparedDocument = AutogramDocument.fromDssDocument(document).prepareEFormAttributes(eFormAttributes,
            autoLoadEform, fsFormId, propertiesCanonicalization, resolvedDigestAlgorithm, plainXmlEnabled);

        var signingParameters = buildParameters(level, resolvedDigestAlgorithm, container, packaging, en319132,
            infoCanonicalization,
            propertiesCanonicalization, keyInfoCanonicalization, preparedDocument.eFormAttributes(),
            preparedDocument.mimeType(), checkPDFACompliance, preferredPreviewWidth, tspSource);
        return new PreparedParameters(signingParameters, preparedDocument.eFormAttributes());
    }

    public SignatureForm getSignatureType() {
        return level.getSignatureForm();
    }

    public ASiCContainerType getContainer() {
        return container;
    }

    public SignatureLevel getLevel() {
        return level != null ? level : SignatureLevel.XAdES_BASELINE_B;
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
