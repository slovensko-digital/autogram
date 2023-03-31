package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.errors.MalformedMimetypeException;
import digital.slovensko.autogram.core.MimeType;
import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.model.InMemoryDocument;

public class SignRequestBody {
    private Document document;
    private ServerSigningParameters parameters;
    private String payloadMimeType;

    public SignRequestBody() {
    }

    public SignRequestBody(Document document, ServerSigningParameters parameters, String payloadMimeType) {
        this.document = document;
        this.parameters = parameters;
        this.payloadMimeType = payloadMimeType;
    }

    public InMemoryDocument getDocument() throws MalformedMimetypeException {
        byte[] content;
        if (MimeType.parse(payloadMimeType).isBase64()) {
            content = Base64.getDecoder().decode(document.getContent());
        } else {
            content = document.getContent().getBytes();
        }

        if (document.getFilename() != null) {
            return new InMemoryDocument(content, document.getFilename());
        } else {
            return new InMemoryDocument(content);
        }
    }

    public SigningParameters getParameters() {
        return parameters.getSigningParameters();
    }

    public MimeType getPayloadMimeType() throws MalformedMimetypeException {
        return MimeType.parse(payloadMimeType);
    }
}
