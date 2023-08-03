package digital.slovensko.autogram.server.dto;

import java.util.Base64;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.model.InMemoryDocument;

public class ValidateRequestBody {
    private final String payloadMimeType;
    private final String payload;

    public ValidateRequestBody(String payloadMimeType, String payload) {
        this.payloadMimeType = payloadMimeType;
        this.payload = payload;
    }

    public InMemoryDocument getDocument() throws RequestValidationException {
        if (payload == null)
            throw new RequestValidationException("Payload is required", "");

        if (payloadMimeType == null)
            // TODO: payloadMimeType is only used to determine if the payload is base64 encoded
            throw new RequestValidationException("PayloadMimeType is required", "");

        if (payloadMimeType.contains("base64"))
            return new InMemoryDocument(Base64.getDecoder().decode(payload));

        return new InMemoryDocument(payload.getBytes());
    }
}
