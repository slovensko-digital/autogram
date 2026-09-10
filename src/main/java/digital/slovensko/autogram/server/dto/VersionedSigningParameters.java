package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_CONTAINER_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_FORMAT_UNSUPPORTED;

public class VersionedSigningParameters {
    public enum BaselineLevel {
        BASELINE_B,
        BASELINE_T
    }

    private SignatureForm format;
    private BaselineLevel level;
    private ASiCContainerType container;
    private SignaturePackaging packaging;
    private DigestAlgorithm digestAlgorithm;
    private Boolean en319132;
    private ServerSigningParameters.LocalCanonicalizationMethod infoCanonicalization;
    private ServerSigningParameters.LocalCanonicalizationMethod propertiesCanonicalization;
    private ServerSigningParameters.LocalCanonicalizationMethod keyInfoCanonicalization;
    private Boolean checkPDFACompliance;

    public ServerSigningParameters toServerSigningParameters(PresentationParameters presentation,
            XDCParameters xdcParameters, boolean isMultiDocument) {
        var resolvedFormat = resolveFormat(isMultiDocument);
        var resolvedContainer = resolveContainer(isMultiDocument);
        var resolvedLevel = resolveLevel(resolvedFormat);
        var visualizationWidth = presentation != null ? presentation.getVisualizationWidth() : null;

        return new ServerSigningParameters(
                resolvedLevel,
                resolvedContainer,
                null,
                xdcParameters != null ? xdcParameters.getContainerXmlns() : null,
                packaging,
                digestAlgorithm,
                en319132,
                infoCanonicalization,
                propertiesCanonicalization,
                keyInfoCanonicalization,
                xdcParameters != null ? xdcParameters.getSchema() : null,
                xdcParameters != null ? xdcParameters.getTransformation() : null,
                xdcParameters != null ? xdcParameters.getIdentifier() : null,
                Boolean.TRUE.equals(checkPDFACompliance),
                visualizationWidth,
                xdcParameters != null && xdcParameters.isAutoLoadEform(),
                xdcParameters != null && xdcParameters.getEmbedUsedSchemas(),
                xdcParameters != null ? xdcParameters.getSchemaIdentifier() : null,
                xdcParameters != null ? xdcParameters.getTransformationIdentifier() : null,
                xdcParameters != null ? xdcParameters.getTransformationLanguage() : null,
                xdcParameters != null ? xdcParameters.getTransformationMediaDestinationTypeDescription() : null,
                xdcParameters != null ? xdcParameters.getTransformationTargetEnvironment() : null,
                xdcParameters != null ? xdcParameters.getFsFormIdentifier() : null);
    }

    private SignatureForm resolveFormat(boolean isMultiDocument) {
        if (!isMultiDocument)
            return format;

        if (format == null)
            return SignatureForm.XAdES;

        if (format == SignatureForm.PAdES)
            throw new RequestValidationException(MULTI_DOCUMENT_FORMAT_UNSUPPORTED);

        return format;
    }

    private ASiCContainerType resolveContainer(boolean isMultiDocument) {
        if (!isMultiDocument)
            return container;

        if (container != null && container != ASiCContainerType.ASiC_E)
            throw new RequestValidationException(MULTI_DOCUMENT_CONTAINER_UNSUPPORTED);

        return ASiCContainerType.ASiC_E;
    }

    private ServerSigningParameters.LocalSignatureLevel resolveLevel(SignatureForm resolvedFormat) {
        var resolvedLevel = level == BaselineLevel.BASELINE_T
                ? ServerSigningParameters.LocalSignatureLevel.BASELINE_T
                : ServerSigningParameters.LocalSignatureLevel.BASELINE_B;

        if (resolvedFormat == null)
            return resolvedLevel;

        return switch (resolvedFormat) {
            case XAdES -> resolvedLevel == ServerSigningParameters.LocalSignatureLevel.BASELINE_T
                    ? ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_T
                    : ServerSigningParameters.LocalSignatureLevel.XAdES_BASELINE_B;
            case PAdES -> resolvedLevel == ServerSigningParameters.LocalSignatureLevel.BASELINE_T
                    ? ServerSigningParameters.LocalSignatureLevel.PAdES_BASELINE_T
                    : ServerSigningParameters.LocalSignatureLevel.PAdES_BASELINE_B;
            case CAdES -> resolvedLevel == ServerSigningParameters.LocalSignatureLevel.BASELINE_T
                    ? ServerSigningParameters.LocalSignatureLevel.CAdES_BASELINE_T
                    : ServerSigningParameters.LocalSignatureLevel.CAdES_BASELINE_B;
            default -> resolvedLevel;
        };
    }
}