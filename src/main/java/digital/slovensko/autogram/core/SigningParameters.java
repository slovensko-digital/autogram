package digital.slovensko.autogram.core;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import com.octosign.whitelabel.error_handling.MalformedMimetypeException;

import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;

public class SigningParameters {
    public static enum SignatureType {
        XADES,
        PADES,
        CADES,
        ASIC_XADES
    }

    public enum Format {
        XADES,
        PADES,
        CADES
    }

    public enum Level {
        BASELINE_B,
        BASELINE_T,
        BASELINE_LT,
        BASELINE_LTA
    }

    public enum Container {
        ASICS,
        ASICE
    }

    private final Container container;
    private final Format format;
    private final String fileMimeType;
    private final String transformationOutputMimeType;
    private final String containerFilename;
    private final String containerXmlns;
    private final String schema;
    private final String signaturePolicyContent;
    private final String signaturePolicyId;
    private final String transformation;

    private final Level level;
    private final SignaturePackaging packaging;
    private final DigestAlgorithm digestAlgorithm;
    private final Boolean en319132;
    private final String infoCanonicalization;
    private final String propertiesCanonicalization;
    private final String keyInfoCanonicalization;

    public SigningParameters(Format format, Level level, String fileMimeType, Container container,
            String containerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm,
            Boolean en319132, String infoCanonicalization,
            String propertiesCanonicalization, String keyInfoCanonicalization,
            String signaturePolicyId, String signaturePolicyContent, String schema, String transformation,
            String transformationOutputMimeType) {
        this.format = format;
        this.level = level;
        this.fileMimeType = fileMimeType;
        this.container = container;
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
    }

    public ASiCWithXAdESSignatureParameters getASiCWithXAdESSignatureParameters() {
        var parameters = new ASiCWithXAdESSignatureParameters();

        parameters.aSiC().setContainerType(getContainer());
        parameters.setSignatureLevel(getSignatureLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setSigningCertificateDigestMethod(getDigestAlgorithm());
        parameters.setSignedInfoCanonicalizationMethod(getInfoCanonicalization());
        parameters.setSignedPropertiesCanonicalizationMethod(getPropertiesCanonicalization());
        parameters.setEn319132(isEn319132());

        return parameters;
    }

    private SignatureLevel getSignatureLevel() {
        switch (format) {
            case XADES:
                switch (getLevel()) {
                    case BASELINE_B:
                        return SignatureLevel.XAdES_BASELINE_B;
                    case BASELINE_T:
                        return SignatureLevel.XAdES_BASELINE_T;
                    case BASELINE_LT:
                        return SignatureLevel.XAdES_BASELINE_LT;
                    case BASELINE_LTA:
                        return SignatureLevel.XAdES_BASELINE_LTA;
                }
            case PADES:
                switch (getLevel()) {
                    case BASELINE_B:
                        return SignatureLevel.PAdES_BASELINE_B;
                    case BASELINE_T:
                        return SignatureLevel.PAdES_BASELINE_T;
                    case BASELINE_LT:
                        return SignatureLevel.PAdES_BASELINE_LT;
                    case BASELINE_LTA:
                        return SignatureLevel.PAdES_BASELINE_LTA;
                }
            case CADES:
                switch (getLevel()) {
                    case BASELINE_B:
                        return SignatureLevel.CAdES_BASELINE_B;
                    case BASELINE_T:
                        return SignatureLevel.CAdES_BASELINE_T;
                    case BASELINE_LT:
                        return SignatureLevel.CAdES_BASELINE_LT;
                    case BASELINE_LTA:
                        return SignatureLevel.CAdES_BASELINE_LTA;
                }
        }

        return null;
    }

    public XAdESSignatureParameters getXAdESSignatureParameters() {
        var parameters = new XAdESSignatureParameters();

        parameters.setSignatureLevel(getSignatureLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319132(isEn319132());
        parameters.setSignedInfoCanonicalizationMethod(getInfoCanonicalization());
        parameters.setSignedPropertiesCanonicalizationMethod(getPropertiesCanonicalization());

        return parameters;
    }

    public PAdESSignatureParameters getPAdESSignatureParameters() {
        var parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(getSignatureLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319122(isEn319132());

        return parameters;
    }

    public CAdESSignatureParameters getCAdESSignatureParameters() {
        var parameters = new CAdESSignatureParameters();

        parameters.setSignatureLevel(getSignatureLevel());
        parameters.setDigestAlgorithm(getDigestAlgorithm());
        parameters.setEn319122(isEn319132());

        return parameters;
    }

    public Format getFormat() {
        return format;
    }

    public MimeType getFileMimeType() throws MalformedMimetypeException {
        return new MimeType(fileMimeType);
    }

    public ASiCContainerType getContainer() {
        if (container == Container.ASICE)
            return ASiCContainerType.ASiC_E;

        if (container == Container.ASICS)
            return ASiCContainerType.ASiC_S;

        return null;
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

    public MimeType getTransformationOutputMimeType() throws MalformedMimetypeException {
        return new MimeType(transformationOutputMimeType);
    }

    public Level getLevel() {
        return level != null ? level : Level.BASELINE_B;
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

    public SignatureType getSignatureType() {
        switch (format) {
            case XADES:
                if (container != null)
                    return SignatureType.ASIC_XADES;
                else
                    return SignatureType.XADES;
            case PADES:
                return SignatureType.PADES;
            case CADES:
                return SignatureType.CADES;
        }

        return null;
    }
}
