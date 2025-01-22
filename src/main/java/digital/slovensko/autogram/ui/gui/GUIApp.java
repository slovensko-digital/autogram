package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.LaunchParameters;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.PortIsUsedException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.server.AutogramServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GUIApp extends Application {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final ExecutorService cachedExecutorService = Executors.newFixedThreadPool(8);

    @Override
    public void start(Stage windowStage) throws Exception {
        AutogramServer server = null;
        Autogram autogram = null;
        try {
            var userSettings = UserSettings.load();

            Platform.setImplicitExit(false);
            setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());
            var titleString = "Autogram";

            autogram = new Autogram(new GUI(getHostServices(), userSettings), userSettings);
            var finalAutogram = autogram;
            autogram.checkForUpdate();
            autogram.initializeSignatureValidator(scheduledExecutorService, cachedExecutorService, userSettings.getTrustedList());

            final var params = LaunchParameters.fromParameters(getParameters());
            final var controller = new MainMenuController(autogram, userSettings);

            if (!params.isStandaloneMode())
                GUIUtils.startIconified(windowStage);

            if (userSettings.isServerEnabled()) {
                try {
                    server = new AutogramServer(autogram, params.getHost(), params.getPort(), params.isProtocolHttps(), cachedExecutorService);
                    server.start();

                    var thread = new Thread(server::stop);
                    windowStage.setOnCloseRequest(event -> {
                        thread.start();
                        finalAutogram.stopTokenSessionTimer();
                        Platform.exit();
                    });

                } catch (PortIsUsedException e) {
                    Platform.runLater(() -> {
                        GUIUtils.showError(e, "Pokračovať v obmedzenom režime", true, true);
                    });

                    server = null;
                    titleString = "Autogram (obmedzený režim)";
                }
            }

            if (server == null) {
                windowStage.setOnCloseRequest(event -> {
                    finalAutogram.stopTokenSessionTimer();
                    Platform.exit();
                });
            }

            GUIUtils.suppressDefaultFocus(windowStage, controller);
            windowStage.setTitle(titleString);
            windowStage.setScene(new Scene(GUIUtils.loadFXML(controller, "main-menu.fxml")));
            windowStage.setResizable(false);
            windowStage.show();

        } catch (Exception e) {
            //ak nastane chyba, zobrazíme chybové okno a ukončíme aplikáciu
            var serverFinal = server; //pomocná premenná, do lambda výrazu nižšie musí vstupovať finalna premenná
            var finalAutogram = autogram;
            Platform.runLater(() -> {
                GUIUtils.showError(new UnrecognizedException(e), "Ukončiť",true);
                if (serverFinal != null)
                    new Thread(serverFinal::stop).start();

                finalAutogram.stopTokenSessionTimer();
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
