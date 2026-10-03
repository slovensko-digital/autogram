package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.PDFUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class ProtectedPdfSigningTest {
    @Test
    void signPasswordProtectedPdfEndToEnd() throws Exception {
        var pdfBytes = createPasswordProtectedPdf();

        var document = AutogramDocument.build(
            new InMemoryDocument(pdfBytes, "locked.pdf", MimeTypeEnum.PDF), null);
        Assertions.assertEquals(PDFUtils.PDFProtection.OPEN_DOCUMENT_PASSWORD,
            PDFUtils.determinePDFProtection(document.toDssDocument()));

        // simulate Autogram.handleProtectedPdfDocument with a user-provided password
        document.setOpenDocumentPassword("user-password".toCharArray());
        Assertions.assertTrue(document.hasOpenDocumentPassword());

        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, SignatureForm.PAdES,
            DigestAlgorithm.SHA256, null, null, false, null, null, null, false, 640, true);
        var input = SigningInput.fromFile(document, parameters);

        var dssParams = DssSigningParametersFactory.createPAdESSignatureParameters(input);
        Assertions.assertArrayEquals("user-password".toCharArray(), dssParams.getPasswordProtection());

        var signedRef = new AtomicReference<SignedDocument>();
        var job = SigningJob.fromInput(input, new Responder() {
            @Override
            public void onDocumentSigned(SignedDocument signedDocument) {
                signedRef.set(signedDocument);
            }

            @Override
            public void onDocumentSignFailed(AutogramException e) {
                throw e;
            }
        });

        var keystore = Objects.requireNonNull(getClass().getResource("test.keystore")).getFile();
        try (var token = new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("".toCharArray()))) {
            job.signWithKeyAndRespond(new SigningKey(token, token.getKeys().get(0)), null);
        }

        Assertions.assertNotNull(signedRef.get());

        var validator = new PDFDocumentValidator(signedRef.get().getDocument());
        validator.setCertificateVerifier(new CommonCertificateVerifier());
        validator.setPasswordProtection("user-password".toCharArray());
        Assertions.assertEquals(1, validator.validateDocument().getSimpleReport().getSignaturesCount());
    }

    private static byte[] createPasswordProtectedPdf() throws IOException {
        try (var document = new PDDocument(); var outputStream = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());

            var permissions = new AccessPermission();
            var protectionPolicy = new StandardProtectionPolicy("owner-password", "user-password", permissions);
            protectionPolicy.setEncryptionKeyLength(128);
            protectionPolicy.setPermissions(permissions);
            document.protect(protectionPolicy);

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
