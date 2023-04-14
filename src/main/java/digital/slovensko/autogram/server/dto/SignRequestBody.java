package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.core.errors.MalformedMimetypeException;
import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;

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

    public InMemoryDocument getDocument() {
        byte[] content;
        if (isBase64()) {
            content = Base64.getDecoder().decode(document.getContent());
        } else {
            content = document.getContent().getBytes();
        }

        var filename = document.getFilename();
        var mimetype = MimeType.fromMimeTypeString(payloadMimeType.split(";")[0]);

        if (filename != null)
            mimetype = MimeType.fromFileName(filename);

        if (parameters.getFileMimeTypeString() != null)
            mimetype = MimeType.fromMimeTypeString(parameters.getFileMimeTypeString());

        return new InMemoryDocument(content, filename, mimetype);
    }

    public SigningParameters getParameters() throws MalformedMimetypeException {
        return parameters.getSigningParameters(isBase64());
    }

    private boolean isBase64() {
        return payloadMimeType.contains("base64");
    }
}
