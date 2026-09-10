package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.ui.SupportedLanguage;
public class VersionedSignRequestBodyTests {
    private final Gson gson = new Gson();

    @Test
    void buildsMultiDocumentSigningInput() {
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

        var request = body.getSigningInput(null, true);

        assertTrue(request.isMultiDocument());
        assertEquals(2, request.getDocumentCount());
        assertEquals("first.txt", request.getFirstDocument().getName());
        assertEquals(eu.europa.esig.dss.enumerations.ASiCContainerType.ASiC_E, request.getParameters().getContainer());
    }

    @Test
    void buildsSingleDocumentSigningInput() {
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

        var request = body.getSigningInput(null, true);

        assertFalse(request.isMultiDocument());
        assertEquals(1, request.getDocumentCount());
        assertEquals("only.txt", request.getSingleDocument().getName());
    }

    @Test
    void buildsEformAsiceSigningInputWithoutFilename() throws IOException {
        var content = Base64.getEncoder().encodeToString(
          Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/general_agenda.asice")));
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "mimeType": "application/vnd.etsi.asic-e+zip; base64",
                      "content": "%s"
                    }
                  ]
                }
                """.formatted(content), VersionedSignRequestBody.class);

        var request = body.getSigningInput(null, false);

        assertNotNull(request.getSingleDocument().getEFormAttributes().transformation());
    }

    @Test
    void buildsSigningInputWithAutomaticallyLoadedEform() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "filename": "document.xml",
                      "mimeType": "application/xml",
                      "xdcParameters": {
                        "autoLoadEform": true
                      },
                      "content": "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?><GeneralAgenda xmlns=\\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda>"
                    }
                  ],
                  "parameters": {
                    "format": "XAdES"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningInput(null, false);

        assertFalse(request.isMultiDocument());
        assertEquals(1, request.getDocumentCount());
        assertNotNull(request.getSingleDocument().getEFormAttributes().transformation());
    }

    @Test
    void keepsAutomaticallyLoadedEformOnLaterDocument() {
        var body = gson.fromJson("""
                {
                  "documents": [
                    {
                      "filename": "first.txt",
                      "content": "Zmlyc3Q=",
                      "mimeType": "text/plain;base64"
                    },
                    {
                      "filename": "document.xml",
                      "mimeType": "application/xml",
                      "xdcParameters": {
                        "autoLoadEform": true
                      },
                      "content": "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?><GeneralAgenda xmlns=\\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\\"><subject>Nové podanie</subject><text>Podávam toto nové podanie.</text></GeneralAgenda>"
                    }
                  ],
                  "parameters": {
                    "format": "XAdES"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningInput(null, false);

        assertNull(request.getDocuments().get(0).getEFormAttributes().transformation());
        assertNotNull(request.getDocuments().get(1).getEFormAttributes().transformation());
    }

    @Test
    void rejectsNestedAsiceInMultiDocumentSigningInput() {
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

        var exception = assertThrows(RequestValidationException.class, () -> body.getSigningInput(null, true));

        assertEquals("Documents[1].MimeType must not be ASiC when signing multiple documents together",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}