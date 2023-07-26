package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.errors.*;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class GUI implements UI {
    private final Map<SigningJob, SigningDialogController> jobControllers = new WeakHashMap<>();
    private SigningKey activeKey;
    private final HostServices hostServices;

    public GUI(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        autogram.startVisualization(job);
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
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback) {
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
    }

    private void showError(AutogramException e) {
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
    public void onSignatureValidationCompleted(SigningJob job) {
        var controller = jobControllers.get(job);
        controller.onSignatureValidationCompleted();
    }

    @Override
    public void onSignatureCheckCompleted(SigningJob job) {
        var controller = jobControllers.get(job);
        controller.onSignatureCheckCompleted();
    }

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
    public void onWorkThreadDo(Runnable callback) {
        new Thread(callback).start();
    }

    @Override
    public void onUIThreadDo(Runnable callback) {
        Platform.runLater(callback);
    }

    public SigningKey getActiveSigningKey() {
        return activeKey;
    }

    public void setActiveSigningKey(SigningKey newKey) {
        if (activeKey != null)
            activeKey.close();
        activeKey = newKey;
        refreshKeyOnAllJobs();
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
