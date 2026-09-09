package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.SignerTextPosition;

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
    void buildsSigningRequestWithAutomaticallyLoadedEform() {
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

        var request = body.getSigningRequest(null, false);

        assertFalse(request.isMultiDocument());
        assertEquals(1, request.getDocumentCount());
        assertNotNull(request.getParameters().getTransformation());
    }

    @Test
    void buildsSinglePadesSigningRequestWithVisibleSignature() throws IOException {
        var samplePdf = Base64.getEncoder().encodeToString(
                getClass().getResourceAsStream("../../sample.pdf").readAllBytes());

        var body = gson.fromJson("""
                {
                  "document": {
                    "filename": "sample.pdf",
                    "content": "%s",
                    "mimeType": "application/pdf;base64",
                    "visibleSignature": {
                      "fieldId": "signature-field-123",
                      "text": "John Smith",
                      "image": {
                        "filename": "signature.png",
                        "content": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wn0lKsAAAAASUVORK5CYII=",
                        "mimeType": "image/png;base64"
                      }
                    }
                  },
                  "parameters": {
                    "format": "PAdES"
                  }
                }
                """.formatted(samplePdf), VersionedSignRequestBody.class);

        var request = body.getSigningRequest(null, true);
        var imageParameters = request.getParameters().getPAdESSignatureParameters().getImageParameters();

        assertNotNull(imageParameters);
        assertEquals("signature-field-123", imageParameters.getFieldParameters().getFieldId());
        assertEquals("John Smith", imageParameters.getTextParameters().getText());
        assertEquals(SignerTextPosition.BOTTOM, imageParameters.getTextParameters().getSignerTextPosition());
        assertNotNull(imageParameters.getImage());
    }

    @Test
    void rejectsVisibleSignatureForNonPadesRequest() throws IOException {
        var samplePdf = Base64.getEncoder().encodeToString(
                getClass().getResourceAsStream("../../sample.pdf").readAllBytes());

        var body = gson.fromJson("""
                {
                  "document": {
                    "filename": "sample.pdf",
                    "content": "%s",
                    "mimeType": "application/pdf;base64",
                    "visibleSignature": {
                      "fieldId": "signature-field-123",
                      "text": "John Smith"
                    }
                  },
                  "parameters": {
                    "format": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """.formatted(samplePdf), VersionedSignRequestBody.class);

        var exception = assertThrows(RequestValidationException.class, () -> body.getSigningRequest(null, true));

        assertEquals("Visible signature is supported only for single-document PAdES signing",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
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