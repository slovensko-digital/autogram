package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class GUI implements UI {
    private Map<SigningJob, SigningDialogController> jobs = new WeakHashMap<>();

    private final EventHandler<WindowEvent> refreshKeyOnAllJobs = e -> {
        for (SigningDialogController c : jobs.values()) {
            c.refreshSigningKey();
        }
    };

    @Override
    public void start(Autogram autogram, String[] args) {
        GUIApp.autogram = autogram; // use singleton for passing since javafx instantiation is tricky
        Application.launch(GUIApp.class, args);
    }

    @Override
    public void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback) {
        Platform.runLater(() -> {
            for(SigningDialogController c : jobs.values()) {
                c.disableKeyPicking();
            }
            PickDriverDialogController controller = new PickDriverDialogController(drivers, callback);
            var root = GUI.loadFXML(controller, "pick-driver-dialog.fxml");

            var scene = new Scene(root, 720, 540);
            var stage = new Stage();
            stage.setTitle("Pick driver");
            stage.setScene(scene);
            stage.setOnCloseRequest(refreshKeyOnAllJobs);

            stage.show();
        });
    }

    @Override
    public void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        Platform.runLater(() -> {
            var controller = new PickKeyDialogController(keys, callback);
            var root = GUI.loadFXML(controller, "pick-key-dialog.fxml");
            var scene = new Scene(root, 720, 540);

            var stage = new Stage();
            stage.setTitle("Pick key");
            stage.setScene(scene);
            stage.setOnCloseRequest(refreshKeyOnAllJobs);

            stage.show();
        });
    }

    @Override
    public void showSigningDialog(SigningJob job, Autogram autogram) {
        Platform.runLater(() -> {
            var controller = new SigningDialogController(job, autogram);
            jobs.put(job, controller);

            var root = GUI.loadFXML(controller, "signing-dialog.fxml");

            var scene = new Scene(root, 720, 540);

            var stage = new Stage();
            stage.setTitle("Autogram");
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> {
                var error = new SigningError();
                job.onDocumentSignFailed(job, error);
            });

            stage.show();
        });
    }

    @Override
    public void hideSigningDialog(SigningJob job, Autogram autogram) {
        Platform.runLater(() -> {
            jobs.get(job).hide();
        });
    }

    @Override
    public void refreshSigningKey() {
        Platform.runLater(() -> {
            // TODO maybe use binding?
            for (SigningDialogController controller : jobs.values()) {
                controller.refreshSigningKey();
            }
        });
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
}
