package digital.slovensko.autogram.ui.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.core.errors.PortIsUsedException;
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

        var params = LaunchParameters.fromParameters(getParameters());

        try {
            var server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps(), cachedExecutorService);
            server.start();

            windowStage.setOnHidden(event -> {
                new Thread(server::stop).start();
            });

            if (!params.isStandaloneMode())
                GUIUtils.startIconified(windowStage);

            ui.showMainMenu(windowStage, autogram);
        } catch (PortIsUsedException e) {
            ui.showErrorPortInUse(windowStage, autogram, params.getPort(), new PortIsUsedException());
        }

        windowStage.setOnCloseRequest(event -> {
            Platform.exit();
        });
    }

    @Override
    public void stop() throws Exception {
        if (!scheduledExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS))
            scheduledExecutorService.shutdownNow();

        if (!cachedExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS))
            cachedExecutorService.shutdownNow();
    }
}
