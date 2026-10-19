package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.dto.VersionedSigningParameters.LocalCanonicalizationMethod;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.EMPTY_PARAMS_LEVEL;

class LegacySigningParameters extends VersionedSigningParameters {
    LegacySigningParameters(SignatureForm form, SignatureProfile profile, ASiCContainerType container,
            SignaturePackaging packaging, DigestAlgorithm digestAlgorithm, Boolean en319132,
            LocalCanonicalizationMethod infoCanonicalization, LocalCanonicalizationMethod propertiesCanonicalization,
            LocalCanonicalizationMethod keyInfoCanonicalization, Boolean checkPDFACompliance) {

        super(form, profile, container, packaging, digestAlgorithm, en319132, infoCanonicalization,
                propertiesCanonicalization, keyInfoCanonicalization, checkPDFACompliance);
    }

    // The legacy level may only omit the form for an already signed document, whose form has been applied by now
    @Override
    public void resolveSignatureFormatAndContainer(boolean isMultiDocument) {
        if (getForm() == null)
            throw new RequestValidationException(EMPTY_PARAMS_LEVEL);

        super.resolveSignatureFormatAndContainer(isMultiDocument);
    }
}
