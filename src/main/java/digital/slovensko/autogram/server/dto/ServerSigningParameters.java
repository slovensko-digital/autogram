package digital.slovensko.autogram.server.dto;

import java.util.Arrays;
import java.util.Base64;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.server.errors.UnsupportedSignatureLevelExceptionError;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.DSSDocument;

import java.nio.charset.StandardCharsets;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public class ServerSigningParameters {
    public enum LocalCanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE,
        INCLUSIVE_WITH_COMMENTS,
        EXCLUSIVE_WITH_COMMENTS,
        INCLUSIVE_11,
        INCLUSIVE_11_WITH_COMMENTS
    }

    public enum VisualizationWidthEnum {
        sm,
        md,
        lg,
        xl,
        xxl
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
    private final boolean checkPDFACompliance;
    private final VisualizationWidthEnum visualizationWidth;
    private final boolean autoLoadEform;

    public ServerSigningParameters(SignatureLevel level, ASiCContainerType container,
            String containerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm,
            Boolean en319132, LocalCanonicalizationMethod infoCanonicalization,
            LocalCanonicalizationMethod propertiesCanonicalization, LocalCanonicalizationMethod keyInfoCanonicalization,
            String schema, String transformation,
            String Identifier, boolean checkPDFACompliance, VisualizationWidthEnum preferredPreviewWidth, boolean autoLoadEform) {
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
        this.checkPDFACompliance = checkPDFACompliance;
        this.visualizationWidth = preferredPreviewWidth;
        this.autoLoadEform = autoLoadEform;
    }

    public SigningParameters getSigningParameters(boolean isBase64, DSSDocument document) {
        return SigningParameters.buildFromRequest(
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
                identifier, checkPDFACompliance, getVisualizationWidth(), autoLoadEform, document);
    }

    private String getTransformation(boolean isBase64) throws MalformedBodyException {
        if (transformation == null)
            return null;

        if (!isBase64)
            return transformation;

        try {
            return new String(Base64.getDecoder().decode(transformation), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new MalformedBodyException("XML validation failed", "Invalid XSLT");
        }
    }

    private String getSchema(boolean isBase64) throws MalformedBodyException {
        if (schema == null)
            return null;

        if (!isBase64)
            return schema;

        try {
            return new String(Base64.getDecoder().decode(schema), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new MalformedBodyException("XML validation failed", "Invalid XSD");
        }
    }

    private static String getCanonicalizationMethodString(LocalCanonicalizationMethod method) {
        if (method == null)
            return null;

        return switch (method) {
            case INCLUSIVE -> CanonicalizationMethod.INCLUSIVE;
            case EXCLUSIVE -> CanonicalizationMethod.EXCLUSIVE;
            case INCLUSIVE_WITH_COMMENTS -> CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS;
            case EXCLUSIVE_WITH_COMMENTS -> CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;
            case INCLUSIVE_11 -> CanonicalizationMethod.INCLUSIVE_11;
            case INCLUSIVE_11_WITH_COMMENTS -> CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS;
        };
    }

    private int getVisualizationWidth() {
        if (visualizationWidth == null)
            return 0;

        return switch (visualizationWidth) {
            case sm -> 640;
            case md -> 768;
            case lg -> 1024;
            case xl -> 1280;
            case xxl -> 1536;
            default -> 0;
        };
    }

    private SignatureLevel getSignatureLevel() {
        return level;
    }

    private ASiCContainerType getContainer() {
        return container;
    }

    public void validate(MimeType mimeType) throws RequestValidationException {
        if (level == null)
            throw new RequestValidationException("Parameters.Level is required", "");

        var supportedLevels = Arrays.asList(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B,
                SignatureLevel.CAdES_BASELINE_B);

        if (!supportedLevels.contains(level))
            throw new UnsupportedSignatureLevelExceptionError(level.name());

        if (level.getSignatureForm() == SignatureForm.PAdES) {
            if (!mimeType.equals(MimeTypeEnum.PDF))
                throw new RequestValidationException("PayloadMimeType and Parameters.Level mismatch",
                        "Parameters.Level: PAdES is not supported for this payload: " + mimeType.getMimeTypeString());

            if (container != null)
                throw new RequestValidationException("Parameters.Container is not supported for PAdES",
                        "PAdES signature cannot be in a container");
        }

        if (level.getSignatureForm() == SignatureForm.XAdES) {
            if (!isXML(mimeType) && !isXDC(mimeType) && !isAsice(mimeType) && container == null)
                if (!(packaging != null && packaging == SignaturePackaging.ENVELOPING))
                    throw new RequestValidationException(
                            "PayloadMimeType, Parameters.Level, Parameters.Container and Parameters.Packaging mismatch",
                            "Parameters.Level: XAdES without container and ENVELOPED packaging is not supported for this payload: "
                                    + mimeType.getMimeTypeString());
        }

        if (containerXmlns != null && containerXmlns.contains("xmldatacontainer")
                && !isXDC(mimeType)) {

            if (!autoLoadEform && (transformation == null || transformation.isEmpty()))
                throw new RequestValidationException("Parameters.Transformation is null",
                        "Parameters.Transformation or Parameters.AutoLoadEform is required when creating XML datacontainer - when Parameters.ContainerXmlns is set to xmldatacontainer");

            if (!autoLoadEform && (schema == null || schema.isEmpty()))
                throw new RequestValidationException("Parameters.Schema is null",
                        "Parameters.Schema or Parameters.AutoLoadEform is required when creating XML datacontainer - when Parameters.ContainerXmlns is set to xmldatacontainer");

            if (identifier == null || identifier.isEmpty())
                throw new RequestValidationException("Parameters.Identifier is null",
                        "Parameters.Identifier is required when creating XML datacontainer - when Parameters.ContainerXmlns is set to xmldatacontainer");

            if (!isXML(mimeType))
                throw new RequestValidationException("PayloadMimeType and Parameters.ContainerXmlns mismatch",
                        "Parameters.ContainerXmlns: XML datacontainer is not supported for this payload: "
                                + mimeType.getMimeTypeString());
        }
    }
}
