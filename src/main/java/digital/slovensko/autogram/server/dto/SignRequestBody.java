package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.model.AutogramDocument;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public class SignRequestBody {
    private final Document document;
    private AutogramDocument autogramDocument;
    private ServerSigningParameters parameters;
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

//      TODO: resolve values at class instantiation
        resolveSigningLevel();
    }

    private void resolveSigningLevel() throws RequestValidationException {
        if (parameters == null)
            parameters = new ServerSigningParameters();

        parameters.resolveSigningLevel(getDocument());
    }

    public AutogramDocument getDocument() {
        if (autogramDocument != null)
            return autogramDocument;

        var content = decodeDocumentContent(document.getContent(), isBase64());
        var filename = document.getFilename();

        this.autogramDocument = new AutogramDocument(new InMemoryDocument(content, filename, getMimetype()));
        return autogramDocument;
    }

    public void validateSigningParameters() throws RequestValidationException, MalformedBodyException,
            TransformationParsingErrorException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getDocument().getMimeType());
    }

    public SigningParameters getParameters(TSPSource tspSource, boolean plainXmlEnabled) {
        return parameters.getSigningParameters(isBase64(), getDocument().getDocument(), tspSource, plainXmlEnabled);
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
