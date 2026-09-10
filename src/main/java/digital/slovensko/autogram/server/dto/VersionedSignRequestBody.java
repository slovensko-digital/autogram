package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.SigningInput;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.util.Base64;
import java.util.List;
import java.util.stream.IntStream;

import static digital.slovensko.autogram.core.AutogramMimeType.fromMimeTypeString;
import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.BASE64_DECODING_FAILED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_BATCH_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED;

public class VersionedSignRequestBody {
    private Document document;
    private List<Document> documents;
    private VersionedSigningParameters parameters;
    private PresentationParameters presentation;
    private String batchId;

    public SigningInput getSigningInput(TSPSource tspSource, boolean plainXmlEnabled) {
        var submittedDocuments = getSubmittedDocuments();
        var resolvedParameters = parameters != null ? parameters : new VersionedSigningParameters();
        var isMultiDocument = submittedDocuments.size() > 1;

        if (isMultiDocument && batchId != null)
            throw new RequestValidationException(MULTI_DOCUMENT_BATCH_UNSUPPORTED);

        var preparedDocuments = IntStream.range(0, submittedDocuments.size())
            .mapToObj(index -> prepareSubmittedDocument(submittedDocuments.get(index), index, isMultiDocument,
                resolvedParameters, tspSource, plainXmlEnabled))
            .toList();

        return SigningInput.of(preparedDocuments.stream().map(PreparedDocument::document).toList(),
            preparedDocuments.getFirst().parameters());
    }

        private PreparedDocument prepareSubmittedDocument(Document submittedDocument, int index, boolean isMultiDocument,
            VersionedSigningParameters resolvedParameters, TSPSource tspSource, boolean plainXmlEnabled) {
        validateDocument(submittedDocument, index, isMultiDocument);

        var requestDocument = buildRequestDocument(submittedDocument);
        var rawDocument = AutogramDocument.fromDssDocument(requestDocument);
        var serverSigningParameters = resolvedParameters.toServerSigningParameters(presentation,
            submittedDocument.getXdcParameters(), isMultiDocument);

        if (!isMultiDocument)
            serverSigningParameters.resolveSigningLevel(requestDocument);

        serverSigningParameters.validate(rawDocument.getMimeType());

        var preparedParameters = serverSigningParameters.getPreparedSigningParameters(
            submittedDocument.getXdcParameters() != null && submittedDocument.getXdcParameters().areResourcesBase64(),
            rawDocument, tspSource, plainXmlEnabled);
        var documentWithEFormAttributes = rawDocument.withEFormAttributes(preparedParameters.eFormAttributes());

        return new PreparedDocument(
            SigningInput.prepareDocument(documentWithEFormAttributes, preparedParameters.signingParameters()),
            preparedParameters.signingParameters());
    }

    private record PreparedDocument(AutogramDocument document, SigningParameters parameters) {
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

    private InMemoryDocument buildRequestDocument(Document document) {
        var content = decodeDocumentContent(document.getContent(), isBase64(document));
        return new InMemoryDocument(content, document.getFilename(), getMimeType(document));
    }

    private MimeType getMimeType(Document document) {
        return fromMimeTypeString(document.getMimeType().split(";")[0]);
    }

    private boolean isBase64(Document document) {
        return document.getMimeType().contains("base64");
    }

    private static byte[] decodeDocumentContent(String content, boolean isBase64) {
        if (isBase64)
            try {
                return Base64.getDecoder().decode(content);
            } catch (IllegalArgumentException e) {
                throw new MalformedBodyException(BASE64_DECODING_FAILED);
            }

        return content.getBytes();
    }
}