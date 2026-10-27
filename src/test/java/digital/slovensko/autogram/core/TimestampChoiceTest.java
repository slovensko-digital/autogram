package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.SignatureRequirementsNotMetException;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.cades.signature.CAdESCounterSignatureParameters;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TimestampChoiceTest {
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

    @ParameterizedTest
    @EnumSource(value = SignatureForm.class, names = {"PAdES", "XAdES", "CAdES"})
    void enablingTimestampChangesTheActualSignatureNotOnlyThePreview(SignatureForm form) throws IOException {
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_B, form), null);
        var requests = new AtomicInteger();
        TSPSource source = (algorithm, digest) -> {
            requests.incrementAndGet();
            return pki.tspSource().getTimeStampResponse(algorithm, digest);
        };
        job.setTimestampEnabled(true, source);
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, true));

        assertEquals(0, requests.get(), "selecting a goal must not request a trial timestamp");
        assertEquals(SignatureProfile.BASELINE_T, job.getParameters().getSignatureProfile());
        var signed = job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null).getDocument();
        var reports = SignatureValidator.getInstance().validate(SignedDocumentValidator.fromDocument(signed));
        var simple = reports.getSimpleReport();
        var id = simple.getFirstSignatureId();

        assertEquals(SignatureQualification.QESIG, reports.getDetailedReport().getSignatureQualification(id));
        assertEquals(1, simple.getSignatureTimestamps(id).size());
        assertEquals(TimestampQualification.QTSA, simple.getTimestampQualification(simple.getSignatureTimestamps(id).getFirst().getId()));
        assertEquals(1, requests.get(), "only the real signature requests a timestamp");
    }

    @ParameterizedTest
    @EnumSource(value = SignatureForm.class, names = {"PAdES", "XAdES", "CAdES"})
    void disablingTimestampOverridesThePreparedLocalProfile(SignatureForm form) throws IOException {
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_T, form), null);
        job.setTimestampEnabled(false, null);
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, false));

        var signed = job.signWithKey(pki.signer(TestPki.JANA).signingKey(), pki.tspSource()).getDocument();
        var simple = SignatureValidator.getInstance().validate(SignedDocumentValidator.fromDocument(signed)).getSimpleReport();

        assertEquals(SignatureProfile.BASELINE_B, job.getParameters().getSignatureProfile());
        assertTrue(simple.getSignatureTimestamps(simple.getFirstSignatureId()).isEmpty());
    }

    @Test
    void choiceDoesNotChangeOtherJobsOrOriginalParameters() throws IOException {
        var input = input(SignatureProfile.BASELINE_B, SignatureForm.XAdES);
        var job = SigningJob.fromLocalInput(input, null);
        var otherJob = SigningJob.fromLocalInput(input, null);
        var original = input.getParameters();
        job.setTimestampEnabled(true, pki.tspSource());

        assertSame(original, input.getParameters());
        assertSame(original, otherJob.getParameters());
        assertEquals(SignatureProfile.BASELINE_B, otherJob.getParameters().getSignatureProfile());
        assertEquals(original.getSignatureForm(), job.getParameters().getSignatureForm());
        assertEquals(original.getContainer(), job.getParameters().getContainer());
        assertEquals(original.getDigestAlgorithm(), job.getParameters().getDigestAlgorithm());
        assertEquals(original.getSignaturePackaging(), job.getParameters().getSignaturePackaging());
        assertEquals(original.getPropertiesCanonicalization(), job.getParameters().getPropertiesCanonicalization());
        assertEquals(input.getDocuments(), job.getDocuments());
    }

    @ParameterizedTest
    @EnumSource(value = SignatureForm.class, names = {"PAdES", "XAdES", "CAdES"})
    void aNonQualifiedTimestampCannotBeSavedAsTheRequestedNotarizedSignature(SignatureForm form) throws IOException {
        var responder = mock(Responder.class);
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_B, form), responder);
        job.setTimestampEnabled(true, pki.nonQualifiedTspSource());
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, true));

        assertThrows(SignatureRequirementsNotMetException.class,
                () -> job.signWithKeyAndRespond(pki.signer(TestPki.JANA).signingKey(), null));
        verifyNoInteractions(responder);
    }

    @ParameterizedTest
    @EnumSource(value = SignatureForm.class, names = {"PAdES", "XAdES", "CAdES"})
    void recognizedAuthorizationAndSealsMeetTheirSelectedGoals(SignatureForm form) throws IOException {
        for (var kind : new TestPki.Kind[]{TestPki.Kind.ADESIG_QC, TestPki.Kind.QESEAL}) {
            var signer = pki.issue("Selected " + kind, kind);
            var qualification = kind == TestPki.Kind.ADESIG_QC
                    ? CertificateQualification.QCERT_FOR_ESIG : CertificateQualification.QCERT_FOR_ESEAL_QSCD;
            for (var timestamp : new boolean[]{false, true}) {
                var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_B, form), null);
                job.setTimestampEnabled(timestamp, pki.tspSource());
                job.setSignatureRequirements(SignatureRequirements.fromCertificate(qualification, timestamp));

                assertNotNull(job.signWithKey(signer.signingKey(), null));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SignatureForm.class, names = {"PAdES", "XAdES", "CAdES"})
    void existingSignaturesDoNotSatisfyTheGoalForTheNewSignature(SignatureForm form) throws IOException {
        var original = input(SignatureProfile.BASELINE_T, form);
        var firstJob = SigningJob.fromLocalInput(original, null);
        var first = firstJob.signWithKey(pki.signer(TestPki.JANA).signingKey(), pki.tspSource());
        var document = AutogramDocument.build(first.getDocument(), null);
        var responder = mock(Responder.class);
        var job = SigningJob.fromLocalInput(SigningInput.fromDocument(document, original.getParameters()), responder);
        job.setTimestampEnabled(true, pki.tspSource());
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, true));
        var weaker = pki.issue("No QSCD " + form, TestPki.Kind.ADESIG_QC);

        assertThrows(SignatureRequirementsNotMetException.class, () -> job.signWithKeyAndRespond(weaker.signingKey(), null));
        verifyNoInteractions(responder);

        // The same container can still be signed successfully with the right certificate after a failed attempt.
        assertNotNull(job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null));
    }

    @Test
    void recognizedAuthorizationWithANonQualifiedTimestampDoesNotMeetTheSelectedGoal() throws IOException {
        var signer = pki.issue("Recognized with wrong timestamp", TestPki.Kind.ADESIG_QC);
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_B, SignatureForm.XAdES), null);
        job.setTimestampEnabled(true, pki.nonQualifiedTspSource());
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG, true));

        assertThrows(SignatureRequirementsNotMetException.class, () -> job.signWithKey(signer.signingKey(), null));
    }

    @Test
    void anUnavailableQualificationCannotSatisfyAKnownSelectedGoal() throws IOException {
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_B, SignatureForm.XAdES), null);
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, false));
        SignatureValidator.getInstance().useCustomVerifier(NetworkClients.certificateVerifier());
        try {
            assertThrows(SignatureRequirementsNotMetException.class,
                    () -> job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null));
        } finally {
            SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
        }
    }

    @ParameterizedTest
    @EnumSource(value = SignatureProfile.class, names = {"BASELINE_B", "BASELINE_T"})
    void websiteRequestsCannotBeOverridden(SignatureProfile profile) throws IOException {
        var job = SigningJob.fromInput(input(profile, SignatureForm.XAdES), null);

        assertFalse(job.canSelectTimestamp());
        assertThrows(IllegalStateException.class, () -> job.setTimestampEnabled(true, pki.tspSource()));
        assertThrows(IllegalStateException.class, () -> job.setTimestampEnabled(false, null));
        assertThrows(IllegalStateException.class, () -> job.setSignatureRequirements(
                SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, true)));
        assertEquals(profile, job.getParameters().getSignatureProfile());
    }

    @Test
    void standaloneCadesCosigningUsesSignatureValuesInsteadOfChangingDssIds() {
        var parameters = standaloneCadesParameters();
        var originalInput = SigningInput.fromDocument(AutogramDocument.build(
                new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null), parameters);
        var original = SigningJob.fromInput(originalInput, null).signWithKey(pki.signer(TestPki.SEAL).signingKey(), null).getDocument();
        var fingerprints = SignatureRequirements.signatureFingerprints(original);
        var input = SigningInput.fromFile(AutogramDocument.build(original, null), parameters);
        var job = SigningJob.fromLocalInput(input, null);
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESIG_QSCD, false));

        var signed = job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null).getDocument();
        var simple = validate(signed);
        assertEquals(2, simple.getSignatureIdList().size());
        assertTrue(simple.getSignatureIdList().stream().allMatch(simple::isValid));
        assertTrue(SignatureRequirements.signatureFingerprints(signed).containsAll(fingerprints));

        // The old seal must not satisfy the goal of the newly added natural-person signature.
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESEAL_QSCD, false));
        assertThrows(SignatureRequirementsNotMetException.class,
                () -> job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null));
    }

    @Test
    void existingCounterSignaturesAreNotMistakenForNewSignatures() {
        var parameters = standaloneCadesParameters();
        var originalInput = SigningInput.fromDocument(AutogramDocument.build(
                new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null), parameters);
        var original = SigningJob.fromInput(originalInput, null).signWithKey(pki.signer(TestPki.JANA).signingKey(), null).getDocument();
        var counterParameters = new CAdESCounterSignatureParameters();
        counterParameters.setSignatureIdToCounterSign(validate(original).getFirstSignatureId());
        counterParameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
        counterParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        var counterKey = pki.signer(TestPki.PETER).signingKey();
        counterParameters.setSigningCertificate(counterKey.getCertificate());
        counterParameters.setCertificateChain(counterKey.getCertificateChain());
        var service = new CAdESService(NetworkClients.certificateVerifier());
        var data = service.getDataToBeCounterSigned(original, counterParameters);
        var countersigned = service.counterSignSignature(original, counterParameters, counterKey.sign(data, DigestAlgorithm.SHA256));
        var fingerprints = SignatureRequirements.signatureFingerprints(countersigned);
        assertEquals(2, fingerprints.size());
        var input = SigningInput.fromFile(AutogramDocument.build(countersigned, null), parameters);
        var job = SigningJob.fromLocalInput(input, null);
        job.setSignatureRequirements(SignatureRequirements.fromCertificate(CertificateQualification.QCERT_FOR_ESEAL_QSCD, false));

        var signed = job.signWithKey(pki.signer(TestPki.SEAL).signingKey(), null).getDocument();
        var simple = validate(signed);
        assertEquals(3, simple.getSignatureIdList().size());
        assertTrue(simple.getSignatureIdList().stream().allMatch(simple::isValid));
        assertTrue(SignatureRequirements.signatureFingerprints(signed).containsAll(fingerprints));
    }

    private static SimpleReport validate(DSSDocument document) {
        return SignatureValidator.getInstance().validate(SignedDocumentValidator.fromDocument(document)).getSimpleReport();
    }

    private static SigningParameters standaloneCadesParameters() {
        return SigningParametersResolver.buildRequested(SignatureProfile.BASELINE_B, SignatureForm.CAdES, DigestAlgorithm.SHA256,
                null, SignaturePackaging.ENVELOPING, true, null, null, null, false, 640, true);
    }

    private static SigningInput input(SignatureProfile profile, SignatureForm form) throws IOException {
        var document = AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParametersResolver.buildRequested(profile, form, DigestAlgorithm.SHA256,
                form == SignatureForm.PAdES ? null : ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                true, null, null, null, false, 640, true);
        return SigningInput.fromDocument(document, parameters);
    }
}
