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
import javafx.stage.Modality;
import javafx.stage.Stage;

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

            var osName = System.getProperty("os.name", "");
            var dark = false;
            if (osName.startsWith("Mac")) {
                var interfaceStyle = System.getenv("AppleInterfaceStyle");
                dark = interfaceStyle != null && interfaceStyle.equalsIgnoreCase("Dark");
            }

            if (dark) {
                setUserAgentStylesheet(getClass().getResource("idsk-dark.css").toExternalForm());
            } else {
                setUserAgentStylesheet(getClass().getResource("idsk.css").toExternalForm());
            }
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
                        finalAutogram.shutdown();
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
                    finalAutogram.shutdown();
                    Platform.exit();
                });
            }

            GUIUtils.suppressDefaultFocus(windowStage, controller);
            windowStage.setTitle(titleString);
            windowStage.setScene(new Scene(GUIUtils.loadFXML(controller, "main-menu.fxml")));
            windowStage.setResizable(false);
            windowStage.show();

            setupMacHandlers(autogram, userSettings);

        } catch (Exception e) {
            //ak nastane chyba, zobrazíme chybové okno a ukončíme aplikáciu
            var serverFinal = server; //pomocná premenná, do lambda výrazu nižšie musí vstupovať finalna premenná
            var finalAutogram = autogram;
            Platform.runLater(() -> {
                GUIUtils.showError(new UnrecognizedException(e), "Ukončiť",true);
                if (serverFinal != null)
                    new Thread(serverFinal::stop).start();

                if (finalAutogram != null)
                    finalAutogram.shutdown();

                Platform.exit();
            });
        }
    }

    private void setupMacHandlers(Autogram autogram, UserSettings userSettings) {
        try {
            Class<?> appClass = Class.forName("com.apple.eawt.Application");
            Object app = appClass.getMethod("getApplication").invoke(null);
            Class<?> aboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
            Class<?> preferencesHandlerClass = Class.forName("com.apple.eawt.PreferencesHandler");

            Object aboutHandler = java.lang.reflect.Proxy.newProxyInstance(
                    aboutHandlerClass.getClassLoader(),
                    new Class<?>[]{aboutHandlerClass},
                    (proxy, method, args) -> {
                        Platform.runLater(autogram::onAboutInfo);
                        return null;
                    });
            appClass.getMethod("setAboutHandler", aboutHandlerClass).invoke(app, aboutHandler);

            Object prefHandler = java.lang.reflect.Proxy.newProxyInstance(
                    preferencesHandlerClass.getClassLoader(),
                    new Class<?>[]{preferencesHandlerClass},
                    (proxy, method, args) -> {
                        Platform.runLater(() -> showSettings(userSettings));
                        return null;
                    });
            appClass.getMethod("setPreferencesHandler", preferencesHandlerClass).invoke(app, prefHandler);
        } catch (Exception ignored) {
            // com.apple.eawt not available
        }
    }

    private void showSettings(UserSettings userSettings) {
        var controller = new SettingsDialogController(userSettings);
        var root = GUIUtils.loadFXML(controller, "settings-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Nastavenia");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (!scheduledExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS))
            scheduledExecutorService.shutdownNow();

        if (!cachedExecutorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS))
            cachedExecutorService.shutdownNow();
    }
}
