package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.AutogramDocument;
import digital.slovensko.autogram.core.AutogramSigningRequest;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static digital.slovensko.autogram.core.AutogramMimeType.fromMimeTypeString;
import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.BASE64_DECODING_FAILED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_BATCH_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.VISIBLE_SIGNATURE_UNSUPPORTED;

public class VersionedSignRequestBody {
    private Document document;
    private List<Document> documents;
    private VersionedSigningParameters parameters;
    private PresentationParameters presentation;
    private String batchId;

    public AutogramSigningRequest getSigningRequest(TSPSource tspSource, boolean plainXmlEnabled) {
        var submittedDocuments = getSubmittedDocuments();
        var resolvedParameters = parameters != null ? parameters : new VersionedSigningParameters();
        var preparedDocuments = new ArrayList<AutogramDocument>();
        var isMultiDocument = submittedDocuments.size() > 1;

        if (isMultiDocument && batchId != null)
            throw new RequestValidationException(MULTI_DOCUMENT_BATCH_UNSUPPORTED);

        for (int index = 0; index < submittedDocuments.size(); index++) {
            var submittedDocument = submittedDocuments.get(index);
            validateDocument(submittedDocument, index, isMultiDocument);

            var requestDocument = buildRequestDocument(submittedDocument);
            var rawDocument = AutogramDocument.fromDssDocument(requestDocument);
            var serverSigningParameters = resolvedParameters.toServerSigningParameters(presentation,
                    submittedDocument.getXdcParameters(), isMultiDocument);

            if (!isMultiDocument)
                serverSigningParameters.resolveSigningLevel(requestDocument);

            serverSigningParameters.validate(rawDocument.getMimeType());

            var preprocessingParameters = serverSigningParameters.getSigningParameters(
                    submittedDocument.getXdcParameters() != null && submittedDocument.getXdcParameters().areResourcesBase64(),
                    rawDocument.toDssDocument(), tspSource, plainXmlEnabled);

            preparedDocuments.add(SigningJob.prepareDocument(rawDocument, preprocessingParameters));
        }

        var finalSigningParameters = resolvedParameters.toServerSigningParameters(presentation, null, isMultiDocument);

        if (!isMultiDocument)
            finalSigningParameters.resolveSigningLevel((InMemoryDocument) preparedDocuments.get(0).toDssDocument());

        finalSigningParameters.validate(preparedDocuments.get(0).getMimeType());

        var coreSigningParameters = finalSigningParameters.getSigningParameters(false,
            preparedDocuments.get(0).toDssDocument(), tspSource, plainXmlEnabled);
        applyVisibleSignature(coreSigningParameters, submittedDocuments, isMultiDocument);

        return AutogramSigningRequest.of(preparedDocuments, coreSigningParameters);
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

        if (document.getVisibleSignature() != null)
            document.getVisibleSignature().validate(documentLabel + ".VisibleSignature");

    if (isMultiDocument && MimeTypeEnum.ASICE.equals(getMimeType(document)))
        throw new RequestValidationException(MULTI_DOCUMENT_NESTED_ASICE_UNSUPPORTED,
            documentLabel + ".MimeType");
    }

    private void applyVisibleSignature(SigningParameters signingParameters, List<Document> submittedDocuments,
            boolean isMultiDocument) {
        if (isMultiDocument || submittedDocuments.isEmpty()) {
            if (submittedDocuments.stream().anyMatch(document -> document.getVisibleSignature() != null))
                throw new RequestValidationException(VISIBLE_SIGNATURE_UNSUPPORTED);
            return;
        }

        var visibleSignature = submittedDocuments.get(0).getVisibleSignature();
        if (visibleSignature == null)
            return;

        if (signingParameters.getSignatureType() != SignatureForm.PAdES)
            throw new RequestValidationException(VISIBLE_SIGNATURE_UNSUPPORTED);

        signingParameters.setPadesVisibleSignatureParameters(visibleSignature.toDssParameters());
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