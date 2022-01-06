package com.octosign.whitelabel.communication;

import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.INCLUSIVE;

/**
 * Immutable parameters for signature creation
 */
public class SignatureParameters {
    public enum Format {
        XADES,
        PADES
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

    public enum Packaging {
        ENVELOPED,
        ENVELOPING,
        DETACHED,
        INTERNALLY_DETACHED
    }

    public enum DigestAlgorithm {
        SHA256,
        SHA384,
        SHA512
    }

    public enum CanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE
    }

    private Format format;

    private Level level;

    private MimeType fileMimeType;

    private Container container;

    private String containerFilename;

    private String containerXmlns;

    private String identifier;

    private Packaging packaging;

    private DigestAlgorithm digestAlgorithm;

    private Boolean en319132;

    private CanonicalizationMethod infoCanonicalization;

    private CanonicalizationMethod propertiesCanonicalization;

    private CanonicalizationMethod keyInfoCanonicalization;

    private String signaturePolicyId;

    private String signaturePolicyContent;

    private String schema;

    private String transformation;

    private MimeType transformationOutputMimeType;

    public SignatureParameters(Format format, Level level, MimeType fileMimeType, Container container, String containerFilename, String containerXmlns, String identifier, Packaging packaging, DigestAlgorithm digestAlgorithm, Boolean en319132, CanonicalizationMethod infoCanonicalization, CanonicalizationMethod propertiesCanonicalization, CanonicalizationMethod keyInfoCanonicalization, String signaturePolicyId, String signaturePolicyContent, String schema, String transformation, MimeType transformationOutputMimeType) {
        this.format = format;
        this.level = level;
        this.fileMimeType = fileMimeType;
        this.container = container;
        this.containerFilename = containerFilename;
        this.containerXmlns = containerXmlns;
        this.identifier = identifier;
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

    public SignatureParameters(SignatureParameters sp) {
        this.format = sp.format;
        this.level = sp.level;
        this.fileMimeType = sp.fileMimeType;
        this.container = sp.container;
        this.containerFilename = sp.containerFilename;
        this.containerXmlns = sp.containerXmlns;
        this.identifier = sp.identifier;
        this.packaging = sp.packaging;
        this.digestAlgorithm = sp.digestAlgorithm;
        this.en319132 = sp.en319132;
        this.infoCanonicalization = sp.infoCanonicalization;
        this.propertiesCanonicalization = sp.propertiesCanonicalization;
        this.keyInfoCanonicalization = sp.keyInfoCanonicalization;
        this.signaturePolicyId = sp.signaturePolicyId;
        this.signaturePolicyContent = sp.signaturePolicyContent;
        this.schema = sp.schema;
        this.transformation = sp.transformation;
        this.transformationOutputMimeType = sp.transformationOutputMimeType;
    }

    public Format getFormat() { return format; }

    public Level getLevel() {
        return level != null ? level : Level.BASELINE_B;
    }

    public MimeType getFileMimeType() {
        return fileMimeType;
    }

    public Container getContainer() {
        return container;
    }

    public String getContainerFilename() { return containerFilename; }

    public String getContainerXmlns() { return containerXmlns; }

    public String getIdentifier() {
        return identifier;
    }

    public Packaging getPackaging() {
        return packaging != null ? packaging : Packaging.ENVELOPED;
    }

    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
    }

    public Boolean isEn319132() {
        return en319132 != null ? en319132 : true;
    }

    public CanonicalizationMethod getInfoCanonicalization() {
        return infoCanonicalization != null ? infoCanonicalization : INCLUSIVE;
    }

    public CanonicalizationMethod getPropertiesCanonicalization() {
        return propertiesCanonicalization != null ? propertiesCanonicalization : INCLUSIVE;
    }

    public CanonicalizationMethod getKeyInfoCanonicalization() {
        return keyInfoCanonicalization != null ? keyInfoCanonicalization : INCLUSIVE;
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

    public MimeType getTransformationOutputMimeType() {
        return transformationOutputMimeType;
    }

}
