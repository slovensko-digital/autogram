package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class GUI implements UI {
    private final Map<SigningJob, SigningDialogController> jobs = new WeakHashMap<>();
    private SigningKey activeKey;

    public void start(String[] args) {
        GUIApp.gui = this; // use singleton for passing since javafx instantiation is tricky
        Application.launch(GUIApp.class, args);
    }

    @Override
    public void showSigningDialog(SigningJob job) {
        var controller = new SigningDialogController(job, this);
        jobs.put(job, controller);

        var root = GUI.loadFXML(controller, "signing-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Podpisovanie dokumentu"); // TODO use document name?
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> {
            var error = new SigningError(); // TODO make this nice
            job.onDocumentSignFailed(error);
        });

        stage.sizeToScene();
        GUI.suppressDefaultFocus(stage, controller);
        GUI.showOnTop(stage);
        GUI.setUserFriendlyPosition(stage);
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
            var root = GUI.loadFXML(controller, "pick-driver-dialog.fxml");

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
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException());
            refreshKeyOnAllJobs();
        } else if (keys.size() == 1) {
            // short-circuit if only one key present
            callback.call(keys.get(0));
        } else {
            var controller = new PickKeyDialogController(keys, callback);
            var root = GUI.loadFXML(controller, "pick-key-dialog.fxml");

            var stage = new Stage();
            stage.setTitle("Výber certifikátu");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> refreshKeyOnAllJobs());
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
    }

    @Override
    public void hideSigningDialog(SigningJob job) {
        jobs.get(job).hide();
    }

    @Override
    public void refreshSigningKey() {
        refreshKeyOnAllJobs();
    }

    private void refreshKeyOnAllJobs() {
        for (SigningDialogController c : jobs.values()) {
            c.refreshSigningKey();
        }
    }

    private void disableKeyPicking() {
        for (SigningDialogController c : jobs.values()) {
            c.disableKeyPicking();
        }
    }

    @Override
    public void showError(AutogramException e) {
        var controller = new ErrorController(e);
        var root = GUI.loadFXML(controller, "error-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(e.getHeading());
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        GUI.suppressDefaultFocus(stage, controller);

        stage.show();
    }

    @Override
    public void showPasswordDialogAndThen(TokenDriver driver, PasswordLambda callback) {
        if (!driver.needsPassword()) {
            callback.call(null);
            return;
        }

        var controller = new PasswordController(callback);
        var root = GUI.loadFXML(controller, "password-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Načítanie klúčov z úložiska");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> refreshKeyOnAllJobs());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    public void showPickFileDialog() {
        var chooser = new FileChooser();
        var list = chooser.showOpenMultipleDialog(new Stage());

        if (list != null) {
            for (File file : list) {
                showSigningDialog(SigningJob.buildFromFile(file));
            }
        }
    }

    public void pickSigningKey() {
        pickSigningKeyAndThen(this::setActiveSigningKey);
    }

    private void pickSigningKeyAndThen(SigningKeyLambda callback) {
        var drivers = TokenDriver.getAvailableDrivers(); // TODO handle empty driver list with ui.showError?
        pickTokenDriverAndThen(drivers, (driver) -> {
            showPasswordDialogAndThen(driver, password -> {
                new Thread(() -> {
                    try {
                        var token = driver.createTokenWithPassword(password);
                        var keys = token.getKeys();
                        Platform.runLater(() -> {
                            pickKeyAndThen(keys, (privateKey) -> {
                                callback.call(new SigningKey(token, privateKey));
                            });
                        });
                    } catch (DSSException e) {
                        Platform.runLater(() -> {
                            resetSigningKey();
                            AutogramException ae = AutogramException.createFromDSSException(e);
                            showError(ae);
                        });
                    }
                }).start();
            });
        });
    }

    public SigningKey getActiveSigningKey() {
        return activeKey;
    }

    public void resetSigningKey() {
        setActiveSigningKey(null);
    }

    private void setActiveSigningKey(SigningKey newKey) {
        if (activeKey != null) activeKey.close();
        activeKey = newKey;
        refreshSigningKey();
    }

    public void enableSigning() {
        refreshKeyOnAllJobs();
    }

    public void disableSigning() {
        for (SigningDialogController c : jobs.values()) {
            c.disableSigning();
        }
    }

    static Parent loadFXML(Object controller, String fxml) {
        try {
            var loader = new FXMLLoader();
            loader.setLocation(controller.getClass().getResource(fxml));
            loader.setController(controller);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setUserFriendlyPosition(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double x = bounds.getMinX() + (bounds.getWidth() - stage.getScene().getWidth()) / 2 + (new Random()).nextInt(100) - 50;
        double y = bounds.getMinY() + (bounds.getHeight() - stage.getScene().getHeight()) / 2 + (new Random()).nextInt(100) - 50;

        stage.setX(x);
        stage.setY(y);
    }

    private static void showOnTop(Stage stage) {
        stage.requestFocus();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.show();

        new Thread(() -> {
            try {
                Thread.sleep(42);
            } catch (InterruptedException ignored) {
                // noop
            }

            Platform.runLater(() -> stage.setAlwaysOnTop(false));
        }).start();
    }

    public static void suppressDefaultFocus(Stage windowStage, SuppressedFocusController controller) {
        windowStage.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) controller.getNodeForLoosingFocus().requestFocus(); // everything else looses focus
        }));
    }
}
