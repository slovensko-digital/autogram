package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.errors.MalformedBodyException;

/**
 * Malformed base64 payloads are client errors and must surface as
 * {@link MalformedBodyException} (HTTP 400), not as a raw
 * {@link IllegalArgumentException} (which becomes HTTP 500).
 */
public class DocumentBase64DecodingErrorTest {
    private final Gson gson = new Gson();

    @Test
    void invalidBase64DocumentContentIsReportedAsMalformedBody() {
        var document = new Document("doc.pdf", "!!!not-base64!!!", "application/pdf;base64", null);

        assertThrows(MalformedBodyException.class, document::getDSSDocument);
    }

    @Test
    void invalidBase64XdcSchemaIsReportedAsMalformedBody() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "filename": "document.xml",
                      "content": "PGRvYy8+",
                      "mimeType": "application/xml;base64",
                      "xdcParameters": {
                        "schema": "!!!not-base64!!!",
                        "schemaMimeType": "application/xml;base64"
                      }
                    }
                  ],
                  "parameters": {
                    "form": "XAdES"
                  }
                }
                """, VersionedSignRequestBody.class);

        assertThrows(MalformedBodyException.class, () -> body.getSigningInput(false));
    }
}
