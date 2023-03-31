package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.cli.CliResponder;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
            for (SigningDialogController c : jobs.values()) {
                c.disableKeyPicking();
            }
            if (drivers.size() == 1) {
                callback.call(drivers.get(0)); // short-circuit if only one driver present
            } else {

                PickDriverDialogController controller = new PickDriverDialogController(drivers, callback);
                var root = GUI.loadFXML(controller, "pick-driver-dialog.fxml");

                var scene = new Scene(root);

                var stage = new Stage();
                stage.setTitle("Výber úložiska certifikátu");
                stage.setScene(scene);
                stage.setOnCloseRequest(refreshKeyOnAllJobs);

                stage.sizeToScene();

                stage.show();
            }
        });
    }

    @Override
    public void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        Platform.runLater(() -> {
            if (keys.size() == 1) {
                callback.call(keys.get(0)); // short-circuit if only one key present
            } else {
                var controller = new PickKeyDialogController(keys, callback);
                var root = GUI.loadFXML(controller, "pick-key-dialog.fxml");

                var stage = new Stage();
                stage.setTitle("Výber certifikátu");
                stage.setScene(new Scene(root));
                stage.setOnCloseRequest(refreshKeyOnAllJobs);

                stage.show();
            }
        });
    }

    @Override
    public void showSigningDialog(SigningJob job, Autogram autogram) {
        Platform.runLater(() -> {
            var controller = new SigningDialogController(job, autogram);
            jobs.put(job, controller);

            var root = GUI.loadFXML(controller, "signing-dialog.fxml");

            var stage = new Stage();
            stage.setTitle("Podpisovanie dokumentu"); // TODO use document name?
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                var error = new SigningError();
                job.onDocumentSignFailed(job, error);
            });

            stage.sizeToScene();
            GUI.showOnTop(stage);
            GUI.setUserFriendlyPosition(stage);
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

    @Override
    public void showError(AutogramException e) {
        Platform.runLater(() -> {
            var controller = new ErrorController(e);
            var root = GUI.loadFXML(controller, "error-dialog.fxml");

            var stage = new Stage();
            stage.setTitle("Nastala chyba"); // TODO
            stage.setScene(new Scene(root));

            stage.sizeToScene();
            stage.show();
            GUI.setUserFriendlyPosition(stage);
        });
    }

    public void showPickFileDialog(Autogram autogram) {
        Platform.runLater(() -> {
            var chooser = new FileChooser();
            var list = chooser.showOpenMultipleDialog(new Stage());

            if (list != null) {
                for (File f : list) {
                    var document = new FileDocument(f.getPath());
                    var parameters = SigningParameters.buildForPDF();
                    var responder = new CliResponder(); // TODO

                    var job = new SigningJob(document, parameters, responder);
                    autogram.showSigningDialog(job);
                }
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
}
