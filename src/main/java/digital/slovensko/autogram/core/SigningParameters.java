package digital.slovensko.autogram.core;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;

public class SigningParameters {
    public enum SignatureType {
        ASIC_XADES,
        XADES,
        ASIC_CADES,
        PADES
    }

    private final ASiCContainerType asicContainer;
    private final String containerFilename;
    private final String containerXmlns;
    private final String schema;
    private final String signaturePolicyContent;
    private final String signaturePolicyId;
    private final String transformation;
    private final MimeType transformationOutputMimeType;
    private final SignatureLevel level;
    private final SignaturePackaging packaging;
    private final DigestAlgorithm digestAlgorithm;
    private final Boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;
    private final String identifier;
    private final MimeType fileMimeType;

    public SigningParameters() {
        asicContainer = ASiCContainerType.ASiC_E;
        containerFilename = "document.asice";
        containerXmlns = "";
        schema = "";
        signaturePolicyContent = "Dont't be evil.";
        signaturePolicyId = "";
        transformation = "";
        level = SignatureLevel.XAdES_BASELINE_B;
        packaging = SignaturePackaging.ENVELOPING;
        digestAlgorithm = DigestAlgorithm.SHA256;
        en319132 = false;
        infoCanonicalization = CanonicalizationMethod.INCLUSIVE;
        propertiesCanonicalization = CanonicalizationMethod.INCLUSIVE;
        keyInfoCanonicalization = CanonicalizationMethod.INCLUSIVE;
        transformationOutputMimeType = null;
        identifier = "";
        fileMimeType = null;
    }

    public SigningParameters(SignatureLevel level, ASiCContainerType container,
            String containerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm,
            Boolean en319132, String infoCanonicalization,
            String propertiesCanonicalization, String keyInfoCanonicalization,
            String signaturePolicyId, String signaturePolicyContent, String schema, String transformation, MimeType transformationOutputMimeType, String identifier, MimeType fileMimeType) {
        this.level = level;
        this.asicContainer = container;
        this.containerFilename = containerFilename;
        this.containerXmlns = containerXmlns;
        this.packaging = packaging;
        this.digestAlgorithm = digestAlgorithm;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.signaturePolicyId = signaturePolicyId;
        this.signaturePolicyContent = signaturePolicyContent;
        this.schema = schema;
        this.transformation = transformation;
        this.transformationOutputMimeType = transformationOutputMimeType;
        this.identifier = identifier;
        this.fileMimeType = fileMimeType;
    }

    public MimeType getTransformationOutputMimeType() {
        return transformationOutputMimeType;
    }

    public ASiCWithXAdESSignatureParameters getASiCWithXAdESSignatureParameters() {
        var parameters = new ASiCWithXAdESSignatureParameters();

        parameters.aSiC().setContainerType(getContainer());
        parameters.setSignatureLevel(level);
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setSigningCertificateDigestMethod(getDigestAlgorithm());
        parameters.setSignedInfoCanonicalizationMethod(getInfoCanonicalization());
        parameters.setSignedPropertiesCanonicalizationMethod(getPropertiesCanonicalization());
        parameters.setEn319132(isEn319132());
        parameters.setSignaturePackaging(packaging);

        return parameters;
    }

    public XAdESSignatureParameters getXAdESSignatureParameters() {
        var parameters = new XAdESSignatureParameters();

        parameters.setSignatureLevel(level);
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319132(isEn319132());
        parameters.setSignedInfoCanonicalizationMethod(getInfoCanonicalization());
        parameters.setSignedPropertiesCanonicalizationMethod(getPropertiesCanonicalization());

        parameters.setSignaturePackaging(getSignaturePackaging());

        return parameters;
    }

    public PAdESSignatureParameters getPAdESSignatureParameters() {
        var parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(level);
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319122(isEn319132());

        return parameters;
    }

    public ASiCWithCAdESSignatureParameters getASiCWithCAdESSignatureParameters() {
        var parameters = new ASiCWithCAdESSignatureParameters();

        parameters.setSignatureLevel(level);
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319122(isEn319132());
        parameters.aSiC().setContainerType(getContainer());

        return parameters;
    }

    public SignatureType getSignatureType() {
        if (getContainer() != null) {
            switch (getLevel()) {
                case CAdES_BASELINE_B:
                case CAdES_BASELINE_T:
                case CAdES_BASELINE_LT:
                case CAdES_BASELINE_LTA:
                    return SignatureType.ASIC_CADES;
                case XAdES_BASELINE_B:
                case XAdES_BASELINE_T:
                case XAdES_BASELINE_LT:
                case XAdES_BASELINE_LTA:
                    return SignatureType.ASIC_XADES;
                default:
                    throw new IllegalArgumentException("Unknown signature level: " + getLevel());
            }
        } else if (getLevel() != null) {
            switch (getLevel()) {
                case XAdES_BASELINE_B:
                case XAdES_BASELINE_T:
                case XAdES_BASELINE_LT:
                case XAdES_BASELINE_LTA:
                    return SignatureType.XADES;
                case PAdES_BASELINE_B:
                case PAdES_BASELINE_T:
                case PAdES_BASELINE_LT:
                case PAdES_BASELINE_LTA:
                    return SignatureType.PADES;
                default:
                    throw new IllegalArgumentException("Unknown signature level: " + getLevel());
            }
        } else {
            throw new IllegalArgumentException("Unknown signature type");
        }
    }

    public ASiCContainerType getContainer() {
        return asicContainer;
    }

    public String getContainerFilename() {
        return containerFilename;
    }

    public String getContainerXmlns() {
        return containerXmlns;
    }

    public String getSignaturePolicyId() {
        return signaturePolicyId;
    }

    public String getSignaturePolicyContent() {
        return signaturePolicyContent;
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
        return en319132 != null ? en319132 : true;
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

    public static SigningParameters buildForPDF() {
        return new SigningParameters(
                SignatureLevel.PAdES_BASELINE_B,
                null,
                null, null,
                null,
                DigestAlgorithm.SHA256,
                false, null,
                null, null,
                null, null, null, null, null, "", null
        );
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean shouldCreateDatacontainer() {
        return getContainerXmlns() != null && getContainerXmlns().contains("xmldatacontainer");
    }
}
