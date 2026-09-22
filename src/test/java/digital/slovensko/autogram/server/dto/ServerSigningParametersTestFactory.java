package digital.slovensko.autogram.server.dto;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

final class ServerSigningParametersTestFactory {
    private ServerSigningParametersTestFactory() {
    }

    static ServerSigningParameters create(
            ServerSigningParameters.LocalSignatureLevel level, ASiCContainerType container,
            String removedContainerFilename, String containerXmlns, SignaturePackaging packaging,
            DigestAlgorithm digestAlgorithm, Boolean en319132,
            ServerSigningParameters.LocalCanonicalizationMethod infoCanonicalization,
            ServerSigningParameters.LocalCanonicalizationMethod propertiesCanonicalization,
            ServerSigningParameters.LocalCanonicalizationMethod keyInfoCanonicalization,
            String schema, String transformation, String identifier, boolean checkPDFACompliance,
            ServerSigningParameters.VisualizationWidthEnum visualizationWidth, Boolean autoLoadEform,
            boolean embedUsedSchemas, String schemaIdentifier, String transformationIdentifier,
            String transformationLanguage,
            ServerSigningParameters.TransformationOutputMimeType transformationMediaDestinationTypeDescription,
            String transformationTargetEnvironment, String fsFormId) {
        var resolvedDigestAlgorithm = digestAlgorithm != null ? digestAlgorithm : DigestAlgorithm.SHA256;
        return new ServerSigningParameters(level, container, containerXmlns, packaging, resolvedDigestAlgorithm, en319132,
                infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization, schema, transformation,
                identifier, checkPDFACompliance, visualizationWidth, autoLoadEform, embedUsedSchemas,
                schemaIdentifier, transformationIdentifier, transformationLanguage,
                transformationMediaDestinationTypeDescription, transformationTargetEnvironment, fsFormId);
    }
}