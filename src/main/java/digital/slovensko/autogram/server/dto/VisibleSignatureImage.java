package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.BASE64_DECODING_FAILED;
import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;

public class VisibleSignatureImage {
    private String filename;
    private String content;
    private String mimeType;

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void validate(String labelPrefix) {
        if (content == null || content.isBlank())
            throw new digital.slovensko.autogram.server.errors.RequestValidationException(MISSING_FIELD,
                    labelPrefix + ".Content");

        if (mimeType == null || mimeType.isBlank())
            throw new digital.slovensko.autogram.server.errors.RequestValidationException(MISSING_FIELD,
                    labelPrefix + ".MimeType");
    }

    public InMemoryDocument toDssDocument() {
        return new InMemoryDocument(decodeContent(), filename, AutogramMimeType.fromMimeTypeString(getRawMimeType()));
    }

    private String getRawMimeType() {
        return mimeType.split(";")[0];
    }

    private boolean isBase64() {
        return mimeType.contains("base64");
    }

    private byte[] decodeContent() {
        if (isBase64()) {
            try {
                return Base64.getDecoder().decode(content);
            } catch (IllegalArgumentException e) {
                throw new MalformedBodyException(BASE64_DECODING_FAILED);
            }
        }

        return content.getBytes(StandardCharsets.UTF_8);
    }
}