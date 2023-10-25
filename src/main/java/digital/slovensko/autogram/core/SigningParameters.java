package digital.slovensko.autogram.core;

import java.util.ArrayList;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.MultipleOriginalDocumentsFoundException;
import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import digital.slovensko.autogram.core.eforms.EFormAttributes;
import digital.slovensko.autogram.core.eforms.EFormResources;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public class SigningParameters {
    private final ASiCContainerType asicContainer;
    private final String containerXmlns;
    private final String schema;
    private final String transformation;
    private final SignatureLevel level;
    private final SignaturePackaging packaging;
    private final DigestAlgorithm digestAlgorithm;
    private final Boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;
    private final String identifier;
    private final boolean checkPDFACompliance;
    private final int visualizationWidth;
    private final boolean autoLoadEform;

    private SigningParameters(SignatureLevel level, ASiCContainerType container,
            String containerXmlns, SignaturePackaging packaging, DigestAlgorithm digestAlgorithm,
            Boolean en319132, String infoCanonicalization, String propertiesCanonicalization,
            String keyInfoCanonicalization, String schema, String transformation, String identifier,
            boolean checkPDFACompliance, int preferredPreviewWidth, boolean autoLoadEform) {
        this.level = level;
        this.asicContainer = container;
        this.containerXmlns = containerXmlns;
        this.packaging = packaging;
        this.digestAlgorithm = digestAlgorithm;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.schema = schema;
        this.transformation = transformation;
        this.identifier = identifier;
        this.checkPDFACompliance = checkPDFACompliance;
        this.visualizationWidth = preferredPreviewWidth;
        this.autoLoadEform = autoLoadEform;
    }

    public ASiCWithXAdESSignatureParameters getASiCWithXAdESSignatureParameters() {
        var parameters = new ASiCWithXAdESSignatureParameters();

        parameters.aSiC().setContainerType(getContainer());
        parameters.setSignatureLevel(getLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setSigningCertificateDigestMethod(getDigestAlgorithm());
        parameters.setSignedInfoCanonicalizationMethod(getInfoCanonicalization());
        parameters.setSignedPropertiesCanonicalizationMethod(getPropertiesCanonicalization());
        parameters.setKeyInfoCanonicalizationMethod(getKeyInfoCanonicalization());
        parameters.setEn319132(isEn319132());
        parameters.setAddX509SubjectName(true);

        return parameters;
    }

    public XAdESSignatureParameters getXAdESSignatureParameters() {
        var parameters = new XAdESSignatureParameters();

        parameters.setSignatureLevel(getLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319132(isEn319132());
        parameters.setSignedInfoCanonicalizationMethod(getInfoCanonicalization());
        parameters.setSignedPropertiesCanonicalizationMethod(getPropertiesCanonicalization());
        parameters.setSignaturePackaging(getSignaturePackaging());
        parameters.setKeyInfoCanonicalizationMethod(getKeyInfoCanonicalization());
        parameters.setAddX509SubjectName(true);

        return parameters;
    }

    public CAdESSignatureParameters getCAdESSignatureParameters() {
        var parameters = new CAdESSignatureParameters();

        parameters.setSignatureLevel(getLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
        parameters.setEn319122(isEn319132());

        return parameters;
    }

    public PAdESSignatureParameters getPAdESSignatureParameters() {
        var parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(getLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319122(isEn319132());

        return parameters;
    }

    public ASiCWithCAdESSignatureParameters getASiCWithCAdESSignatureParameters() {
        var parameters = new ASiCWithCAdESSignatureParameters();

        parameters.setSignatureLevel(getLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319122(isEn319132());
        parameters.aSiC().setContainerType(getContainer());

        return parameters;
    }

    public SignatureForm getSignatureType() {
        return level.getSignatureForm();
    }

    public ASiCContainerType getContainer() {
        return asicContainer;
    }

    public String getContainerXmlns() {
        return containerXmlns;
    }

    public String getSchema() {
        return schema;
    }

    public String getTransformation() {
        return transformation;
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
        return en319132 != null ? en319132 : false;
    }

    public String getInfoCanonicalization() {
        return infoCanonicalization != null ? infoCanonicalization
                : CanonicalizationMethod.INCLUSIVE;
    }

    public String getPropertiesCanonicalization() {
        return propertiesCanonicalization != null ? propertiesCanonicalization
                : CanonicalizationMethod.INCLUSIVE;
    }

    public String getKeyInfoCanonicalization() {
        return keyInfoCanonicalization != null ? keyInfoCanonicalization
                : CanonicalizationMethod.INCLUSIVE;
    }

    public static EFormAttributes tryToLoadEFormAttributes(DSSDocument document, String propertiesCanonicalization) throws AutogramException {
        if (isAsice(document.getMimeType()))
            try {
                document = AsicContainerUtils.getOriginalDocument(document);
            } catch (MultipleOriginalDocumentsFoundException | OriginalDocumentNotFoundException e) {
                return null;
            }

        if (!isXDC(document.getMimeType()) && !isXML(document.getMimeType()))
            return null;

        EFormResources eformResources;
        if (isXDC(document.getMimeType()) || EFormUtils.isXDCContent(document))
            eformResources = EFormResources.buildEFormResourcesFromXDC(document, propertiesCanonicalization);
        else
            eformResources = EFormResources.buildEFormResourcesFromEformXml(document, propertiesCanonicalization);

        if (eformResources == null)
            return null;

        var transformation = eformResources.findTransformation();
        var schema = eformResources.findSchema();
        var identifier = eformResources.getIdentifier();
        var containerXmlns = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";

        return new EFormAttributes(identifier, transformation, schema, containerXmlns);
    }

    public static SigningParameters buildFromRequest(SignatureLevel level, ASiCContainerType container,
            String containerXmlns, SignaturePackaging packaging, DigestAlgorithm digestAlgorithm,
            Boolean en319132, String infoCanonicalization, String propertiesCanonicalization,
            String keyInfoCanonicalization, String schema, String transformation, String identifier,
            boolean checkPDFACompliance, int preferredPreviewWidth, boolean autoLoadEform, DSSDocument document) throws AutogramException {

        return buildParameters(level, container, containerXmlns, packaging, digestAlgorithm, en319132,
                infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization, schema, transformation,
                identifier, checkPDFACompliance, preferredPreviewWidth, autoLoadEform, document);
    }

    private static SigningParameters buildParameters(SignatureLevel level, ASiCContainerType container,
            String containerXmlns, SignaturePackaging packaging, DigestAlgorithm digestAlgorithm,
            Boolean en319132, String infoCanonicalization, String propertiesCanonicalization,
            String keyInfoCanonicalization, String schema, String transformation, String identifier,
            boolean checkPDFACompliance, int preferredPreviewWidth, boolean autoLoadEform, DSSDocument document) throws AutogramException {

        if (autoLoadEform) {
            var eformAttributes = tryToLoadEFormAttributes(document, propertiesCanonicalization);

            if (eformAttributes != null) {
                schema = eformAttributes.getSchema();
                transformation = eformAttributes.getTransformation();
                identifier = eformAttributes.getIdentifier();
                containerXmlns = eformAttributes.getContainerXmlns();
            }
        }

        return new SigningParameters(level, container, containerXmlns, packaging, digestAlgorithm, en319132,
                infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization, schema, transformation,
                identifier, checkPDFACompliance, preferredPreviewWidth, autoLoadEform);
    }

    public static SigningParameters buildForPDF(String filename, DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132) throws AutogramException {
        return buildParameters(
                SignatureLevel.PAdES_BASELINE_B,
                null,
                null, null,
                DigestAlgorithm.SHA256,
                signAsEn319132, null,
                null, null,
                null, null, "", checkPDFACompliance, 640, false, document);
    }

    public static SigningParameters buildForASiCWithXAdES(String filename, DSSDocument document, boolean signAsEn319132) throws AutogramException {
        return buildParameters(SignatureLevel.XAdES_BASELINE_B, ASiCContainerType.ASiC_E,
                null, SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256, signAsEn319132, null, null,
                null, null, null, "", false, 640, true, document);
    }

    public static SigningParameters buildForASiCWithCAdES(String filename, DSSDocument document, boolean signAsEn319132) throws AutogramException {
        return buildParameters(SignatureLevel.CAdES_BASELINE_B, ASiCContainerType.ASiC_E,
                null, SignaturePackaging.ENVELOPING, DigestAlgorithm.SHA256, signAsEn319132, null, null,
                null, null, null, "", false, 640, true, document);
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean shouldCreateDatacontainer() {
        return getContainerXmlns() != null && getContainerXmlns().contains("xmldatacontainer");
    }

    public boolean getCheckPDFACompliance() {
        return checkPDFACompliance;
    }

    public int getVisualizationWidth() {
        return (visualizationWidth > 0) ? visualizationWidth : 768;
    }

    public boolean getAutoLoadEform() {
        return autoLoadEform;
    }

    public String extractTransformationOutputMimeTypeString() throws TransformationParsingErrorException {
        var mimeType = EFormUtils.extractTransformationOutputMimeTypeString(transformation);
        if (!new ArrayList<String>() {
            {
                add("HTML");
                add("TXT");
            }
        }.contains(mimeType))
            throw new TransformationParsingErrorException("Unsupported transformation output method: " + mimeType);

        return mimeType;
    }
}
