package com.octosign.whitelabel.communication;

import static com.octosign.whitelabel.communication.SignatureParameters.CanonicalizationMethod.*;

/**
 * Immutable parameters for signature creation
 */
public class SignatureParameters {
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

    private final Format format;
    private final Level level;
    private final MimeType fileMimeType;
    private final Container container;
    private final String containerFilename;
    private final String containerXmlns;
    private final String identifier;
    private final Packaging packaging;
    private final DigestAlgorithm digestAlgorithm;
    private final Boolean en319132;
    private final CanonicalizationMethod infoCanonicalization;
    private final CanonicalizationMethod propertiesCanonicalization;
    private final CanonicalizationMethod keyInfoCanonicalization;
    private final String signaturePolicyId;
    private final String signaturePolicyContent;
    private final String schema;
    private final String transformation;
    private final MimeType transformationOutputMimeType;

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
