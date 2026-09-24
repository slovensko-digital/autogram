package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.ui.SupportedLanguage;

public class VersionedSignRequestBodyMissingMimeTypeTest {
    private final Gson gson = new Gson();

    @Test
    void rejectsSingleDocumentWithMissingMimeTypeAsValidationError() {
        var body = gson.fromJson("""
                {
                  "document": {
                    "content": "QQ=="
                  }
                }
                """, VersionedSignRequestBody.class);

        var exception = assertThrows(RequestValidationException.class, () -> body.getSigningInput(false));

        assertEquals("Document.MimeType is required",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }

    @Test
    void rejectsDocumentsWithMissingMimeTypeAsValidationError() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "content": "QQ=="
                    }
                  ]
                }
                """, VersionedSignRequestBody.class);

        var exception = assertThrows(RequestValidationException.class, () -> body.getSigningInput(false));

        assertEquals("Document.MimeType is required",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}
