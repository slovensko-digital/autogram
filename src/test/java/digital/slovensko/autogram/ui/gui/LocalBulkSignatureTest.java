package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.CertificateStatusChecker;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnabledIfSystemProperty(named = "autogram.guiTests", matches = "true")
class LocalBulkSignatureTest {
    private static TestPki pki;

    @BeforeAll
    static void startJavaFx() throws Exception {
        pki = TestPki.create();
        SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
        var ready = new FutureTask<Void>(() -> { Platform.setImplicitExit(false); return null; });
        try {
            Platform.startup(ready);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @AfterAll
    static void restoreVerifier() {
        SignatureValidator.getInstance().useCustomVerifier(null);
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void bulkAppliesTheSelectedGoalToEveryFile(boolean rememberedKey, boolean timestamp, @TempDir Path directory) throws Exception {
        runBulk(rememberedKey, timestamp, false, directory);
    }

    @Test
    void bulkDoesNotSaveFilesWithAnUnqualifiedTimestamp(@TempDir Path directory) throws Exception {
        runBulk(true, true, true, directory);
    }

    @Test
    void apiBulkKeepsItsExistingKeySelectionWithoutALocalGoal() throws Exception {
        var batchRef = new AtomicReference<Batch>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override public void startBatch(Batch batch, Autogram autogram) { batchRef.set(batch); }
        };
        var autogram = new Autogram(ui, new UserSettings());
        var key = pki.signer(TestPki.JANA).signingKey();
        try {
            var responder = mock(BatchResponder.class);
            autogram.startBatchSigning(1, responder);
            var batch = batchRef.get();
            assertFalse(batch.canSelectSignatureGoal());
            var gui = mock(GUI.class);
            when(gui.getActiveSigningKey()).thenReturn(key);
            doAnswer(call -> { ((Runnable) call.getArgument(0)).run(); return null; }).when(gui).onWorkThreadDo(any());
            onFxThread(() -> {
                var controller = new BatchDialogController(batch, autogram, gui);
                var loader = new FXMLLoader(BatchDialogController.class.getResource("batch-dialog.fxml"), SupportedLanguage.ENGLISH.loadResources());
                loader.setController(controller);
                loader.load();
                controller.onMainButtonPressed(null);
            });
            verify(gui, never()).pickBatchSignatureAndThen(any(), any(), any());
            verify(responder).onBatchStartSuccess(batch);
        } finally {
            autogram.shutdown();
            key.close();
        }
    }

    private void runBulk(boolean rememberedKey, boolean timestamp, boolean unqualifiedTimestamp, Path directory) throws Exception {
        var requests = new AtomicInteger();
        TSPSource source = (algorithm, digest) -> {
            requests.incrementAndGet();
            return (unqualifiedTimestamp ? pki.nonQualifiedTspSource() : pki.tspSource()).getTimeStampResponse(algorithm, digest);
        };
        var settings = new UserSettings() {
            @Override public TSPSource getTspSource() { return source; }
        };
        settings.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
        settings.setPlainXmlEnabled(true);
        settings.setTokenSessionTimeout(5);
        var batchRef = new AtomicReference<Batch>();
        var result = new AtomicReference<BatchUiResult>();
        var ui = new TestAutogramFactory.FakeUI() {
            @Override public void startBatch(Batch batch, Autogram autogram) { batchRef.set(batch); }
            @Override public void onDocumentBatchSaved(BatchUiResult saved) { result.set(saved); }
        };
        var autogram = spy(new Autogram(ui, settings));
        var token = pki.signer(TestPki.JANA).token();
        var entry = token.getKeys().getFirst();
        var key = new SigningKey(token, entry);
        try {
            var files = List.of(Files.writeString(directory.resolve("one.txt"), "one").toFile(),
                    Files.writeString(directory.resolve("two.txt"), "two").toFile());
            var parameters = settings.getDefaultSigningParameters();
            autogram.startLocalBatchSigning(files.size(), new BatchGuiFileResponder(autogram, files, directory.resolve("signed"),
                    parameters, EFormAttributes.build(parameters, true), false));
            var batch = batchRef.get();
            assertNotNull(batch);
            assertTrue(batch.canSelectSignatureGoal());
            var picks = new AtomicInteger();
            var gui = mock(GUI.class);
            when(gui.getActiveSigningKey()).thenReturn(rememberedKey ? key : null);
            doAnswer(call -> { ((Runnable) call.getArgument(0)).run(); return null; }).when(gui).onWorkThreadDo(any());
            doAnswer(call -> {
                Consumer<SigningKey> callback = call.getArgument(1);
                callback.accept(call.getArgument(0));
                return null;
            }).when(gui).setActiveSigningKeyAndThen(any(), any());
            doAnswer(call -> {
                Consumer<SigningKey> callback = call.getArgument(1);
                chooseGoal(batch, settings, source, entry, timestamp, () -> callback.accept(key));
                picks.incrementAndGet();
                return null;
            }).when(autogram).pickSigningKeyForBatchAndThen(eq(batch), any());
            doAnswer(call -> {
                chooseGoal(batch, settings, source, entry, timestamp, call.getArgument(2));
                picks.incrementAndGet();
                return null;
            }).when(gui).pickBatchSignatureAndThen(eq(key), eq(batch), any());

            onFxThread(() -> {
                var controller = new BatchDialogController(batch, autogram, gui);
                var loader = new FXMLLoader(BatchDialogController.class.getResource("batch-dialog.fxml"), SupportedLanguage.ENGLISH.loadResources());
                loader.setController(controller);
                loader.load();
                controller.onMainButtonPressed(null);
            });

            assertEquals(1, picks.get(), "one goal is chosen for the whole bulk operation, including a remembered key");
            assertEquals(timestamp ? 2 : 0, requests.get());
            assertNotNull(result.get());
            assertEquals(unqualifiedTimestamp ? 0 : 2, result.get().getSignedCount());
            if (unqualifiedTimestamp) {
                assertFalse(Files.exists(directory.resolve("signed")));
            } else {
                for (var file : result.get().getTargetFilesSortedList()) {
                    var report = SignatureValidator.getSignedDocumentSimpleReport(new FileDocument(file));
                    assertEquals(timestamp ? 1 : 0, report.getSignatureTimestamps(report.getFirstSignatureId()).size());
                }
            }
            // A new operation must ask again; the preceding operation's choice is not a global preference.
            autogram.startLocalBatchSigning(1, mock(BatchResponder.class));
            assertNotSame(batch, batchRef.get());
            assertNull(batchRef.get().getSignatureChoice());
        } finally {
            autogram.shutdown();
            key.close();
        }
    }

    private void chooseGoal(Batch batch, UserSettings settings, TSPSource source,
            DSSPrivateKeyEntry key, boolean timestamp, Runnable onPicked) throws Exception {
        var controller = new PickKeyDialogController(List.of(key), ignored -> onPicked.run(), settings,
                (entry, answer) -> answer.accept(new CertificateStatusChecker.Result(
                        CertificateStatus.GOOD, CertificateQualification.QCERT_FOR_ESIG_QSCD)), null,
                new PickKeyDialogController.TimestampOptions(batch, source));
        var loader = new FXMLLoader(PickKeyDialogController.class.getResource("pick-key-dialog.fxml"), SupportedLanguage.ENGLISH.loadResources());
        loader.setController(controller);
        Parent root = loader.load();
        var stage = new Stage();
        stage.setScene(new Scene(root));
        try {
            ((RadioButton) controller.radios.getChildren().get(timestamp ? 0 : 1)).setSelected(true);
            controller.onPickCertificateButtonAction(null);
        } finally {
            stage.close();
        }
    }

    private static void onFxThread(FxAction action) throws Exception {
        var task = new FutureTask<Void>(() -> { action.run(); return null; });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    private interface FxAction {
        void run() throws Exception;
    }
}
