package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUIApp extends Application {
    public static Autogram autogram;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);

        // TODO how do we load fonts from CSS?
        Font.loadFont(getClass().getResource("fonts/SourceSansPro-Regular.ttf").toExternalForm(), 16);
        Font.loadFont(getClass().getResource("fonts/SourceSansPro-Bold.ttf").toExternalForm(), 16);
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

        GUIUtils.suppressDefaultFocus(windowStage, controller);

        windowStage.setTitle("Autogram");
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);
        windowStage.show();
    }
}
