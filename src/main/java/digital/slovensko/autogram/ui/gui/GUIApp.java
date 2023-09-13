package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {
    @Override
    public void start(Stage windowStage) throws Exception {
        var userSettings = UserSettings.load();
        var ui = new GUI(getHostServices(), userSettings);
        var autogram = new Autogram(ui, userSettings.isCorrectDocumentDisplay());

        Platform.setImplicitExit(false);
        autogram.checkForUpdate();

        setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());

        var controller = new MainMenuController(autogram, userSettings);
        var root = GUIUtils.loadFXML(controller, "main-menu.fxml");

        var scene = new Scene(root);

        var params = LaunchParameters.fromParameters(getParameters());
        var server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps());

        if (userSettings.isServerEnabled()) {
            server.start();
        }

        windowStage.setOnCloseRequest(event -> {
            if (userSettings.isServerEnabled()) {
                new Thread(server::stop).start();
            }

            Platform.exit();
        });

        if (!params.isStandaloneMode())
            GUIUtils.startIconified(windowStage);

        GUIUtils.suppressDefaultFocus(windowStage, controller);
        windowStage.setTitle("Autogram");
        windowStage.setScene(scene);
        windowStage.setResizable(false);
        windowStage.show();
    }
}
