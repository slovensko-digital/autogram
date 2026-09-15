package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.util.Base64;

import static digital.slovensko.autogram.core.dto.AutogramMimeType.fromMimeTypeString;
import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.BASE64_DECODING_FAILED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_PARAMS;

public class SignRequestBody {
    private final Document document;
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
            throw new RequestValidationException(MISSING_FIELD, "PayloadMimeType");

        if (document == null)
            throw new RequestValidationException(MISSING_FIELD, "Document");

        if (document.getContent() == null)
            throw new RequestValidationException(MISSING_FIELD, "Document.Content");

//      TODO: resolve values at class instantiation
        validateSignatureLevel();
    }

    private void validateSignatureLevel() throws RequestValidationException {
        if (parameters == null)
            parameters = new ServerSigningParameters();

        parameters.resolveSignatureLevel(getRequestDocument());
    }

    public AutogramDocument getDocument() {
        return AutogramDocument.build(getRequestDocument(), parameters.getEFormAttributes(isBase64()));
    }

    private InMemoryDocument getRequestDocument() {
        var content = decodeDocumentContent(document.getContent(), isBase64());
        var filename = document.getFilename();

        return new InMemoryDocument(content, filename, getMimetype());
    }

    public void validateSigningParameters() throws RequestValidationException, MalformedBodyException,
            TransformationParsingErrorException {
        if (parameters == null)
            throw new RequestValidationException(MISSING_PARAMS);

        parameters.validate(getDocument().toDssDocument());
    }

    public SigningInput getSigningInput(boolean plainXmlEnabled) {
        return parameters.getSigningInput(isBase64(), getDocument(), plainXmlEnabled);
    }

    public SigningParameters getParameters(boolean plainXmlEnabled) {
        return getSigningInput(plainXmlEnabled).getParameters();
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
                throw new MalformedBodyException(BASE64_DECODING_FAILED);
            }

        return content.getBytes();
    }
}
