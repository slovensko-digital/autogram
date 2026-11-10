package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Signs a sample PDF into an ASiC-E XAdES container with a {@link TestPki} signer and validates it with
 * {@link SignatureValidator} (set it up with {@code useCustomVerifier(pki.verifier())} first).
 */
public class TestSignedDocuments {
    public static DSSDocument sign(TestPki.Signer signer, SignatureProfile profile, TSPSource tspSource) {
        try {
            var pdf = AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "zmluva.pdf", MimeTypeEnum.PDF), null);
            var input = SigningInput.prepareForASiCWithXAdES(pdf, parameters(profile));
            return SigningJob.fromInput(input, responder()).signWithKey(signer.signingKey(), tspSource).getDocument();
        } catch (IOException | AutogramException e) {
            throw new IllegalStateException("cannot sign the test document", e);
        }
    }

    /** The sample PDF signed as Adobe Acrobat and DocuSign do by default, with the adbe.pkcs7.detached sub-filter. */
    public static DSSDocument pkcs7Pdf(TestPki.Signer signer) {
        var parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        parameters.setSubFilter("adbe.pkcs7.detached");
        try (var token = signer.token()) {
            var pdf = new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "zmluva.pdf", MimeTypeEnum.PDF);
            var key = token.getKeys().get(0);
            parameters.setSigningCertificate(key.getCertificate());
            parameters.setCertificateChain(key.getCertificateChain());
            var service = new PAdESService(new CommonCertificateVerifier());
            var signature = token.sign(service.getDataToSign(pdf, parameters), parameters.getDigestAlgorithm(), key);
            return service.signDocument(pdf, parameters, signature);
        } catch (IOException e) {
            throw new IllegalStateException("cannot sign the test document", e);
        }
    }

    /** Report of the single signed document in the container, as the signing window gets it. */
    public static ValidationReports.DocumentReport validate(DSSDocument signed) {
        try {
            var container = new InMemoryDocument(signed.openStream().readAllBytes(), "zmluva.asice", MimeTypeEnum.ASICE);
            return validate(SigningInput.prepareForASiCWithXAdES(AutogramDocument.build(container, null), parameters(SignatureProfile.BASELINE_B)));
        } catch (IOException | AutogramException e) {
            throw new IllegalStateException("cannot validate the test document", e);
        }
    }

    /** Report of a signed PDF, as the signing window gets it. */
    public static ValidationReports.DocumentReport validatePdf(DSSDocument signed) {
        try {
            var pdf = new InMemoryDocument(signed.openStream().readAllBytes(), "zmluva.pdf", MimeTypeEnum.PDF);
            return validate(SigningInput.fromFile(AutogramDocument.build(pdf, null), parameters(SignatureProfile.BASELINE_B)));
        } catch (IOException | AutogramException e) {
            throw new IllegalStateException("cannot validate the test document", e);
        }
    }

    private static ValidationReports.DocumentReport validate(SigningInput input) {
        var reports = SignatureValidator.getInstance().getSignatureValidationReport(SigningJob.fromInput(input, responder()));
        if (reports.getDocumentReports().size() != 1)
            throw new IllegalStateException("expected one signed document, got " + reports.getDocumentReports().size());
        return reports.getDocumentReports().get(0);
    }

    /** The same container with the signed PDF changed after signing, so its signature fails (hash mismatch). */
    public static DSSDocument tamper(DSSDocument signed) {
        try (var in = new ZipInputStream(signed.openStream())) {
            var out = new ByteArrayOutputStream();
            try (var zip = new ZipOutputStream(out)) {
                for (var entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                    var content = in.readAllBytes();
                    if (entry.getName().equals("zmluva.pdf"))
                        content = Arrays.copyOf(content, content.length + 1);
                    var copy = new ZipEntry(entry.getName());
                    if (entry.getName().equals("mimetype")) {
                        var crc = new CRC32();
                        crc.update(content);
                        copy.setMethod(ZipEntry.STORED);
                        copy.setSize(content.length);
                        copy.setCrc(crc.getValue());
                    }
                    zip.putNextEntry(copy);
                    zip.write(content);
                    zip.closeEntry();
                }
            }
            return new InMemoryDocument(out.toByteArray(), "zmluva.asice", MimeTypeEnum.ASICE);
        } catch (IOException e) {
            throw new IllegalStateException("cannot tamper with the test document", e);
        }
    }

    private static SigningParameters parameters(SignatureProfile profile) throws AutogramException {
        return SigningParameters.buildParameters(profile, SignatureForm.XAdES, DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING, false, null, null, null, false, 640, true);
    }

    private static Responder responder() {
        return new Responder() {
            @Override
            public void onDocumentSigned(SignedDocument signedDocument) {
            }

            @Override
            public void onDocumentSignFailed(AutogramException error) {
                throw new IllegalStateException(error);
            }
        };
    }
}
