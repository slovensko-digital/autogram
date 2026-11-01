package digital.slovensko.autogram.core;

import digital.slovensko.autogram.TestMethodSources;
import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * Signing through temporary files instead of the heap, turned on by the --temp-files option: which signatures end up in a temporary file, and that no
 * temporary file is left behind after a successful, failed or repeated signing.
 * <p>
 * Documents are signed for real with a {@link TestPki} key. A cancelled PIN entry is simulated by a key that throws
 * when asked to sign, i.e. after DSS has already prepared the document. Leftovers are detected by comparing the
 * {@code autogram-signing-*} entries in the temporary directory before and after a test step.
 */
class SigningResourcesTest {
    private static final Path TEMP_DIRECTORY = Path.of(System.getProperty("java.io.tmpdir"));
    private static TestPki pki;
    private final List<SignedDocument> signedDocuments = new ArrayList<>();

    @BeforeAll
    static void createPki() {
        pki = TestPki.create();
    }

    @BeforeEach
    void useTemporaryFiles() {
        // as with the --temp-files option
        SigningResources.useTemporaryFiles();
    }

    @AfterEach
    void cleanUp() {
        // the container setting is a global DSS singleton, other tests expect the default in-memory one
        SigningResources.useMemory();
        // documents a test signed but did not hand over to a responder
        signedDocuments.forEach(signed -> SigningResources.deleteIfTemporary(signed.getDocument()));
    }

    /** The --temp-files option turns signing through temporary files on, here together with printing the help. */
    @Test
    void optionTurnsTemporaryFilesOn() {
        SigningResources.useMemory();

        AppStarter.start(new String[] {"--temp-files", "--help"});

        assertTrue(SigningResources.isEnabled());
    }

    /** Without the option, ASiC-E and PAdES are signed in memory as before and no temporary file is created. */
    @Test
    void withoutOptionDocumentsAreSignedInMemory() throws Exception {
        SigningResources.useMemory();
        var before = temporaryEntries();

        var container = sign(textJob(SignatureForm.XAdES, ASiCContainerType.ASiC_E, recordingResponder(new AtomicReference<>())));
        var pdf = sign(pdfJob(recordingResponder(new AtomicReference<>())));

        assertInstanceOf(InMemoryDocument.class, container.getDocument());
        assertInstanceOf(InMemoryDocument.class, pdf.getDocument());
        assertEquals(before, temporaryEntries());
    }

    /** XAdES in ASiC-E: the ZIP container is written to a temporary file, not to a byte array. */
    @Test
    void asicContainerIsBuiltInTemporaryFile() throws Exception {
        var signed = sign(textJob(SignatureForm.XAdES, ASiCContainerType.ASiC_E, recordingResponder(new AtomicReference<>())));

        assertTemporaryFileStartingWith(signed, "PK");
    }

    /** CAdES in ASiC-E goes through the same ZIP setting, CAdES without a container (.p7m) still signs in memory. */
    @Test
    void asicWithCadesContainerIsBuiltInTemporaryFile() throws Exception {
        var signed = sign(textJob(SignatureForm.CAdES, ASiCContainerType.ASiC_E, recordingResponder(new AtomicReference<>())));

        assertTemporaryFileStartingWith(signed, "PK");
    }

    /** PAdES needs no DSS singleton, each signing passes its own PDF factory to the service. */
    @Test
    void padesSignatureIsWrittenToTemporaryFile() throws Exception {
        var signed = sign(pdfJob(recordingResponder(new AtomicReference<>())));

        assertTemporaryFileStartingWith(signed, "%PDF");
    }

    /** DSS writes an intermediate PDF already while preparing the data to sign, it is deleted once signed. */
    @Test
    void padesSigningKeepsOnlyTheSignedDocument() throws Exception {
        var signed = sign(pdfJob(recordingResponder(new AtomicReference<>())));

        var directory = ((FileDocument) signed.getDocument()).getFile().toPath().getParent();
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    /** The responder still reads the whole signed document, the file and its directory are deleted only afterwards. */
    @Test
    void temporarySignedDocumentIsDeletedAfterResponderGetsIt() throws Exception {
        var before = temporaryEntries();
        var received = new AtomicReference<byte[]>();
        var job = pdfJob(recordingResponder(received));
        var signed = sign(job);
        var content = Files.readAllBytes(((FileDocument) signed.getDocument()).getFile().toPath());

        assertTrue(job.onJobSigned(signed));

        assertArrayEquals(content, received.get());
        assertEquals(before, temporaryEntries());
    }

    /** A signature finished after the result was delivered, e.g. the window was closed meanwhile, is dropped. */
    @Test
    void temporarySignedDocumentIsDeletedWhenResultWasAlreadyDelivered() throws Exception {
        var job = pdfJob(recordingResponder(new AtomicReference<>()));
        job.onJobSigned(sign(job));
        var before = temporaryEntries();
        var late = sign(job);

        assertFalse(job.onJobSigned(late));

        assertEquals(before, temporaryEntries());
    }

    /** The PIN entry is cancelled after DSS prepared the PDF, the whole directory of the signing is deleted. */
    @Test
    void failedPadesSigningLeavesNoTemporaryFiles() throws Exception {
        var before = temporaryEntries();
        var job = pdfJob(recordingResponder(new AtomicReference<>()));

        assertThrows(IllegalStateException.class, () -> job.signWithKey(failingKey(), null));

        assertEquals(before, temporaryEntries());
    }

    /** The same for ASiC-E, whose container is built in the shared temporary directory, not in the signing's own. */
    @Test
    void failedAsicSigningLeavesNoTemporaryFiles() throws Exception {
        var before = temporaryEntries();
        var job = textJob(SignatureForm.XAdES, ASiCContainerType.ASiC_E, recordingResponder(new AtomicReference<>()));

        assertThrows(IllegalStateException.class, () -> job.signWithKey(failingKey(), null));

        assertEquals(before, temporaryEntries());
    }

    /** Each attempt of the same job prepares the PDF again, none of them may leave a file behind. */
    @Test
    void repeatedAttemptsLeaveNoTemporaryFiles() throws Exception {
        var before = temporaryEntries();
        var job = pdfJob(recordingResponder(new AtomicReference<>()));
        var key = failingKey();

        // like a wrong PIN entered twice before the right one, see Autogram.signWithPINRetry
        assertThrows(IllegalStateException.class, () -> job.signWithKey(key, null));
        assertThrows(IllegalStateException.class, () -> job.signWithKey(key, null));
        doCallRealMethod().when(key).sign(any(), any());
        job.onJobSigned(job.signWithKey(key, null));

        assertEquals(before, temporaryEntries());
    }

    /** Only files named by Autogram's signing are deleted, never a file the user opened, even in the temp directory. */
    @Test
    void otherFilesAreNotDeleted() throws Exception {
        var userFile = Files.createTempFile("zmluva", ".pdf");
        try {
            SigningResources.deleteIfTemporary(new FileDocument(userFile.toFile()));
            SigningResources.deleteIfTemporary(new InMemoryDocument(new byte[] {1}, "zmluva.pdf"));

            assertTrue(Files.exists(userFile));
        } finally {
            Files.deleteIfExists(userFile);
        }
    }

    private SignedDocument sign(SigningJob job) {
        var signed = job.signWithKey(pki.signer(TestPki.JANA).signingKey(), null);
        assertNotNull(signed.getDocument());
        signedDocuments.add(signed);
        return signed;
    }

    /** A key whose signing fails like a cancelled PIN entry, after DSS has prepared the document. */
    private static SigningKey failingKey() {
        var key = spy(pki.signer(TestPki.JANA).signingKey());
        doThrow(new IllegalStateException("PIN entry cancelled")).when(key).sign(any(), any());
        return key;
    }

    private static SigningJob pdfJob(Responder responder) throws IOException {
        return job("sample.pdf", MimeTypeEnum.PDF, SignatureForm.PAdES, null, null, responder);
    }

    private static SigningJob textJob(SignatureForm form, ASiCContainerType container, Responder responder) throws IOException {
        return job("sample.txt", MimeTypeEnum.TEXT, form, container, SignaturePackaging.ENVELOPING, responder);
    }

    private static SigningJob job(String resource, MimeType mimeType, SignatureForm form, ASiCContainerType container,
            SignaturePackaging packaging, Responder responder) throws IOException {
        var document = AutogramDocument.build(new InMemoryDocument(TestMethodSources.loadContent(resource), resource, mimeType), null);
        var parameters = SigningParameters.buildParameters(SignatureProfile.BASELINE_B, form, DigestAlgorithm.SHA256, container,
                packaging, false, null, null, null, false, 640, true);
        return SigningJob.fromInput(SigningInput.of(List.of(document), parameters), responder);
    }

    /** Files and directories of Autogram's signing in the temporary directory. */
    private static Set<Path> temporaryEntries() throws IOException {
        try (var entries = Files.list(TEMP_DIRECTORY)) {
            return entries.filter(entry -> entry.getFileName().toString().startsWith("autogram-signing-"))
                    .collect(Collectors.toSet());
        }
    }

    private static void assertTemporaryFileStartingWith(SignedDocument signed, String header) throws IOException {
        var document = assertInstanceOf(FileDocument.class, signed.getDocument());
        assertTrue(SigningResources.isTemporary(document));
        var start = new String(Files.readAllBytes(document.getFile().toPath()), 0, header.length());
        assertEquals(header, start);
    }

    private static Responder recordingResponder(AtomicReference<byte[]> received) {
        return new Responder() {
            @Override
            public void onDocumentSigned(SignedDocument signedDocument) {
                try (var in = signedDocument.getDocument().openStream()) {
                    received.set(in.readAllBytes());
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void onDocumentSignFailed(AutogramException error) {
                throw new IllegalStateException(error);
            }
        };
    }
}
