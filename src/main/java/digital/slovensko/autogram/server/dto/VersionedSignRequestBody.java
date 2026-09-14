package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.SigningInput;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

import java.util.List;

import static digital.slovensko.autogram.core.AutogramMimeType.fromMimeTypeString;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_BATCH_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED;

public class VersionedSignRequestBody {
    private Document document;
    private List<Document> documents;
    private VersionedSigningParameters parameters;
    private PresentationParameters presentation;
    private String batchId;

    public SigningInput getSigningInput(boolean plainXmlEnabled) {
        var submittedDocuments = getSubmittedDocuments();
        if (parameters == null)
            parameters = new VersionedSigningParameters();

        var isMultiDocument = submittedDocuments.size() > 1;

        if (isMultiDocument && batchId != null)
            throw new RequestValidationException(MULTI_DOCUMENT_BATCH_UNSUPPORTED);

        submittedDocuments.forEach(doc -> validateDocument(doc, submittedDocuments.indexOf(doc), isMultiDocument));

        parameters.resolveSignatureFormatAndContainer(isMultiDocument);
        var signingParameters = parameters.toSigningParameters(presentation, plainXmlEnabled);

        var autogramDocuments = submittedDocuments.stream()
                .map((doc) -> doc.toAutogramDocument(signingParameters.getPropertiesCanonicalization(), signingParameters.getDigestAlgorithm()))
                .toList();


        return SigningInput.of(autogramDocuments, signingParameters);
    }

    public String getBatchId() {
        return batchId;
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

        if (document.getContent() == null)
            throw new RequestValidationException(MISSING_FIELD, documentLabel + ".Content");

        if (document.getMimeType() == null)
            throw new RequestValidationException(MISSING_FIELD, documentLabel + ".MimeType");

    if (isMultiDocument && MimeTypeEnum.ASICE.equals(getMimeType(document)))
        throw new RequestValidationException(MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED,
            documentLabel + ".MimeType");
    }

    private MimeType getMimeType(Document document) {
        return fromMimeTypeString(document.getMimeTypeString().split(";")[0]);
    }
}