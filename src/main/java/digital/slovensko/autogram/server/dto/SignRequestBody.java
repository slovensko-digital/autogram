package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.model.InMemoryDocument;

public class SignRequestBody {
    private final Document document;
    private final ServerSigningParameters parameters;
    private final String payloadMimeType;
    private final String batchId;

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType, String batchId) {
        this.document = document;
        this.parameters = parameters;
        this.payloadMimeType = payloadMimeType;
        this.batchId = batchId;
    }

    public InMemoryDocument getDocument() throws RequestValidationException {
        if (payloadMimeType == null)
            throw new RequestValidationException("PayloadMimeType is required", "");

        if (document == null)
            throw new RequestValidationException("Document is required", "");

        if (document.getContent() == null)
            throw new RequestValidationException("Document.Content is required", "");

        byte[] content;
        if (isBase64()) {
            content = Base64.getDecoder().decode(document.getContent());
        } else {
            content = document.getContent().getBytes();
        }

        var filename = document.getFilename();
        var mimetype = AutogramMimeType.fromMimeTypeString(payloadMimeType.split(";")[0]);

        return new InMemoryDocument(content, filename, mimetype);
    }

    public SigningParameters getParameters() throws RequestValidationException {
        if (parameters == null)
            throw new RequestValidationException("Parameters are required", "");

        parameters.validate(getDocument().getMimeType());

        return parameters.getSigningParameters(isBase64());
    }


    public String getBatchId() {
        return batchId;
    }

    private boolean isBase64() {
        return payloadMimeType.contains("base64");
    }
}
