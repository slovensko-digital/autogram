package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.TsaServerMisconfiguredException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
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
        var checkedSource = new TimestampSourceChecker(() -> null).check(pki.tspSource(), pki.verifier());
        job.setTimestampEnabled(true, checkedSource.source());

        assertEquals(SignatureProfile.BASELINE_T, job.getParameters().getSignatureProfile());
        var signed = job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null).getDocument();
        var reports = SignatureValidator.getInstance().validate(SignedDocumentValidator.fromDocument(signed));
        var simple = reports.getSimpleReport();
        var id = simple.getFirstSignatureId();

        assertEquals(SignatureQualification.QESIG, reports.getDetailedReport().getSignatureQualification(id));
        assertEquals(1, simple.getSignatureTimestamps(id).size());
        assertEquals(TimestampQualification.QTSA, simple.getTimestampQualification(simple.getSignatureTimestamps(id).getFirst().getId()));
    }

    @ParameterizedTest
    @EnumSource(value = SignatureForm.class, names = {"PAdES", "XAdES", "CAdES"})
    void disablingTimestampOverridesThePreparedLocalProfile(SignatureForm form) throws IOException {
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_T, form), null);
        job.setTimestampEnabled(false, null);

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

    @Test
    void aNonQualifiedFallbackCannotBeSavedAsTheAdvertisedQualifiedTimestamp() throws IOException {
        var requests = new AtomicInteger();
        TSPSource source = (algorithm, digest) -> (requests.getAndIncrement() == 0 ? pki.tspSource() : pki.nonQualifiedTspSource())
                .getTimeStampResponse(algorithm, digest);
        var checked = new TimestampSourceChecker(() -> null).check(source, pki.verifier());
        assertEquals(TimestampQualification.QTSA, checked.qualification());
        var responder = mock(Responder.class);
        var job = SigningJob.fromLocalInput(input(SignatureProfile.BASELINE_B, SignatureForm.XAdES), responder);
        job.setTimestampEnabled(true, checked.source());

        assertThrows(TsaServerMisconfiguredException.class,
                () -> job.signWithKeyAndRespond(pki.signer(TestPki.JANA).signingKey(), null));
        verifyNoInteractions(responder);
    }

    @ParameterizedTest
    @EnumSource(value = SignatureProfile.class, names = {"BASELINE_B", "BASELINE_T"})
    void websiteRequestsCannotBeOverridden(SignatureProfile profile) throws IOException {
        var job = SigningJob.fromInput(input(profile, SignatureForm.XAdES), null);

        assertFalse(job.canSelectTimestamp());
        assertThrows(IllegalStateException.class, () -> job.setTimestampEnabled(true, pki.tspSource()));
        assertThrows(IllegalStateException.class, () -> job.setTimestampEnabled(false, null));
        assertEquals(profile, job.getParameters().getSignatureProfile());
    }

    private static SigningInput input(SignatureProfile profile, SignatureForm form) throws IOException {
        var document = AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent("sample.pdf"), "test.pdf", MimeTypeEnum.PDF), null);
        var parameters = SigningParametersResolver.buildRequested(profile, form, DigestAlgorithm.SHA256,
                form == SignatureForm.PAdES ? null : ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                true, null, null, null, false, 640, true);
        return SigningInput.fromDocument(document, parameters);
    }
}
