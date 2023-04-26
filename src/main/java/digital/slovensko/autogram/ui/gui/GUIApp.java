package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {
    public static Autogram autogram;

    @Override
    public void start(Stage windowStage) throws Exception {
        Platform.setImplicitExit(false);

        setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());

        var controller = new MainMenuController(autogram, getHostServices());
        var root = GUIUtils.loadFXML(controller, "main-menu.fxml");

        var scene = new Scene(root);

        var params = LaunchParameters.fromParameters(getParameters());
        var server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps());

        server.start();

        windowStage.setOnCloseRequest(event -> {
            new Thread(server::stop).start();

            Platform.exit();
        });

        windowStage.addEventHandler(Event.ANY, (e) -> {
            System.out.println(e);
        });

        GUIUtils.suppressDefaultFocus(windowStage, controller);

        if (params.isStandaloneMode()) {

        } else {
            windowStage.setOnShown((e) -> {
                System.out.println("Shown EVENT");
                windowStage.setIconified(true);
            });
        }

        windowStage.setTitle("Autogram");
        windowStage.setScene(scene);
        windowStage.sizeToScene();
        windowStage.setResizable(false);
        windowStage.show();
    }
}
