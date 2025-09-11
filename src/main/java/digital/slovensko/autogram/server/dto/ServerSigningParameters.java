package digital.slovensko.autogram.server.dto;

import static digital.slovensko.autogram.core.AutogramMimeType.isAsice;
import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.server.errors.UnsupportedSignatureLevelException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

public class ServerSigningParameters {
    public enum LocalCanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE,
        INCLUSIVE_WITH_COMMENTS,
        EXCLUSIVE_WITH_COMMENTS,
        INCLUSIVE_11,
        INCLUSIVE_11_WITH_COMMENTS
    }

    public enum LocalSignatureLevel {
        XAdES_BASELINE_B,
        PAdES_BASELINE_B,
        CAdES_BASELINE_B,
        XAdES_BASELINE_T,
        PAdES_BASELINE_T,
        CAdES_BASELINE_T,
        BASELINE_B,
        BASELINE_T
    }

    public enum TransformationOutputMimeType {
        TXT,
        HTML,
        XHTML
    }

    public enum VisualizationWidthEnum {
        sm,
        md,
        lg,
        xl,
        xxl
    }

    private ASiCContainerType container;
    private LocalSignatureLevel level;
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
    private final boolean embedUsedSchemas;
    private final String schemaIdentifier;
    private final String transformationIdentifier;
    private final String transformationLanguage;
    private final TransformationOutputMimeType transformationMediaDestinationTypeDescription;
    private final String transformationTargetEnvironment;
    private final String fsFormId;

    public ServerSigningParameters(LocalSignatureLevel level, ASiCContainerType container,
            String containerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm,
            Boolean en319132, LocalCanonicalizationMethod infoCanonicalization,
            LocalCanonicalizationMethod propertiesCanonicalization, LocalCanonicalizationMethod keyInfoCanonicalization,
            String schema, String transformation,
            String Identifier, boolean checkPDFACompliance, VisualizationWidthEnum preferredPreviewWidth,
            boolean autoLoadEform, boolean embedUsedSchemas, String schemaIdentifier, String transformationIdentifier,
            String transformationLanguage, TransformationOutputMimeType transformationMediaDestinationTypeDescription,
            String transformationTargetEnvironment, String fsFormId) {
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
        this.embedUsedSchemas = embedUsedSchemas;
        this.schemaIdentifier = schemaIdentifier;
        this.transformationIdentifier = transformationIdentifier;
        this.transformationLanguage = transformationLanguage;
        this.transformationMediaDestinationTypeDescription = transformationMediaDestinationTypeDescription;
        this.transformationTargetEnvironment = transformationTargetEnvironment;
        this.fsFormId = fsFormId;
    }

    public ServerSigningParameters() {
        this.containerXmlns = null;
        this.packaging = null;
        this.digestAlgorithm = null;
        this.en319132 = null;
        this.infoCanonicalization = null;
        this.propertiesCanonicalization = null;
        this.keyInfoCanonicalization = null;
        this.schema = null;
        this.transformation = null;
        this.identifier = null;
        this.checkPDFACompliance = false;
        this.visualizationWidth = null;
        this.autoLoadEform = false;
        this.embedUsedSchemas = false;
        this.schemaIdentifier = null;
        this.transformationIdentifier = null;
        this.transformationLanguage = null;
        this.transformationMediaDestinationTypeDescription = null;
        this.transformationTargetEnvironment = null;
        this.fsFormId = null;
    }

    public SigningParameters getSigningParameters(boolean isBase64, DSSDocument document, TSPSource tspSource, boolean plainXmlEnabled) {
        var xsltParams = new XsltParams(
                transformationIdentifier,
                transformationLanguage,
                getTransformationMediaDestinationTypeDescription(),
                transformationTargetEnvironment,
                null);

        var eFormAttributes = new EFormAttributes(
                identifier,
                getTransformation(isBase64),
                getSchema(isBase64),
                containerXmlns,
                schemaIdentifier,
                xsltParams,
                getBoolean(embedUsedSchemas));

        return SigningParameters.buildParameters(
                getSignatureLevel(),
                digestAlgorithm,
                getContainer(),
                packaging,
                getBoolean(en319132),
                getCanonicalizationMethodString(infoCanonicalization),
                getCanonicalizationMethodString(propertiesCanonicalization),
                getCanonicalizationMethodString(keyInfoCanonicalization),
                eFormAttributes,
                autoLoadEform,
                getFsFormId(),
                getBoolean(checkPDFACompliance),
                getVisualizationWidth(),
                document,
                tspSource,
                plainXmlEnabled);
    }

    private static boolean getBoolean(Boolean variable) {
        if (variable == null)
            return false;

        return variable;
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

    private String getTransformationMediaDestinationTypeDescription() {
        if (transformationMediaDestinationTypeDescription == null)
            return null;

        return switch (transformationMediaDestinationTypeDescription) {
            case TXT -> "TXT";
            case HTML -> "HTML";
            case XHTML -> "XHTML";
        };
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
        return SignatureLevel.valueByName(level.name());
    }

    private ASiCContainerType getContainer() {
        return container;
    }

    public void resolveSigningLevel(InMemoryDocument document) throws RequestValidationException {
        if (level != null && level != LocalSignatureLevel.BASELINE_B && level != LocalSignatureLevel.BASELINE_T)
            return;

        if (level == null)
            level = LocalSignatureLevel.BASELINE_B;

        var report = SignatureValidator.getSignedDocumentSimpleReport(document);
        var signedLevel = SignatureValidator.getSignedDocumentSignatureLevel(report);
        if (signedLevel == null)
            throw new RequestValidationException("Parameters.Level can't be empty if document is not signed yet", "");

        container = report.getContainerType();
        level = switch (signedLevel.getSignatureForm()) {
            case PAdES -> LocalSignatureLevel.valueOf("PAdES" + level.name());
            case XAdES -> LocalSignatureLevel.valueOf("XAdES" + level.name());
            case CAdES -> LocalSignatureLevel.valueOf("CAdES" + level.name());
            default -> null;
        };

        if (level == null)
            throw new RequestValidationException("Signed document has unsupported SignatureLevel", "");
    }

    private String getFsFormId() {
        if (fsFormId == null || fsFormId.isEmpty())
            return null;

        return fsFormId;
    }

    public void validate(MimeType mimeType) throws RequestValidationException {
        if (level == null)
            throw new RequestValidationException("Parameters.Level is required", "");

        var supportedLevels = Arrays.asList(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B,
                SignatureLevel.CAdES_BASELINE_B,
                SignatureLevel.XAdES_BASELINE_T,
                SignatureLevel.PAdES_BASELINE_T,
                SignatureLevel.CAdES_BASELINE_T);

        if (!supportedLevels.contains(getSignatureLevel()))
            throw new UnsupportedSignatureLevelException(level.name());

        if (getSignatureLevel().getSignatureForm() == SignatureForm.PAdES) {
            if (!mimeType.equals(MimeTypeEnum.PDF))
                throw new RequestValidationException("PayloadMimeType and Parameters.Level mismatch",
                        "Parameters.Level: PAdES is not supported for this payload: " + mimeType.getMimeTypeString());

            if (container != null)
                throw new RequestValidationException("Parameters.Container is not supported for PAdES",
                        "PAdES signature cannot be in a container");
        }

        if (getSignatureLevel().getSignatureForm() == SignatureForm.XAdES) {
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
