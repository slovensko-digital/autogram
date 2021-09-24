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
        XADES_BASELINE_B,
        XADES_BASELINE_T,
        XADES_BASELINE_LT,
        XADES_BASELINE_LTA,

        PADES_BASELINE_B,
        PADES_BASELINE_T,
        PADES_BASELINE_LT,
        PADES_BASELINE_LTA,
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
        return level;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public Container getContainer() {
        return container;
    }

    public Packaging getPackaging() { return packaging; }

    public DigestAlgorithm getDigestAlgorithm() { return digestAlgorithm; }

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

    public String getIdentifier() {
        return identifier;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "SignatureParameters{" +
                "format=" + format +
                ", level=" + level +
                ", fileMimeType='" + fileMimeType + '\'' +
                ", container=" + container +
                ", packaging=" + packaging +
                ", digestAlgorithm=" + digestAlgorithm +
                ", en319132=" + en319132 +
                ", infoCanonicalization=" + infoCanonicalization +
                ", propertiesCanonicalization=" + propertiesCanonicalization +
                ", keyInfoCanonicalization=" + keyInfoCanonicalization +
                ", signaturePolicyId='" + signaturePolicyId + '\'' +
                ", signaturePolicyContent='" + signaturePolicyContent + '\'' +
                ", transformation='" + transformation + '\'' +
                ", schema='" + schema + '\'' +
                ", identifier='" + identifier + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public static class Builder {
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

        public Builder() {}

        public Builder(SignatureParameters tp) {
            if (tp == null) return;

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
            this.schema = tp.schema;
            this.identifier = tp.identifier;
            this.version = tp.version;
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
            return new SignatureParameters(this.format, this.level, this.fileMimeType, this.container, this.packaging, this.digestAlgorithm, this.en319132, this.infoCanonicalization, this.propertiesCanonicalization, this.keyInfoCanonicalization, this.signaturePolicyId, this.signaturePolicyContent, this.transformation, this.schema, this.identifier, this.version);
        }
    }
//    public class PrefilledBuilder extends Builder {
//        public PrefilledBuilder basedOn(SignatureParameterTemplate spt) {
//            var sp = spt.getParameters();
//
//            SignatureParameters.this.fileMimeType = sp.fileMimeType;
//            Container.this.container = sp.container;
//            this.packaging = sp.packaging;
//            this.digestAlgorithm = sp.digestAlgorithm;
//            this.en319132 = sp.en319132;
//            this.infoCanonicalization = sp.infoCanonicalization;
//            this.propertiesCanonicalization = sp.propertiesCanonicalization;
//            this.keyInfoCanonicalization = sp.keyInfoCanonicalization;
//            this.signaturePolicyId = sp.signaturePolicyId;
//            this.signaturePolicyContent = sp.signaturePolicyContent;
//            this.transformation = sp.transformation;
//            this.schema = sp.schema;
//            this.identifier = sp.identifier;
//            this.version = sp.version;
//
//            return this;
//        }
//    }
}
