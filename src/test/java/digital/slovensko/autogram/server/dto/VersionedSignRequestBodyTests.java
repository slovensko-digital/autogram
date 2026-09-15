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
import eu.europa.esig.dss.enumerations.SignatureLevel;
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
                    "form": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningInput(true);

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
                    "form": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningInput(true);

        assertFalse(request.isMultiDocument());
        assertEquals(1, request.getDocumentCount());
        assertEquals("only.txt", request.getSingleDocument().getName());
    }

    @Test
    void buildsPadesSigningInputWithPresentationParameters() throws IOException {
        var content = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/sample.pdf")));
        var body = gson.fromJson("""
                {
                  "parameters": {
                    "form": "PAdES",
                    "profile": "BASELINE_T",
                    "checkPDFACompliance": true
                  },
                  "presentation": {
                    "visualizationWidth": "lg"
                  },
                  "documents": [
                    {
                      "mimeType": "application/pdf; base64",
                      "content": "%s"
                    }
                  ]
                }
                """.formatted(content), VersionedSignRequestBody.class);

        var request = body.getSigningInput(false);

        assertEquals(SignatureLevel.PAdES_BASELINE_T, request.getParameters().getLevel());
        assertNull(request.getParameters().getContainer());
        assertTrue(request.getParameters().getCheckPDFACompliance());
        assertEquals(1024, request.getParameters().getVisualizationWidth());
    }

    @Test
    void signedPdfInheritsSignatureFormWhenItIsOmitted() throws IOException {
        var content = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/sample_signed.pdf")));
        var body = gson.fromJson("""
                {
                  "document": {
                    "mimeType": "application/pdf; base64",
                    "content": "%s"
                  }
                }
                """.formatted(content), VersionedSignRequestBody.class);

        var request = body.getSigningInput(false);

        assertEquals(SignatureLevel.PAdES_BASELINE_B, request.getParameters().getLevel());
        assertNull(request.getParameters().getContainer());
    }

    @Test
    void signedPdfAllowsDifferentRequestedProfile() throws IOException {
        var content = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/sample_signed.pdf")));
        var body = gson.fromJson("""
                {
                  "parameters": {
                    "profile": "BASELINE_T"
                  },
                  "document": {
                    "mimeType": "application/pdf; base64",
                    "content": "%s"
                  }
                }
                """.formatted(content), VersionedSignRequestBody.class);

        var request = body.getSigningInput(false);

        assertEquals(SignatureLevel.PAdES_BASELINE_T, request.getParameters().getLevel());
    }

    @Test
    void rejectsIncompatibleSignatureFormForSignedPdf() throws IOException {
        var content = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/sample_signed.pdf")));
        var body = gson.fromJson("""
                {
                  "parameters": {
                    "form": "XAdES"
                  },
                  "document": {
                    "mimeType": "application/pdf; base64",
                    "content": "%s"
                  }
                }
                """.formatted(content), VersionedSignRequestBody.class);

        assertThrows(RequestValidationException.class, () -> body.getSigningInput(false));
    }

    @Test
    void rejectsPackagingOverrideForSignedPdf() throws IOException {
        var content = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/sample_signed.pdf")));
        var body = gson.fromJson("""
                {
                  "parameters": {
                    "packaging": "ENVELOPING"
                  },
                  "document": {
                    "mimeType": "application/pdf; base64",
                    "content": "%s"
                  }
                }
                """.formatted(content), VersionedSignRequestBody.class);

        assertThrows(RequestValidationException.class, () -> body.getSigningInput(false));
    }

    @Test
    void rejectsPackagingOverrideForSignedAsice() throws IOException {
        var content = Base64.getEncoder().encodeToString(
                Files.readAllBytes(Path.of("src/test/resources/digital/slovensko/autogram/sample_pdf_xades.asice")));
        var body = gson.fromJson("""
                {
                  "parameters": {
                    "packaging": "ENVELOPING"
                  },
                  "document": {
                    "mimeType": "application/vnd.etsi.asic-e+zip; base64",
                    "content": "%s"
                  }
                }
                """.formatted(content), VersionedSignRequestBody.class);

        assertThrows(RequestValidationException.class, () -> body.getSigningInput(false));
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
                      "xdcParameters": {
                        "autoLoadEform": true
                      },
                      "content": "%s"
                    }
                  ]
                }
                """.formatted(content), VersionedSignRequestBody.class);

        var request = body.getSigningInput(false);

        assertNotNull(request.getSingleDocument().getEFormAttributes().transformation());
    }

    @Test
    void infersAsiceContainerFromSingleAsiceDocument() throws IOException {
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

        var request = body.getSigningInput(false);

        assertEquals(eu.europa.esig.dss.enumerations.ASiCContainerType.ASiC_E,
                request.getParameters().getContainer());
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
                    "form": "XAdES"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningInput(false);

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
                    "form": "XAdES"
                  }
                }
                """, VersionedSignRequestBody.class);

        var request = body.getSigningInput(false);

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
                    "form": "XAdES",
                    "container": "ASiC_E"
                  }
                }
                """, VersionedSignRequestBody.class);

        var exception = assertThrows(RequestValidationException.class, () -> body.getSigningInput(true));

        assertEquals("Documents[1].MimeType must not be ASiC when signing multiple documents together",
                exception.getSubheading(SupportedLanguage.ENGLISH.loadResources()));
    }
}