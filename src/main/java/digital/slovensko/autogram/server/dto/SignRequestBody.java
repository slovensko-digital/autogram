package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;

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

    public VersionedSignRequestBody toVersionedBody() {
        var localParameters = parameters;
        if (parameters == null)
            localParameters = new ServerSigningParameters();

        if (payloadMimeType == null)
            throw new RequestValidationException(MISSING_FIELD, "PayloadMimeType");

        if (document == null)
            throw new RequestValidationException(MISSING_FIELD, "Document");

        if (document.content() == null)
            throw new RequestValidationException(MISSING_FIELD, "Document.Content");

        var versionedDocument = new Document(
            document.filename(),
            document.content(),
            payloadMimeType,
            localParameters.toXDCParameters()
        );

        return new VersionedSignRequestBody(
            versionedDocument,
            null,
            localParameters.toVersionedParameters(),
            localParameters.toPresentationParameters(),
            batchId
        );
    }
}
