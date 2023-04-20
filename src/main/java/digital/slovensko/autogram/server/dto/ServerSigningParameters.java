package digital.slovensko.autogram.server.dto;

import static digital.slovensko.autogram.server.dto.ServerSigningParameters.LocalCanonicalizationMethod.*;

import java.util.Base64;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

public class ServerSigningParameters {
    public enum LocalCanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE,
        INCLUSIVE_WITH_COMMENTS,
        EXCLUSIVE_WITH_COMMENTS,
        INCLUSIVE_11,
        INCLUSIVE_11_WITH_COMMENTS
    }

    private final ASiCContainerType container;
    private final SignatureLevel level;
    private final String containerXmlns;
    private final String schema;
    private final String transformation;
    private final SignaturePackaging packaging;
    private final DigestAlgorithm digestAlgorithm;
    private final Boolean en319132;
    private final LocalCanonicalizationMethod infoCanonicalization;
    private final LocalCanonicalizationMethod propertiesCanonicalization;
    private final LocalCanonicalizationMethod keyInfoCanonicalization;
    private final String identifier;

    public ServerSigningParameters(SignatureLevel level, ASiCContainerType container,
            String containerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm,
            Boolean en319132, LocalCanonicalizationMethod infoCanonicalization,
            LocalCanonicalizationMethod propertiesCanonicalization, LocalCanonicalizationMethod keyInfoCanonicalization,
            String schema, String transformation,
            String Identifier) {
        this.level = level;
        this.container = container;
        this.containerXmlns = containerXmlns;
        this.packaging = packaging;
        this.digestAlgorithm = digestAlgorithm;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.schema = schema;
        this.transformation = transformation;
        this.identifier = Identifier;
    }

    public SigningParameters getSigningParameters(boolean isBase64) {
        return new SigningParameters(
                getSignatureLevel(),
                getContainer(),
                containerXmlns,
                packaging,
                digestAlgorithm,
                en319132,
                getCanonicalizationMethodString(infoCanonicalization),
                getCanonicalizationMethodString(propertiesCanonicalization),
                getCanonicalizationMethodString(keyInfoCanonicalization),
                getSchema(isBase64),
                getTransformation(isBase64),
                identifier);
    }

    private String getTransformation(boolean isBase64) {
        if (transformation == null)
            return null;

        if (isBase64)
            return new String(Base64.getDecoder().decode(transformation));

        return transformation;
    }

    private String getSchema(boolean isBase64) {
        if (schema == null)
            return null;

        if (isBase64)
            return new String(Base64.getDecoder().decode(schema));

        return schema;
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
        return level;
    }

    private ASiCContainerType getContainer() {
        return container;
    }
}
