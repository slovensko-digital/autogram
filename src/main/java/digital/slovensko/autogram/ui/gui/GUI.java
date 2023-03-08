package digital.slovensko.autogram.ui.gui;

import com.octosign.whitelabel.ui.I18n;
import com.octosign.whitelabel.ui.Main;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class GUI implements UI {
    public static GUI ui;
    static GUI gui;
    public static Autogram autogram;

    @Override
    public void start(String[] args) {
        Application.launch(GUIApp.class, args);
    }

    @Override
    public void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback) {
        Platform.runLater(() -> {
            var stage = new Stage();

            var fxmlLoader = loadFXML("pick-driver-dialog.fxml");
            VBox root = fxmlLoader.getRoot();

            PickDriverDialogController controller = fxmlLoader.getController();
            controller.callback = callback;
            controller.drivers = drivers;
            controller.driverLabel.setText(drivers.get(0).getClass().toString());

            var scene = new Scene(root, 720, 540);
            stage.setTitle("Pick driver");
            stage.setScene(scene);

            stage.show();
        });
    }

    @Override
    public void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        Platform.runLater(() -> {
            var stage = new Stage();

            var fxmlLoader = loadFXML("pick-key-dialog.fxml");
            VBox root = fxmlLoader.getRoot();

            PickKeyDialogController controller = fxmlLoader.getController();
            controller.callback = callback;
            controller.keys = keys;
            controller.keyLabel.setText(keys.get(0).getCertificate().getSubject().getPrettyPrintRFC2253());

            var scene = new Scene(root, 720, 540);
            stage.setTitle("Pick key");
            stage.setScene(scene);

            stage.show();
        });
    }

    @Override
    public void showSigningDialog(SigningJob job, Autogram autogram) {
        Platform.runLater(() -> {
            var stage = new Stage();

            stage.setOnCloseRequest(e -> {
                var error = new SigningError();
                job.onDocumentSignFailed(job, error);
            });

            var fxmlLoader = loadFXML("signing-dialog.fxml");
            AnchorPane root = fxmlLoader.getRoot();

            SigningDialogController controller = fxmlLoader.getController();
            controller.setAutogram(autogram);
            controller.setSigningJob(job);
            var key = autogram.getActiveSigningKey();
            if(key != null) {
                controller.mainButton.setText(key.getCertificate().getSubject().getPrettyPrintRFC2253());
            }

            var scene = new Scene(root, 720, 540);
            stage.setTitle("Autogram");
            stage.setScene(scene);

            stage.show();
        });
    }

    @Override
    public void refreshSigningKey(SigningKey key) {
        // TODO loop through active jobs and set signing keys
    }

    static FXMLLoader loadFXML(String fxml) {
        var fxmlLoader = new FXMLLoader(GUIApp.class.getResource(fxml), I18n.getBundle());
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fxmlLoader;
    }
}
