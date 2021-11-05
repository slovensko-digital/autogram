package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.cli.command.CommandFactory;
import com.octosign.whitelabel.cli.command.ListenCommand;
import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.octosign.whitelabel.ui.FXUtils.displayError;
import static com.octosign.whitelabel.ui.FXUtils.displayInfo;
import static com.octosign.whitelabel.ui.I18n.translate;
import static java.util.Objects.requireNonNullElse;

public class Main extends Application {

   public enum Status {
        LOADING,
        READY,
    }

    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();

    private static final Locale locale = new Locale("sk");
    private static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

    private final CertificateManager certificateManager = new CertificateManager();

    private StatusIndication statusIndication;

    private Server server;

    private static final Logger LOGGER = Logger.getLogger("logger", String.valueOf(Main.class));

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO split - 2 exceptions
        try {
            var cliCommand = CommandFactory.fromParameters(getParameters());

            if (cliCommand instanceof ListenCommand) {
                startServer((ListenCommand) cliCommand);
                statusIndication = new StatusIndication(this::exit);

                // Prevent exiting in server mode on last window close
                Platform.setImplicitExit(false);
            } else {
                displayInfo("info.appLaunched.header", "info.appLaunched.description");
            }
        } catch (IntegrationException e) {
            displayError("error.launchFailed.header", "error.launchFailed.description", e);
        }
    }

    private void startServer(ListenCommand command) {
        var version = getVersion();
        Server server;
        try {
            server = new Server(command.getInitialNonce());
        } catch (Throwable e) {
            displayError("error.cannotListen.header", "error.cannotListen.description", e);
            return;
        }

        server.setDevMode(version.equals("dev"));
        server.setInfo(new Info(version, Status.LOADING));
        if (command.getOrigin() != null) server.setAllowedOrigin(command.getOrigin());
        if (command.getSecretKey() != null) server.setSecretKey(command.getSecretKey());

        try {
            server.start();
        } catch (Throwable e) {
            displayError("error.cannotOpenWindow.header", "error.cannotOpenWindow.description", e);
            return;
        }

        System.out.println(translate("app.runningOn", server.getAddress()));
        if (server.isDevMode()) {
            var docsAddress = "http:/" + server.getAddress().toString() + "/documentation";
            System.out.println(translate("text.docsAvailableAt", docsAddress));
        }

        server.setOnSign((SignatureUnit signatureUnit) -> {
            var future = new CompletableFuture<Document>();

            Platform.runLater(() -> {
                openWindow(signatureUnit, (String signedContent) -> {
                    var signedDocument = signatureUnit.getDocument().clone();
                    signedDocument.setContent(signedContent);
                    future.complete(signedDocument);
                });
            });
            return future;
        });

        server.setInfo(new Info(version, Status.READY));
    }

    private void exit() {
        if (statusIndication != null) statusIndication.dispose();
        if (server != null) server.stop();
        Platform.exit();
    }

    private void openWindow(SignatureUnit signatureUnit, Consumer<String> onSigned) {
        System.setProperty("javafx.sg.warn", "true");
        var windowStage = new Stage();

        var fxmlLoader = loadWindow("main");
        VBox root = fxmlLoader.getRoot();

        MainController controller = fxmlLoader.getController();
        controller.setCertificateManager(certificateManager);
        controller.setSignatureUnit(signatureUnit);
        controller.loadDocument();
        controller.setOnSigned((String signedContent) -> {
            onSigned.accept(signedContent);
            windowStage.close();
        });

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(translate("app.name"));
        windowStage.setScene(scene);
        windowStage.show();
    }

    public static FXMLLoader loadWindow(String name) {
        var fxmlLoader = new FXMLLoader(Main.class.getResource(name + ".fxml"), bundle);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IntegrationException(Code.FXML_LOADER_ERROR, e);
        }
        return fxmlLoader;
    }

    /**
     * Application version as defined in pom if packaged or dev otherwise
     */
    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }

    public static ResourceBundle getBundle() { return bundle; }
    public static Locale getLocale() { return locale; }

    public static String getProperty(String path) {
        return "stub";
    }
}
