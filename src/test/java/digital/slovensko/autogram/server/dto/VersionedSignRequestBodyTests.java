package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.ui.SupportedLanguage;

public class VersionedSignRequestBodyTests {
    private final Gson gson = new Gson();

    @Test
    void buildsMultiDocumentSigningRequest() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "filename": "first.txt",
                      "content": "Zmlyc3Q=",
                      "mimeType": "text/plain;base64"
                    },
                    {
                      "filename": "second.txt",
                      "content": "c2Vjb25k",
                      "mimeType": "text/plain;base64"
                    }
                  ],
                  "parameters": {
                    "format": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningRequest(null, true);

        assertTrue(request.isMultiDocument());
        assertEquals(2, request.getDocumentCount());
        assertEquals("first.txt", request.getFirstDocument().getName());
        assertEquals(eu.europa.esig.dss.enumerations.ASiCContainerType.ASiC_E, request.getParameters().getContainer());
    }

    @Test
    void buildsSingleDocumentSigningRequest() {
        var body = gson.fromJson("""
                {
                  "document": {
                    "filename": "only.txt",
                    "content": "b25seQ==",
                    "mimeType": "text/plain;base64"
                  },
                  "parameters": {
                    "format": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningRequest(null, true);

        assertFalse(request.isMultiDocument());
        assertEquals(1, request.getDocumentCount());
        assertEquals("only.txt", request.getSingleDocument().getName());
    }

    @Test
    void rejectsNestedAsiceInMultiDocumentSigningRequest() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "filename": "first.txt",
                      "content": "Zmlyc3Q=",
                      "mimeType": "text/plain;base64"
                    },
                    {
                      "filename": "nested.asice",
                      "content": "QQ==",
                      "mimeType": "application/vnd.etsi.asic-e+zip;base64"
                    }
                  ],
                  "parameters": {
                    "format": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """, VersionedSignRequestBody.class);

        var exception = assertThrows(RequestValidationException.class, () -> body.getSigningRequest(null, true));

        assertEquals("Documents[1].MimeType must not be ASiC when signing multiple documents together",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}