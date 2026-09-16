package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocumentSignature;
import digital.slovensko.autogram.core.errors.SigningParametersException;
import digital.slovensko.autogram.core.errors.UnknownEformException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;

import java.util.List;

import static digital.slovensko.autogram.core.errors.SigningParametersException.Error.INVALID_PACKAGING;

/**
 * The exclusive place SigningParameters gets built. SigningParameters.buildParameters is
 * package-private for exactly this reason: nothing outside this class (and outside the core
 * package) constructs a SigningParameters directly. Callers outside core seed a request via
 * buildRequested, then reconcile it against documents via resolveStrict/resolveLenient.
 * resolveStrict rejects an incompatible combination (API contract, mapped to 4xx by callers);
 * resolveLenient corrects it instead (standalone defaults from UserSettings/CliSettings, where
 * failing outright on a document the user just wants signed would be surprising).
 */
public class SigningParametersResolver {
    public static SigningParameters buildRequested(
            SignatureProfile profile, SignatureForm form, DigestAlgorithm digestAlgorithm, ASiCContainerType container, SignaturePackaging packaging,
            boolean en319132, String infoCanonicalization, String propertiesCanonicalization, String keyInfoCanonicalization,
            boolean checkPDFACompliance, int preferredPreviewWidth, boolean plainXmlEnabled) {
        return SigningParameters.buildParameters(profile, form, digestAlgorithm, container, packaging, en319132,
                infoCanonicalization, propertiesCanonicalization, keyInfoCanonicalization,
                checkPDFACompliance, preferredPreviewWidth, plainXmlEnabled);
    }

    public static SigningParameters resolveStrict(SigningParameters requested, List<AutogramDocument> documents) {
        rejectUnknownPlainXml(requested, documents);

        if (requiresAsicContainer(documents) && requested.getContainer() != ASiCContainerType.ASiC_E)
            requested = withContainer(requested, ASiCContainerType.ASiC_E);

        if (isInvalidXadesPackaging(requested, documents))
            throw new SigningParametersException(INVALID_PACKAGING);

        return requested;
    }

    public static SigningParameters resolveLenient(SigningParameters requested, List<AutogramDocument> documents) {
        rejectUnknownPlainXml(requested, documents);

        if (requiresAsicContainer(documents) && requested.getContainer() != ASiCContainerType.ASiC_E)
            requested = withContainer(requested, ASiCContainerType.ASiC_E);

        if (isInvalidXadesPackaging(requested, documents))
            requested = withContainer(requested, ASiCContainerType.ASiC_E);

        return requested;
    }

    /**
     * Lenient resolution for a single file being auto-detected (the GUI file-picker/batch and CLI
     * path): if the document is already signed, adopt its embedded form/container/packaging
     * as-is; otherwise pick a form/container for it based on its type and the requested level
     * (raw PDF for PAdES, ASiC-E wrapping otherwise), then run the result through the same
     * lenient correction as resolveLenient.
     */
    public static SigningParameters resolveLenientFromFile(SigningParameters requested, AutogramDocument document) {
        var documents = List.of(document);

        var signedDocumentSignature = SignatureValidator.getSignedDocumentSignature(document.toDssDocument());
        if (signedDocumentSignature != null)
            return resolveLenient(withSignedDocumentSignature(requested, signedDocumentSignature), documents);

        if (document.isPDF()) {
            var level = requested.getLevel();
            if (level == SignatureLevel.PAdES_BASELINE_B)
                return resolveLenient(requested, documents);

            if (level == SignatureLevel.CAdES_BASELINE_B)
                return resolveLenient(withFormAndContainer(requested, SignatureForm.CAdES, ASiCContainerType.ASiC_E), documents);
        }

        return resolveLenient(withFormAndContainer(requested, SignatureForm.XAdES, ASiCContainerType.ASiC_E), documents);
    }

    /**
     * Adopts the form/container/packaging embedded in an already-signed document, used when
     * re-signing. Explicit packaging on the signed document wins; otherwise the requested
     * packaging is kept.
     */
    public static SigningParameters withSignedDocumentSignature(SigningParameters requested, SignedDocumentSignature signature) {
        return copyWith(requested, signature.form(), signature.container(),
                signature.packaging() != null ? signature.packaging() : requested.getSignaturePackaging());
    }

    /** Forces a specific form/container, e.g. when wrapping a document into an ASiC-E container. */
    public static SigningParameters withFormAndContainer(SigningParameters requested, SignatureForm form, ASiCContainerType container) {
        return copyWith(requested, form, container, requested.getSignaturePackaging());
    }

    private static void rejectUnknownPlainXml(SigningParameters parameters, List<AutogramDocument> documents) {
        if (!parameters.isPlainXmlEnabled() && documents.stream().anyMatch(d -> d.isXML() && !d.isEForm()))
            throw new UnknownEformException();
    }

    private static boolean requiresAsicContainer(List<AutogramDocument> documents) {
        return documents.size() > 1 || documents.stream().anyMatch(d -> d.isEForm() || d.isAsice());
    }

    private static boolean isInvalidXadesPackaging(SigningParameters parameters, List<AutogramDocument> documents) {
        return parameters.getSignatureForm() == SignatureForm.XAdES
                && parameters.getContainer() == null
                && parameters.getSignaturePackaging() != SignaturePackaging.ENVELOPING
                && documents.stream().anyMatch(d -> !d.isXML() && !d.isXDC() && !d.isAsice());
    }

    private static SigningParameters withContainer(SigningParameters parameters, ASiCContainerType container) {
        return copyWith(parameters, parameters.getSignatureForm(), container, parameters.getSignaturePackaging());
    }

    private static SigningParameters copyWith(SigningParameters base, SignatureForm form, ASiCContainerType container, SignaturePackaging packaging) {
        return SigningParameters.buildParameters(
                base.getSignatureProfile(), form, base.getDigestAlgorithm(),
                container, packaging, base.isEn319132(),
                base.getInfoCanonicalization(), base.getPropertiesCanonicalization(),
                base.getKeyInfoCanonicalization(), base.getCheckPDFACompliance(),
                base.getVisualizationWidth(), base.isPlainXmlEnabled());
    }
}
