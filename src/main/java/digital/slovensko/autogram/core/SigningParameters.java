package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import digital.slovensko.autogram.util.AsicContainerUtils;
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
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.EMPTY_DOCUMENT;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_LEVEL;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.NO_MIME_TYPE;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.WRONG_MIME_TYPE;
import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.XSLT_NO_XDC;

public class SigningParameters {
    private final SignatureLevel level;
    private final DigestAlgorithm digestAlgorithm;
    private final ASiCContainerType container;
    private final SignaturePackaging packaging;
    private final boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;
    private final EFormAttributes eFormAttributes;
    private final boolean checkPDFACompliance;
    private final int visualizationWidth;
    private final TSPSource tspSource;

    private SigningParameters(
            SignatureLevel level, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging signaturePackaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            EFormAttributes eFormAttributes, boolean checkPDFACompliance, int preferredPreviewWidth, TSPSource tspSource) {

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
        this.eFormAttributes = eFormAttributes;
    }

    public static SigningParameters buildParameters(
            SignatureLevel level, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            EFormAttributes eFormAttributes, boolean autoLoadEform, String fsFormId, boolean checkPDFACompliance,
            int preferredPreviewWidth, DSSDocument document, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {

        if (level == null)
            throw new SigningParametersException(NO_LEVEL);

        if (document == null)
            throw new SigningParametersException(EMPTY_DOCUMENT);

        if (document.getMimeType() == null)
            throw new SigningParametersException(NO_MIME_TYPE);

        if (digestAlgorithm == null)
            digestAlgorithm = DigestAlgorithm.SHA256;

        var extractedDocument = document;
        if (AutogramMimeType.isAsice(document.getMimeType()))
            extractedDocument = AsicContainerUtils.getOriginalDocument(document);

        if (AutogramMimeType.isXML(extractedDocument.getMimeType()) && XDCValidator.isXDCContent(extractedDocument))
            extractedDocument.setMimeType(AutogramMimeType.XML_DATACONTAINER);

        fsFormId = EFormUtils.translateFsFormId(fsFormId);
        eFormAttributes = EFormAttributes.build(eFormAttributes, autoLoadEform, fsFormId, extractedDocument, propertiesCanonicalization);

        var extractedDocumentMimeType = extractedDocument.getMimeType();

        if (eFormAttributes.containerXmlns() != null && eFormAttributes.containerXmlns().contains("xmldatacontainer")) {
            if (container == null) container = ASiCContainerType.ASiC_E;

            if (packaging == null) packaging = SignaturePackaging.ENVELOPING;

            if (!AutogramMimeType.isXML(extractedDocumentMimeType) && !AutogramMimeType.isXDC(extractedDocumentMimeType))
                throw new SigningParametersException(WRONG_MIME_TYPE);
        }

        if (AutogramMimeType.isXDC(extractedDocumentMimeType) || AutogramMimeType.isXML(extractedDocumentMimeType)) {
            XDCValidator.validateXml(
                    eFormAttributes.schema(), eFormAttributes.transformation(), extractedDocument,
                    propertiesCanonicalization, digestAlgorithm, eFormAttributes.embedUsedSchemas());
        }

        if (!AutogramMimeType.isXDC(extractedDocumentMimeType)) {
            // if the document is not an XML resulting in XML Datacontainer, ignore all eForm attributes (mainly transformation)
            if (eFormAttributes.containerXmlns() == null || !eFormAttributes.containerXmlns().contains("xmldatacontainer")) {
                if (eFormAttributes.transformation() != null)
                    throw new SigningParametersException(XSLT_NO_XDC);

                eFormAttributes = new EFormAttributes(null, null, null, null, null, null, false);
            }
        }

        if (!plainXmlEnabled && (AutogramMimeType.isXML(extractedDocumentMimeType) || AutogramMimeType.isXDC(extractedDocumentMimeType)) && (eFormAttributes.transformation() == null))
            throw new UnknownEformException();

        return new SigningParameters(
                level, digestAlgorithm, container, packaging, en319132, infoCanonicalization, propertiesCanonicalization,
                keyInfoCanonicalization, eFormAttributes, checkPDFACompliance, preferredPreviewWidth, tspSource);
    }

    public static SigningParameters buildForPDF(DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132, TSPSource tspSource) throws AutogramException {
        return buildParameters(
                (tspSource == null) ? SignatureLevel.PAdES_BASELINE_B : SignatureLevel.PAdES_BASELINE_T, DigestAlgorithm.SHA256,
                null, null, signAsEn319132, null, null, null, null, false,
                null, checkPDFACompliance, 640, document, tspSource, true);
    }

    public static SigningParameters buildForASiCWithXAdES(DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {
        return buildParameters(
                (tspSource == null) ? SignatureLevel.XAdES_BASELINE_B : SignatureLevel.XAdES_BASELINE_T, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, signAsEn319132, null, null, null,  null, true,
                EFormUtils.getFsFormIdFromFilename(document.getName()), checkPDFACompliance, 640, document, tspSource, plainXmlEnabled);
    }

    public static SigningParameters buildForASiCWithCAdES(DSSDocument document, boolean checkPDFACompliance, boolean signAsEn319132, TSPSource tspSource, boolean plainXmlEnabled) throws AutogramException {
        return buildParameters(
                (tspSource == null) ? SignatureLevel.CAdES_BASELINE_B : SignatureLevel.CAdES_BASELINE_T, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING, signAsEn319132, null, null, null, null, true,
                EFormUtils.getFsFormIdFromFilename(document.getName()), checkPDFACompliance, 640, document, tspSource, plainXmlEnabled);
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
        return container;
    }

    public String getContainerXmlns() {
        return eFormAttributes.containerXmlns();
    }

    public String getSchema() {
        return eFormAttributes.schema();
    }

    public String getTransformation() {
        return eFormAttributes.transformation();
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

    public String getIdentifier() {
        return eFormAttributes.identifier();
    }

    public boolean getCheckPDFACompliance() {
        return checkPDFACompliance;
    }

    public int getVisualizationWidth() {
        return (visualizationWidth > 0) ? visualizationWidth : 768;
    }

    public String getXsltDestinationType() {
        return eFormAttributes.xsltParams().destinationType();
    }

    public String getXsdIdentifier() {
        return eFormAttributes.xsdIdentifier();
    }

    public boolean shouldCreateXdc() {
        return eFormAttributes.containerXmlns() != null && eFormAttributes.containerXmlns().contains("xmldatacontainer");
    }

    public XsltParams getXsltParams() {
        return eFormAttributes.xsltParams();
    }

    public TSPSource getTspSource() {
        return tspSource;
    }

    public boolean shouldEmbedSchemas() {
        return eFormAttributes.embedUsedSchemas();
    }
}
