package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import org.junit.jupiter.api.Test;

import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

/**
 * Reproduces the PAdES-vs-ASiC-E mismatch reached through the GUI/CLI file path:
 * {@code UserSettings.getDefaultSigningParameters()} always requests an ASiC-E container,
 * and {@code SigningParametersResolver.resolveLenientFromFile} never clears it for a raw PDF.
 */
class PadesContainerMismatchTest {

    private static AutogramDocument samplePdf() throws IOException {
        try (InputStream in = PadesContainerMismatchTest.class
                .getResourceAsStream("/digital/slovensko/autogram/sample.pdf")) {
            return AutogramDocument.build(
                    new InMemoryDocument(in.readAllBytes(), "sample.pdf", MimeTypeEnum.PDF), null);
        }
    }

    private static SigningParameters padesRequested(SignatureProfile profile) {
        return SigningParametersResolver.buildRequested(
                profile, SignatureForm.PAdES, DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                false, null, null, null, false, 640, false);
    }

    @Test
    void pdfForPadesMustNotKeepAsicEContainer() throws IOException {
        var resolved = SigningParametersResolver.resolveLenientFromFile(
                padesRequested(SignatureProfile.BASELINE_B), samplePdf());

        assertEquals(SignatureForm.PAdES, resolved.getSignatureForm());
        assertNull(resolved.getContainer(),
                "A PDF auto-detected for PAdES must resolve to a raw PDF (container == null), not ASiC-E");
    }

    @Test
    void pdfForPadesWithTsaMustStayPades() throws IOException {
        var resolved = SigningParametersResolver.resolveLenientFromFile(
                padesRequested(SignatureProfile.BASELINE_T), samplePdf());

        assertEquals(SignatureForm.PAdES, resolved.getSignatureForm(),
                "A PDF with TSA enabled (BASELINE_T) must stay PAdES, not silently become XAdES");
    }

    @Test
    void signingPdfAsPadesWithAsicEContainerMustNotThrowClassCastException() throws IOException {
        var input = SigningInput.fromFile(samplePdf(), padesRequested(SignatureProfile.BASELINE_B));
        assertEquals(ASiCContainerType.ASiC_E, input.getParameters().getContainer(),
                "precondition: the requested ASiC-E container survives lenient file resolution");

        var job = SigningJob.fromInput(input, mock(Responder.class));

        // A real certificate is required because DSS computes the encryption algorithm from it;
        // the signature itself stays mocked so no real signing happens.
        var key = mock(SigningKey.class);
        var keystore = PadesContainerMismatchTest.class
                .getResource("/digital/slovensko/autogram/core/test.keystore").getFile();
        try (var token = new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection(new char[0]))) {
            var testKey = token.getKeys().get(0);
            when(key.getCertificate()).thenReturn(testKey.getCertificate());
            when(key.getCertificateChain()).thenReturn(testKey.getCertificateChain());
        }

        var thrown = assertThrows(Throwable.class, () -> job.signWithKeyAndRespond(key, null),
                "the mocked signature value cannot produce a real signature, so signing is expected to fail eventually");

        assertFalse(thrown instanceof ClassCastException,
                "A PDF PAdES job carrying an ASiC-E container must not be cast to AbstractASiCSignatureService, "
                        + "but got " + thrown);
    }
}
