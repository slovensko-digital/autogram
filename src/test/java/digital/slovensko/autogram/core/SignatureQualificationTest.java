package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.reports.Reports;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Qualification of signatures, seals and timestamps against the offline test PKI instead of the EU trusted lists. */
class SignatureQualificationTest {
    private static TestPki pki;

    @BeforeAll
    static void useTestPki() {
        pki = TestPki.create();
        SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
    }

    @AfterAll
    static void useTrustedLists() {
        SignatureValidator.getInstance().useCustomVerifier(null);
    }

    @Test
    void qualifiedCertificateGivesQualifiedSignature() throws Exception {
        var reports = validate(sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_B, null));
        var id = signatureId(reports);

        assertEquals(Indication.TOTAL_PASSED, reports.getSimpleReport().getIndication(id));
        assertEquals(SignatureQualification.QESIG, reports.getDetailedReport().getSignatureQualification(id));
        assertTrue(reports.getSimpleReport().getSignatureTimestamps(id).isEmpty());
    }

    @Test
    void qualifiedTimestampIsQualified() throws Exception {
        var reports = validate(sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_T, pki.tspSource()));
        var id = signatureId(reports);
        var simple = reports.getSimpleReport();

        assertEquals(SignatureQualification.QESIG, reports.getDetailedReport().getSignatureQualification(id));
        var timestamps = simple.getSignatureTimestamps(id);
        assertEquals(1, timestamps.size());
        assertEquals(TimestampQualification.QTSA, simple.getTimestampQualification(timestamps.get(0).getId()));
    }

    @Test
    void sealCertificateGivesQualifiedSeal() throws Exception {
        var reports = validate(sign(pki.signer(TestPki.SEAL), SignatureProfile.BASELINE_B, null));
        var id = signatureId(reports);

        assertEquals(Indication.TOTAL_PASSED, reports.getSimpleReport().getIndication(id));
        assertEquals(SignatureQualification.QESEAL, reports.getDetailedReport().getSignatureQualification(id));
    }

    @Test
    void certificateWithoutQcStatementsIsNotQualified() throws Exception {
        var signer = pki.issue("Nekvalifikovaný Podpisovateľ", TestPki.Kind.NOT_QUALIFIED);
        var reports = validate(sign(signer, SignatureProfile.BASELINE_B, null));
        var id = signatureId(reports);

        assertEquals(Indication.TOTAL_PASSED, reports.getSimpleReport().getIndication(id));
        // without QcType DSS cannot tell a signature from a seal, Autogram shows it as an unknown signature
        assertEquals(SignatureQualification.UNKNOWN, reports.getDetailedReport().getSignatureQualification(id));
    }

    @Test
    void revokedCertificateIsNotValid() throws Exception {
        var signer = pki.issue("Zneplatnený Podpisovateľ", TestPki.Kind.QESIG);
        var signed = sign(signer, SignatureProfile.BASELINE_B, null);
        pki.revoke(signer);
        var reports = validate(signed);
        var id = signatureId(reports);

        assertNotEquals(Indication.TOTAL_PASSED, reports.getSimpleReport().getIndication(id));
        assertNotEquals(SignatureQualification.QESIG, reports.getDetailedReport().getSignatureQualification(id));
    }

    @Test
    void trustedListsCountAsLoadedWithCustomVerifier() {
        assertTrue(SignatureValidator.getInstance().areTLsLoaded());
    }

    private static DSSDocument sign(TestPki.Signer signer, SignatureProfile profile, TSPSource tspSource) throws Exception {
        var pdf = AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "zmluva.pdf", MimeTypeEnum.PDF), null);
        var input = SigningInput.prepareForASiCWithXAdES(pdf, parameters(profile));
        return SigningJob.fromInput(input, responder()).signWithKey(signer.signingKey(), tspSource).getDocument();
    }

    private static Reports validate(DSSDocument signed) throws Exception {
        var container = new InMemoryDocument(signed.openStream().readAllBytes(), "zmluva.asice", MimeTypeEnum.ASICE);
        var input = SigningInput.prepareForASiCWithXAdES(AutogramDocument.build(container, null), parameters(SignatureProfile.BASELINE_B));
        var reports = SignatureValidator.getInstance().getSignatureValidationReport(SigningJob.fromInput(input, responder()));
        assertEquals(1, reports.getDocumentReports().size());
        return reports.getDocumentReports().get(0).reports();
    }

    private static String signatureId(Reports reports) {
        var ids = reports.getSimpleReport().getSignatureIdList();
        assertEquals(1, ids.size());
        return ids.get(0);
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
                fail(error);
            }
        };
    }
}
