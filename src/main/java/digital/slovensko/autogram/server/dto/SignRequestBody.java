package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.server.errors.RequestValidationException;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.EMPTY_PARAMS_LEVEL;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;

public record SignRequestBody (
    LegacyDocument document,
    ServerSigningParameters parameters,
    String payloadMimeType,
    String batchId
) {

    public SignRequestBody(LegacyDocument document, ServerSigningParameters parameters, String payloadMimeType) {
        this(document, parameters, payloadMimeType, null);
    }

    private static void validateLevel(ServerSigningParameters parameters, Document document) {
        if (parameters.level() != null && parameters.level().getSignatureForm() != null)
            return;

        if (SignatureValidator.getSignedDocumentSignature(document.getDSSDocument()) == null)
            throw new RequestValidationException(EMPTY_PARAMS_LEVEL);
    }

    public VersionedSignRequestBody toVersionedBody() {
        var localParameters = parameters;
        if (parameters == null)
            localParameters = new ServerSigningParameters();

        if (document == null)
            throw new RequestValidationException(MISSING_FIELD, "Document");

        var versionedDocument = new Document(
            document.filename(),
            document.content(),
            payloadMimeType,
            localParameters.toXDCParameters()
        );

        validateLevel(localParameters, versionedDocument);

        return new VersionedSignRequestBody(
            versionedDocument,
            null,
            localParameters.toVersionedParameters(),
            localParameters.toPresentationParameters(),
            batchId
        );
    }
}
