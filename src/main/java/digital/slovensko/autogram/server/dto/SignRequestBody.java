package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.errors.OriginalDocumentNotFoundException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import eu.europa.esig.dss.model.DSSDocument;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.InvalidXMLException;
import digital.slovensko.autogram.core.errors.MultipleOriginalDocumentsFoundException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.InMemoryDocument;

import static digital.slovensko.autogram.core.AutogramMimeType.*;
import static digital.slovensko.autogram.core.eforms.EFormUtils.*;

public class SignRequestBody {
    private final Document document;
    private final ServerSigningParameters parameters;
    private final String payloadMimeType;
    private final String batchId;

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType) {
        this(document, parameters, payloadMimeType, null);
    }

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType,
            String batchId) {
        this.document = document;
        this.parameters = parameters;
        this.payloadMimeType = payloadMimeType;
        this.batchId = batchId;
    }

    public void validateDocument() throws RequestValidationException, MalformedBodyException {
        if (payloadMimeType == null)
            throw new RequestValidationException("PayloadMimeType is required", "");

        if (document == null)
            throw new RequestValidationException("Document is required", "");

        if (document.getContent() == null)
            throw new RequestValidationException("Document.Content is required", "");
    }

    public InMemoryDocument getDocument() {
        var content = decodeDocumentContent(document.getContent(), isBase64());
        var filename = document.getFilename();

        return new InMemoryDocument(content, filename, getMimetype());
    }

    public void validateSigningParameters() throws RequestValidationException, MalformedBodyException,
            TransformationParsingErrorException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getMimeType());

        var signingParameters = parameters.getSigningParameters(isBase64(), getDocument());
        var parsedPaylodMimeType = getMimetype();
        if (isAsice(parsedPaylodMimeType) || isXML(parsedPaylodMimeType) || isXDC(parsedPaylodMimeType))
            validateXml(signingParameters.getSchema(), getXmlDocument());

        signingParameters.extractTransformationOutputMimeTypeString();
    }

    public SigningParameters getParameters() {
        return parameters.getSigningParameters(isBase64(), getDocument());
    }

    public String getBatchId() {
        return batchId;
    }

    private MimeType getMimetype() {
        return fromMimeTypeString(payloadMimeType.split(";")[0]);
    }

    private boolean isBase64() {
        return payloadMimeType.contains("base64");
    }

    private DSSDocument getXmlDocument()
            throws XMLValidationException, InvalidXMLException, OriginalDocumentNotFoundException,
            MultipleOriginalDocumentsFoundException {
        if (isAsice(getMimetype()))
            return getXdcDocumentFromAsice();

        return getDocument();
    }

    private DSSDocument getXdcDocumentFromAsice()
            throws XMLValidationException, InvalidXMLException, OriginalDocumentNotFoundException,
            MultipleOriginalDocumentsFoundException {
        var originalDocument = AsicContainerUtils.getOriginalDocument(getDocument());
        if (!isXDC(originalDocument.getMimeType()))
            return null;

        return originalDocument;
    }

    private static byte[] decodeDocumentContent(String content, boolean isBase64) throws MalformedBodyException {
        if (isBase64)
            try {
                return Base64.getDecoder().decode(content);
            } catch (IllegalArgumentException e) {
                throw new MalformedBodyException("Base64 decoding failed", "Invalid document content");
            }

        return content.getBytes();
    }
}
