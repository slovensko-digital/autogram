package digital.slovensko.autogram.ui.gui;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.TokenRemovedException;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class GUI implements UI {
    private final Map<SigningJob, SigningDialogController> jobControllers = new WeakHashMap<>();
    private SigningKey activeKey;
    private final HostServices hostServices;
    private BatchDialogController batchController;
    private static final boolean DEBUG = false;
    private static Logger logger = LoggerFactory.getLogger(GUI.class);

    public GUI(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        autogram.startVisualization(job);
    }

    @Override
    public void startBatch(Batch batch, Autogram autogram, Consumer<SigningKey> callback) {
        batchController = new BatchDialogController(batch, callback, autogram, this);
        var root = GUIUtils.loadFXML(batchController, "batch-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Hromadné podpisovanie");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> cancelBatch(batch));

        stage.sizeToScene();
        GUIUtils.suppressDefaultFocus(stage, batchController);
        GUIUtils.showOnTop(stage);
        GUIUtils.setUserFriendlyPosition(stage);
    }

    @Override
    public void cancelBatch(Batch batch) {
        batchController.close();
        batch.end();
        refreshKeyOnAllJobs();
    }

    @Override
    public void signBatch(SigningJob job, SigningKey key) {
        assertOnWorkThread();
        try {
            job.signWithKeyAndRespond(key);
            Logging.log("GUI: Signing batch job: " + job.hashCode() + " file " + job.getDocument().getName());
        } catch (AutogramException e) {
            job.onDocumentSignFailed(e);
        } catch (DSSException e) {
            job.onDocumentSignFailed(AutogramException.createFromDSSException(e));
        } catch (Exception e) {
            AutogramException autogramException = new AutogramException("Document signing has failed", "", "", e);
            job.onDocumentSignFailed(autogramException);
        }
        onUIThreadDo(() -> {
            updateBatch();
        });
    }

    private void updateBatch() {
        if (batchController == null)
            return;
        assertOnUIThread();
        batchController.update();
    }

    @Override
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback) {
        disableKeyPicking();

        if (drivers.isEmpty()) {
            showError(new NoDriversDetectedException());
            refreshKeyOnAllJobs();
        } else if (drivers.size() == 1) {
            // short-circuit if only one driver present
            callback.accept(drivers.get(0));
        } else {
            PickDriverDialogController controller = new PickDriverDialogController(drivers, callback);
            var root = GUIUtils.loadFXML(controller, "pick-driver-dialog.fxml");

            var stage = new Stage();
            stage.setTitle("Výber úložiska certifikátu");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> refreshKeyOnAllJobs());

            stage.sizeToScene();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
    }

    @Override
    public void requestPasswordAndThen(TokenDriver driver, Consumer<char[]> callback) {
        if (!driver.needsPassword()) {
            callback.accept(null);
            return;
        }

        var controller = new PasswordController(callback);
        var root = GUIUtils.loadFXML(controller, "password-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Načítanie klúčov z úložiska");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> refreshKeyOnAllJobs());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys,
            Consumer<DSSPrivateKeyEntry> callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException());
            refreshKeyOnAllJobs();
        } else if (keys.size() == 1) {
            // short-circuit if only one key present
            callback.accept(keys.get(0));
        } else {
            var controller = new PickKeyDialogController(keys, callback);
            var root = GUIUtils.loadFXML(controller, "pick-key-dialog.fxml");

            var stage = new Stage();
            stage.setTitle("Výber certifikátu");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> refreshKeyOnAllJobs());
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
    }

    private void refreshKeyOnAllJobs() {
        jobControllers.values().forEach(SigningDialogController::refreshSigningKey);
        if (batchController != null) {
            batchController.refreshSigningKey();
        }
    }

    @Override
    public void showError(AutogramException e) {
        logger.debug("GUI showing error", e);
        var controller = new ErrorController(e);
        var root = GUIUtils.loadFXML(controller, "error-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(e.getHeading());
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        GUIUtils.suppressDefaultFocus(stage, controller);

        stage.show();
    }

    @Override
    public void onUpdateAvailable() {
        var controller = new UpdateController(hostServices);
        var root = GUIUtils.loadFXML(controller, "update-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Dostupná aktualizácia");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onAboutInfo() {
        var controller = new AboutDialogController(hostServices);
        var root = GUIUtils.loadFXML(controller, "about-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("O projekte Autogram");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onPDFAComplianceCheckFailed(SigningJob job) {
        var controller = new PDFAComplianceDialogController(job, this);
        var root = GUIUtils.loadFXML(controller, "pdfa-compliance-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Dokument nie je vo formáte PDF/A");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getJobWindow(job));
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void showVisualization(Visualization visualization, Autogram autogram) {
        var controller = new SigningDialogController(visualization, autogram, this);
        jobControllers.put(visualization.getJob(), controller);

        var root = GUIUtils.loadFXML(controller, "signing-dialog.fxml");

        var stage = new Stage();

        stage.setTitle("Podpisovanie dokumentu"); // TODO use document name?
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> cancelJob(visualization.getJob()));

        stage.sizeToScene();
        GUIUtils.suppressDefaultFocus(stage, controller);
        GUIUtils.showOnTop(stage);
        GUIUtils.setUserFriendlyPosition(stage);
    }

    @Override
    public void showIgnorableExceptionDialog(IgnorableException e) {
        var controller = new IgnorableExceptionDialogController(e);
        var root = GUIUtils.loadFXML(controller, "ignorable-exception-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Chyba pri zobrazovaní dokumentu");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);

        GUIUtils.showOnTop(stage);
        GUIUtils.setUserFriendlyPosition(stage);
    }

    private void disableKeyPicking() {
        jobControllers.values().forEach(SigningDialogController::disableKeyPicking);
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        showError(e);
        resetSigningKey();
    }

    @Override
    public void onSigningSuccess(SigningJob job) {
        jobControllers.get(job).close();
        refreshKeyOnAllJobs();
        updateBatch();
    }

    @Override
    public void onSigningFailed(AutogramException e) {
        showError(e);
        if (e instanceof TokenRemovedException) {
            resetSigningKey();
        } else {
            refreshKeyOnAllJobs();
        }
    }

    @Override
    public void onDocumentSaved(File targetFile) {
        var controller = new SigningSuccessDialogController(targetFile, hostServices);
        var root = GUIUtils.loadFXML(controller, "signing-success-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Dokument bol úspešne podpísaný");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onDocumentBatchSaved(BatchUiResult result) {
        var stage = new Stage();
        SuppressedFocusController controller;
        Parent root;
        if (result.hasErrors()) {
            controller = new BatchSigningFailureDialogController(result, hostServices);
            root = GUIUtils.loadFXML(controller, "batch-signing-failure-dialog.fxml");
            stage.setTitle("Hromadné podpisovanie ukončené s chybami");
        } else {
            controller = new BatchSigningSuccessDialogController(result, hostServices);
            root = GUIUtils.loadFXML(controller, "batch-signing-success-dialog.fxml");
            stage.setTitle("Hromadné podpisovanie úspešne ukončené");
        }
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.sizeToScene();
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onWorkThreadDo(Runnable callback) {
        if (Platform.isFxApplicationThread()) {
            new Thread(callback).start();
        } else {
            callback.run();
        }
    }

    @Override
    public void onUIThreadDo(Runnable callback) {
        if (Platform.isFxApplicationThread()) {
            callback.run();
        } else {
            Platform.runLater(callback);
        }
    }

    public static void assertOnUIThread() {
        if (DEBUG && !Platform.isFxApplicationThread())
            throw new RuntimeException("Can be run only on UI thread");
    }

    public static void assertOnWorkThread() {
        if (DEBUG && Platform.isFxApplicationThread())
            throw new RuntimeException("Can be run only on work thread");
    }

    public SigningKey getActiveSigningKey() {
        return activeKey;
    }

    public void setActiveSigningKey(SigningKey newKey) {
        if (!isActiveSigningKeyChangeAllowed()) {
            throw new RuntimeException("Signing key change is not allowed");
        }
        if (activeKey != null)
            activeKey.close();
        activeKey = newKey;
        refreshKeyOnAllJobs();
    }

    public boolean isActiveSigningKeyChangeAllowed() {
        return true;
    }

    public void disableSigning() {
        jobControllers.values().forEach(SigningDialogController::disableSigning);
    }

    public void resetSigningKey() {
        setActiveSigningKey(null);
    }

    public void cancelJob(SigningJob job) {
        job.onDocumentSignFailed(new SigningCanceledByUserException());
        jobControllers.get(job).close();
    }

    public void focusJob(SigningJob job) {
        getJobWindow(job).requestFocus();
    }

    private Window getJobWindow(SigningJob job) {
        return jobControllers.get(job).mainBox.getScene().getWindow();
    }
}
