package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.core.errors.EFormException;
import digital.slovensko.autogram.server.errors.MalformedBodyException;
import digital.slovensko.autogram.ui.SupportedLanguage;

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
    void invalidBase64XdcSchemaIsReportedAsInvalidXsd() {
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

        var exception = assertThrows(EFormException.class, () -> body.getSigningInput(false));

        assertEquals("Invalid XSD schema", exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }

    @Test
    void invalidBase64XdcTransformationIsReportedAsInvalidXslt() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "filename": "document.xml",
                      "content": "PGRvYy8+",
                      "mimeType": "application/xml;base64",
                      "xdcParameters": {
                        "transformation": "!!!not-base64!!!",
                        "schemaMimeType": "application/xml;base64"
                      }
                    }
                  ],
                  "parameters": {
                    "form": "XAdES"
                  }
                }
                """, VersionedSignRequestBody.class);

        var exception = assertThrows(EFormException.class, () -> body.getSigningInput(false));

        assertEquals("Invalid XSLT transformation",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}
