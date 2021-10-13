package com.octosign.whitelabel.communication;

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

    private String fileMimeType;

    private Container container;

    private Packaging packaging;

    private DigestAlgorithm digestAlgorithm;

    private boolean en319132;

    private CanonicalizationMethod infoCanonicalization;

    private CanonicalizationMethod propertiesCanonicalization;

    private CanonicalizationMethod keyInfoCanonicalization;

    private String signaturePolicyId;

    private String signaturePolicyContent;

    private String transformation;

    private String schema;

    private String identifier;

    private String version;

    public SignatureParameters(Format format, Level level, String fileMimeType, Container container, Packaging packaging, DigestAlgorithm digestAlgorithm, boolean en319132, CanonicalizationMethod infoCanonicalization, CanonicalizationMethod propertiesCanonicalization, CanonicalizationMethod keyInfoCanonicalization, String signaturePolicyId, String signaturePolicyContent, String transformation, String schema, String identifier, String version) {
        this.format = format;
        this.level = level;
        this.fileMimeType = fileMimeType;
        this.container = container;
        this.packaging = packaging;
        this.digestAlgorithm = digestAlgorithm;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.signaturePolicyId = signaturePolicyId;
        this.signaturePolicyContent = signaturePolicyContent;
        this.transformation = transformation;
        this.schema = schema;
        this.identifier = identifier;
        this.version = version;
    }

    public Format getFormat() {
        return format;
    }

    public Level getLevel() {
        return level != null ? level : Level.BASELINE_B;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public Container getContainer() {
        return container;
    }

    public Packaging getPackaging() {
        return packaging != null ? packaging : Packaging.ENVELOPED;
    }

    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
    }

    public boolean isEn319132() {
        return en319132;
    }

    public CanonicalizationMethod getInfoCanonicalization() {
        return infoCanonicalization;
    }

    public CanonicalizationMethod getPropertiesCanonicalization() {
        return propertiesCanonicalization;
    }

    public CanonicalizationMethod getKeyInfoCanonicalization() {
        return keyInfoCanonicalization;
    }

    public String getSignaturePolicyId() {
        return signaturePolicyId;
    }

    public String getSignaturePolicyContent() {
        return signaturePolicyContent;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getSchema() {
        return schema;
    }

    public String getIdentifier() { return identifier; }

    public String getVersion() { return version; }
}
