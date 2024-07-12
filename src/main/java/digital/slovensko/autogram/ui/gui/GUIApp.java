package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GUIApp extends Application {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final ExecutorService cachedExecutorService = Executors.newFixedThreadPool(4);
    private final ExecutorService serverExecutorService = Executors.newFixedThreadPool(1);

    @Override
    public void start(Stage windowStage) throws Exception {
        AutogramServer server = null;
        try {
            var userSettings = UserSettings.load();

            Platform.setImplicitExit(false);
            setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());

            final Autogram autogram;
            autogram = new Autogram(new GUI(getHostServices(), userSettings), userSettings);
            autogram.checkForUpdate();
            autogram.initializeSignatureValidator(scheduledExecutorService, cachedExecutorService, userSettings.getTrustedList());

            final var params = LaunchParameters.fromParameters(getParameters());
            final var controller = new MainMenuController(autogram, userSettings);

            server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps(), serverExecutorService);
            if (userSettings.isServerEnabled()) {
                server.start();
            }


            Thread thread = new Thread(server::stop);
            windowStage.setOnCloseRequest(event -> {
                if (userSettings.isServerEnabled()) {
                    thread.start();
                }
                Platform.exit();
            });


            if (!params.isStandaloneMode())
                GUIUtils.startIconified(windowStage);

            GUIUtils.suppressDefaultFocus(windowStage, controller);
            windowStage.setTitle("Autogram");
            windowStage.setScene(new Scene(GUIUtils.loadFXML(controller, "main-menu.fxml")));
            windowStage.setResizable(false);
            windowStage.show();
        } catch (Exception e) {
            //ak nastane chyba, zobrazíme chybové okno a ukončíme aplikáciu
            final var serverFinal = server; //pomocná premenná, do lambda výrazu nižšie musí vstupovať finalna premenná
            Platform.runLater(() -> {
                GUIUtils.showError(new UnrecognizedException(e), "Ukončiť",true);
                if (serverFinal != null) {
                    new Thread(serverFinal::stop).start();
                }
                Platform.exit();
            });
        }
    }

    @Override
    public void stop() throws Exception {
        if (!scheduledExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS))
            scheduledExecutorService.shutdownNow();

        if (!cachedExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS))
            cachedExecutorService.shutdownNow();
    }
}
