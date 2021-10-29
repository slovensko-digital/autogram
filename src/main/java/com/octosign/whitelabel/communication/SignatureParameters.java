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

    private String filename;

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

    private String transformationOutputMimeType;

    private String schema;

    private String identifier;

    private String version;

    public SignatureParameters(String filename, Format format, Level level, String fileMimeType, Container container, Packaging packaging, DigestAlgorithm digestAlgorithm, boolean en319132, CanonicalizationMethod infoCanonicalization, CanonicalizationMethod propertiesCanonicalization, CanonicalizationMethod keyInfoCanonicalization, String signaturePolicyId, String signaturePolicyContent, String transformation, String transformationOutputMimeType, String schema, String identifier, String version) {
        this.filename = filename;
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
        this.transformationOutputMimeType = transformationOutputMimeType;
        this.schema = schema;
        this.identifier = identifier;
        this.version = version;
    }


    public String getFilename() { return filename; }

    public Format getFormat() { return format; }

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

    public String getTransformationOutputMimeType() {
        return transformationOutputMimeType;
    }

    public String getSchema() {
        return schema;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getVersion() {
        return version;
    }

    public static class Builder {
        private String filename;

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

        private String transformationOutputMimeType;

        private String schema;

        private String identifier;

        private String version;

        public Builder() {}

        public Builder(SignatureParameters tp) {
            if (tp == null) return;

            this.filename = tp.filename;
            this.format = tp.format;
            this.level = tp.level;
            this.fileMimeType = tp.fileMimeType;
            this.container = tp.container;
            this.packaging = tp.packaging;
            this.digestAlgorithm = tp.digestAlgorithm;
            this.en319132 = tp.en319132;
            this.infoCanonicalization = tp.infoCanonicalization;
            this.propertiesCanonicalization = tp.propertiesCanonicalization;
            this.keyInfoCanonicalization = tp.keyInfoCanonicalization;
            this.signaturePolicyId = tp.signaturePolicyId;
            this.signaturePolicyContent = tp.signaturePolicyContent;
            this.transformation = tp.transformation;
            this.transformationOutputMimeType = tp.transformationOutputMimeType;
            this.schema = tp.schema;
            this.identifier = tp.identifier;
            this.version = tp.version;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder format(Format format) {
            this.format = format;
            return this;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Builder fileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
            return this;
        }

        public Builder container(Container container) {
            this.container = container;
            return this;
        }

        public Builder packaging(Packaging packaging) {
            this.packaging = packaging;
            return this;
        }

        public Builder digestAlgorithm(DigestAlgorithm digestAlgorithm) {
            this.digestAlgorithm = digestAlgorithm;
            return this;
        }

        public Builder en319132(boolean en319132) {
            this.en319132 = en319132;
            return this;
        }

        public Builder infoCanonicalization(CanonicalizationMethod infoCanonicalization) {
            this.infoCanonicalization = infoCanonicalization;
            return this;
        }

        public Builder propertiesCanonicalization(CanonicalizationMethod propertiesCanonicalization) {
            this.propertiesCanonicalization = propertiesCanonicalization;
            return this;
        }

        public Builder keyInfoCanonicalization(CanonicalizationMethod keyInfoCanonicalization) {
            this.keyInfoCanonicalization = keyInfoCanonicalization;
            return this;
        }

        public Builder signaturePolicyId(String signaturePolicyId) {
            this.signaturePolicyId = signaturePolicyId;
            return this;
        }

        public Builder signaturePolicyContent(String signaturePolicyContent) {
            this.signaturePolicyContent = signaturePolicyContent;
            return this;
        }

        public Builder transformation(String transformation) {
            this.transformation = transformation;
            return this;
        }

        public Builder transformationOutputMimeType(String transformationOutputMimeType) {
            this.transformationOutputMimeType = transformationOutputMimeType;
            return this;
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public SignatureParameters build() {
            return new SignatureParameters(this.filename, this.format, this.level, this.fileMimeType, this.container, this.packaging, this.digestAlgorithm, this.en319132, this.infoCanonicalization, this.propertiesCanonicalization, this.keyInfoCanonicalization, this.signaturePolicyId, this.signaturePolicyContent, this.transformation, this.transformationOutputMimeType, this.schema, this.identifier, this.version);
        }
    }
}
