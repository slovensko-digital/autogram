package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import digital.slovensko.autogram.core.dto.SignedDocumentSignature;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_CONTAINER_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_FORMAT_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.SIGNED_DOCUMENT_CONTAINER_MISMATCH;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.SIGNED_DOCUMENT_FORM_MISMATCH;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.SIGNED_DOCUMENT_PACKAGING_MISMATCH;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.SigningParametersResolver;

public class VersionedSigningParameters {
    public enum LocalCanonicalizationMethod {
        INCLUSIVE,
        EXCLUSIVE,
        INCLUSIVE_WITH_COMMENTS,
        EXCLUSIVE_WITH_COMMENTS,
        INCLUSIVE_11,
        INCLUSIVE_11_WITH_COMMENTS
    }

    private SignatureForm form;
    private SignatureProfile profile;
    private ASiCContainerType container;
    private SignaturePackaging packaging;
    private DigestAlgorithm digestAlgorithm;
    private Boolean en319132;
    private LocalCanonicalizationMethod infoCanonicalization;
    private LocalCanonicalizationMethod propertiesCanonicalization;
    private LocalCanonicalizationMethod keyInfoCanonicalization;
    private Boolean checkPDFACompliance;

    public VersionedSigningParameters() {
    }

    public VersionedSigningParameters(SignatureForm form, SignatureProfile profile, ASiCContainerType container,
            SignaturePackaging packaging, DigestAlgorithm digestAlgorithm, Boolean en319132,
            LocalCanonicalizationMethod infoCanonicalization, LocalCanonicalizationMethod propertiesCanonicalization,
            LocalCanonicalizationMethod keyInfoCanonicalization, Boolean checkPDFACompliance) {

        this.form = form;
        this.profile = profile;
        this.container = container;
        this.packaging = packaging;
        this.digestAlgorithm = digestAlgorithm;
        this.en319132 = en319132;
        this.infoCanonicalization = infoCanonicalization;
        this.propertiesCanonicalization = propertiesCanonicalization;
        this.keyInfoCanonicalization = keyInfoCanonicalization;
        this.checkPDFACompliance = checkPDFACompliance;
    }

    public SigningParameters toSigningParameters(PresentationParameters presentation, boolean plainXmlEnabled) {
        return SigningParametersResolver.buildRequested(
                profile,
                form,
                digestAlgorithm,
                container,
                packaging,
                getBoolean(en319132),
                getCanonicalizationMethodString(infoCanonicalization),
                getCanonicalizationMethodString(propertiesCanonicalization),
                getCanonicalizationMethodString(keyInfoCanonicalization),
                getBoolean(checkPDFACompliance),
                presentation != null ? presentation.getVisualizationWidth() : 0,
                plainXmlEnabled
        );
    }

    public void resolveSignatureFormatAndContainer(boolean isMultiDocument) {
        if (profile == null)
            profile = SignatureProfile.BASELINE_B;

        if (form == null)
            form = SignatureForm.XAdES;

        if (!isMultiDocument)
            return;

        if (form == SignatureForm.PAdES)
            throw new RequestValidationException(MULTI_DOCUMENT_FORMAT_UNSUPPORTED);

        if (container != null && container != ASiCContainerType.ASiC_E)
            throw new RequestValidationException(MULTI_DOCUMENT_CONTAINER_UNSUPPORTED);
    }

    public void applySignedDocumentSignature(SignedDocumentSignature signedDocumentSignature) {
        if (form != null && form != signedDocumentSignature.form())
            throw new RequestValidationException(SIGNED_DOCUMENT_FORM_MISMATCH);

        if (container != null && container != signedDocumentSignature.container())
            throw new RequestValidationException(SIGNED_DOCUMENT_CONTAINER_MISMATCH);

        if (packaging != null && packaging != signedDocumentSignature.packaging())
            throw new RequestValidationException(SIGNED_DOCUMENT_PACKAGING_MISMATCH);

        form = signedDocumentSignature.form();
        container = signedDocumentSignature.container();
        packaging = signedDocumentSignature.packaging();
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

    private static boolean getBoolean(Boolean variable) {
        if (variable == null)
            return false;

        return variable;
    }
}