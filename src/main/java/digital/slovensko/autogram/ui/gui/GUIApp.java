package digital.slovensko.autogram.ui.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final ExecutorService cachedExecutorService = Executors.newFixedThreadPool(8);

    @Override
    public void start(Stage windowStage) throws Exception {
        var ui = new GUI(getHostServices());
        var autogram = new Autogram(ui);

        Platform.setImplicitExit(false);
        autogram.checkForUpdate();
        autogram.initializeSignatureValidator(scheduledExecutorService, cachedExecutorService);

        setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());

        var controller = new MainMenuController(autogram);
        var root = GUIUtils.loadFXML(controller, "main-menu.fxml");

        var scene = new Scene(root);

        var params = LaunchParameters.fromParameters(getParameters());
        var server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps(), cachedExecutorService);

        server.start();

        windowStage.setOnCloseRequest(event -> {
            new Thread(server::stop).start();
            System.out.println("Closing application");

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

    @Override
    public void stop() throws Exception {
        if (scheduledExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
            // System.out.println("Background threads exited");
        } else {
            // System.out.println("Background threads did not exit, trying to force termination (via interruption)");
            scheduledExecutorService.shutdownNow();
        }

        if (cachedExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
            // System.out.println("Background threads exited");
        } else {
            // System.out.println("Background threads did not exit, trying to force termination (via interruption)");
            cachedExecutorService.shutdownNow();
        }
    }
}
