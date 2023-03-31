package digital.slovensko.autogram.server.dto;

import static digital.slovensko.autogram.server.dto.ServerSigningParameters.LocalCanonicalizationMethod.*;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

public class ServerSigningParameters {
    private enum Format {
        XADES,
        PADES,
        CADES
    }

    private enum Level {
        BASELINE_B,
        BASELINE_T,
        BASELINE_LT,
        BASELINE_LTA
    }

    private enum Container {
        ASICS,
        ASICE
    }

    public enum LocalCanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE,
        INCLUSIVE_WITH_COMMENTS,
        EXCLUSIVE_WITH_COMMENTS,
        INCLUSIVE_11,
        INCLUSIVE_11_WITH_COMMENTS
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
    private final LocalCanonicalizationMethod infoCanonicalization;
    private final LocalCanonicalizationMethod propertiesCanonicalization;
    private final LocalCanonicalizationMethod keyInfoCanonicalization;

    public ServerSigningParameters(Format format, Level level, String fileMimeType, Container container,
            String containerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm,
            Boolean en319132, LocalCanonicalizationMethod infoCanonicalization,
            LocalCanonicalizationMethod propertiesCanonicalization, LocalCanonicalizationMethod keyInfoCanonicalization,
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

    public SigningParameters getSigningParameters() {
        return new SigningParameters(
                getSignatureLevel(), getContainer(),
                containerFilename, containerXmlns, packaging,
                digestAlgorithm,
                en319132,
                getCanonicalizationMethodString(infoCanonicalization),
                getCanonicalizationMethodString(propertiesCanonicalization),
                getCanonicalizationMethodString(keyInfoCanonicalization),
                signaturePolicyId, signaturePolicyContent, schema, transformation);
    }

    private static String getCanonicalizationMethodString(LocalCanonicalizationMethod method) {
        if (method == INCLUSIVE)
            return CanonicalizationMethod.INCLUSIVE;
        if (method == EXCLUSIVE)
            return CanonicalizationMethod.EXCLUSIVE;
        if (method == INCLUSIVE_WITH_COMMENTS)
            return CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS;
        if (method == EXCLUSIVE_WITH_COMMENTS)
            return CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;
        if (method == INCLUSIVE_11)
            return CanonicalizationMethod.INCLUSIVE_11;
        if (method == INCLUSIVE_11_WITH_COMMENTS)
            return CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS;

        return null;
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

    private ASiCContainerType getContainer() {
        if (container == Container.ASICE)
            return ASiCContainerType.ASiC_E;

        if (container == Container.ASICS)
            return ASiCContainerType.ASiC_S;

        return null;
    }

    private Level getLevel() {
        return level != null ? level : Level.BASELINE_B;
    }
}
