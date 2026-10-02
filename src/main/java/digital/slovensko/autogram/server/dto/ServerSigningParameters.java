package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.dto.PresentationParameters.VisualizationWidthEnum;
import digital.slovensko.autogram.server.dto.VersionedSigningParameters.LocalCanonicalizationMethod;
import digital.slovensko.autogram.server.dto.XDCParameters.TransformationOutputMimeType;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;

public record ServerSigningParameters (
    LocalSignatureLevel level,
    ASiCContainerType container,
    String containerXmlns,
    SignaturePackaging packaging,
    DigestAlgorithm digestAlgorithm,
    Boolean en319132,
    LocalCanonicalizationMethod infoCanonicalization,
    LocalCanonicalizationMethod propertiesCanonicalization,
    LocalCanonicalizationMethod keyInfoCanonicalization,
    String schema,
    String transformation,
    String identifier,
    boolean checkPDFACompliance,
    VisualizationWidthEnum visualizationWidth,
    Boolean autoLoadEform,
    boolean embedUsedSchemas,
    String schemaIdentifier,
    String transformationIdentifier,
    String transformationLanguage,
    TransformationOutputMimeType transformationMediaDestinationTypeDescription,
    String transformationTargetEnvironment,
    String fsFormId
) {

    public enum LocalSignatureLevel {
        XAdES_BASELINE_B,
        PAdES_BASELINE_B,
        CAdES_BASELINE_B,
        XAdES_BASELINE_T,
        PAdES_BASELINE_T,
        CAdES_BASELINE_T,
        BASELINE_B,
        BASELINE_T;

        public SignatureForm getSignatureForm() {
            if (name().equals("BASELINE_B") || name().equals("BASELINE_T"))
                return null;

            return SignatureLevel.valueByName(name()).getSignatureForm();
        }

        public SignatureProfile getSignatureProfile() {
            if (name().equals("BASELINE_B") || name().equals("BASELINE_T"))
                return SignatureProfile.valueByName(name());

            return SignatureLevel.valueByName(name()).getSignatureProfile();
        }
    }

    public ServerSigningParameters () {
        this(null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, false, null, null, null, null, null, null);
    }

    public XDCParameters toXDCParameters() {
        return new XDCParameters(
            fsFormId,
            autoLoadEform,
            identifier,
            containerXmlns,
            embedUsedSchemas,
            schema,
            null,
            schemaIdentifier,
            transformation,
            transformationIdentifier,
            transformationLanguage,
            transformationMediaDestinationTypeDescription,
            transformationTargetEnvironment
        );
    }

    public VersionedSigningParameters toVersionedParameters() {
        return new VersionedSigningParameters(
            level != null ? level.getSignatureForm() : null,
            level != null ? level.getSignatureProfile() : null,
            container,
            packaging,
            digestAlgorithm,
            en319132,
            infoCanonicalization,
            propertiesCanonicalization,
            keyInfoCanonicalization,
            checkPDFACompliance
        );
    }

    public PresentationParameters toPresentationParameters() {
        return PresentationParameters.fromString(visualizationWidth == null ? null : visualizationWidth.name());
    }
}
