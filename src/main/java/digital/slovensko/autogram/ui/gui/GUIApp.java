package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {
    public static Autogram autogram;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);

        setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());

        var windowStage = new Stage();

        var controller = new MainMenuController(autogram, getHostServices());
        var root = GUIUtils.loadFXML(controller, "main-menu.fxml");

        var scene = new Scene(root);

        var params = LaunchParameters.fromParameters(getParameters());
        var server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps());

        server.start();

        if (!params.isStandaloneMode()) windowStage.setIconified(true);

        windowStage.setOnCloseRequest(event -> {
            new Thread(server::stop).start();

            Platform.exit();
        });

        windowStage.setTitle("Autogram");
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);
        windowStage.show();

        GUIUtils.suppressDefaultFocus(windowStage, controller);
    }
}
