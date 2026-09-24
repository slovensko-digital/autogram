package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.dto.AutogramMimeType;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

import java.util.List;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_BATCH_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED;

public record VersionedSignRequestBody (Document document, List<Document> documents,
        VersionedSigningParameters parameters, PresentationParameters presentation, String batchId) {

    public SigningInput getSigningInput(boolean plainXmlEnabled) {
        var submittedDocuments = getSubmittedDocuments();
        var parameters = this.parameters == null ? new VersionedSigningParameters() : this.parameters;

        var isMultiDocument = submittedDocuments.size() > 1;

        if (isMultiDocument && batchId != null)
            throw new RequestValidationException(MULTI_DOCUMENT_BATCH_UNSUPPORTED);

        submittedDocuments.forEach(doc -> validateDocument(doc, submittedDocuments.indexOf(doc), isMultiDocument));

        submittedDocuments.stream()
            .map(Document::getDSSDocument)
            .map(SignatureValidator::getSignedDocumentSignature)
            .filter(java.util.Objects::nonNull)
            .forEach(parameters::applySignedDocumentSignature);

        parameters.resolveSignatureFormatAndContainer(isMultiDocument);
        var signingParameters = parameters.toSigningParameters(presentation, plainXmlEnabled);

        var autogramDocuments = submittedDocuments.stream()
                .map((doc) -> doc.toAutogramDocument(signingParameters.getPropertiesCanonicalization(), signingParameters.getDigestAlgorithm()))
                .toList();


        return SigningInput.of(autogramDocuments, signingParameters);
    }

    private List<Document> getSubmittedDocuments() {
        if (documents != null && !documents.isEmpty())
            return documents;

        if (document != null)
            return List.of(document);

        throw new RequestValidationException(MISSING_FIELD, "Document or Documents");
    }

    private void validateDocument(Document document, int index, boolean isMultiDocument) {
        var documentLabel = isMultiDocument ? "Documents[" + index + "]" : "Document";

        if (document == null)
            throw new RequestValidationException(MISSING_FIELD, documentLabel);

        if (document.content() == null)
            throw new RequestValidationException(MISSING_FIELD, documentLabel + ".Content");

        if (document.getMimeTypeString() == null)
            throw new RequestValidationException(MISSING_FIELD, documentLabel + ".MimeType");

    if (isMultiDocument && MimeTypeEnum.ASICE.equals(getMimeType(document)))
        throw new RequestValidationException(MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED,
            documentLabel + ".MimeType");
    }

    private MimeType getMimeType(Document document) {
        return AutogramMimeType.fromMimeTypeString(document.getMimeTypeString().split(";")[0]);
    }
}