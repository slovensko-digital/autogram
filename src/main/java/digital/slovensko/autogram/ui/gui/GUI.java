package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.Autogram;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class GUI implements UI {
    private final Map<SigningJob, SigningDialogController> jobControllers = new WeakHashMap<>();
    private SigningKey activeKey;

    public void start(String[] args) {
        // use singleton for passing since javafx instantiation is tricky
        GUIApp.autogram = new Autogram(this);
        Application.launch(GUIApp.class, args);
    }

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        var controller = new SigningDialogController(job, autogram, this);
        jobControllers.put(job, controller);

        var root = GUIUtils.loadFXML(controller, "signing-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Podpisovanie dokumentu"); // TODO use document name?
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> job.onDocumentSignFailed(new SigningCanceledByUserException()));

        stage.sizeToScene();
        GUIUtils.suppressDefaultFocus(stage, controller);
        GUIUtils.showOnTop(stage);
        GUIUtils.setUserFriendlyPosition(stage);
    }

    @Override
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, TokenDriverLambda callback) {
        disableKeyPicking();

        if (drivers.isEmpty()) {
            showError(new NoDriversDetectedException());
            refreshKeyOnAllJobs();
        } else if (drivers.size() == 1) {
            // short-circuit if only one driver present
            callback.call(drivers.get(0));
        } else {
            PickDriverDialogController controller = new PickDriverDialogController(drivers, callback);
            var root = GUIUtils.loadFXML(controller, "pick-driver-dialog.fxml");

            var stage = new Stage();
            stage.setTitle("Výber úložiska certifikátu");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(event -> refreshKeyOnAllJobs());

            stage.sizeToScene();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
    }

    @Override
    public void requestPasswordAndThen(TokenDriver driver, PasswordLambda callback) {
        if (!driver.needsPassword()) {
            callback.call(null);
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
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException());
            refreshKeyOnAllJobs();
        } else if (keys.size() == 1) {
            // short-circuit if only one key present
            callback.call(keys.get(0));
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
        for (SigningDialogController c : jobControllers.values()) {
            c.refreshSigningKey();
        }
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

    private void disableKeyPicking() {
        for (SigningDialogController c : jobControllers.values()) {
            c.disableKeyPicking();
        }
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        showError(e);
        resetSigningKey();
    }

    @Override
    public void onSigningSuccess(SigningJob job) {
        jobControllers.get(job).close();
    }

    @Override
    public void onSigningFailed(AutogramException e) {
        showError(e);
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
        if (activeKey != null) activeKey.close();
        activeKey = newKey;
        refreshKeyOnAllJobs();
    }

    public void disableSigning() {
        for (SigningDialogController c : jobControllers.values()) {
            c.disableSigning();
        }
    }

    public void resetSigningKey() {
        setActiveSigningKey(null);
    }
}
