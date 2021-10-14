package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.cli.command.Command;
import com.octosign.whitelabel.cli.command.CommandFactory;
import com.octosign.whitelabel.cli.command.ListenCommand;
import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.octosign.whitelabel.ui.FX.displayError;
import static java.util.Objects.requireNonNullElse;

public class Main extends Application {
    
    public enum Status {
        LOADING,
        READY
    }

    private final CertificateManager certificateManager = new CertificateManager();

    private StatusIndication statusIndication;

    private Server server;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Command cliCommand;
        try {
            cliCommand = CommandFactory.fromParameters(getParameters());
        } catch (Exception e) {
            displayError("openingFailed", e);
            return;
        }

        if (cliCommand instanceof ListenCommand) {
            startServer((ListenCommand) cliCommand);

            statusIndication = new StatusIndication(() -> this.exit());

            // Prevent exiting in server mode on last window close
            Platform.setImplicitExit(false);
            return;
        }

        // No CLI command means standalone GUI mode
        // TODO: Show something more useful, this should be either:
        // 1. Standalone mode once it gets implemented
        // 2. Info about the app and how it is launched from the web
        displayAlert(
                AlertType.INFORMATION,
                getProperty("application.name"),
                getProperty("alert.appLaunched.header"),
                getProperty("alert.appLaunched.description")
        );
    }

    private void startServer(ListenCommand command) {
        var version = getVersion();
        var hostname = getProperty("server.hostname");
        var port = command.getPort() > 0
                ? command.getPort()
                : Integer.parseInt(getProperty("server.defaultPort"));

        Server server;
        try {
            server = new Server(hostname, port, command.getInitialNonce());
        } catch (Throwable e) {
            displayError("cannotListen", e);
            return;
        }

        server.setDevMode(version.equals("dev"));
        server.setInfo(new Info(version, Status.LOADING));
        if (command.getOrigin() != null) server.setAllowedOrigin(command.getOrigin());
        if (command.getSecretKey() != null) server.setSecretKey(command.getSecretKey());

        try {
            server.start();
        } catch (Throwable e) {
            displayError("cannotOpenWindow", e);
            return;
        }

        System.out.println("Running in server mode on " + server.getAddress().toString());
        if (server.isDevMode()) {
            var docsAddress = "http:/" + server.getAddress().toString() + "/documentation";
            System.out.println(getProperty("text.documentationAvailableAt", docsAddress));
        }

        server.setOnSign((SignatureUnit signatureUnit) -> {
            var future = new CompletableFuture<Document>();

            Platform.runLater(() -> {
                openWindow(signatureUnit, (String signedContent) -> {
                    Document signedDocument = signatureUnit.getDocument().clone();
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
        var windowStage = new Stage();

        var fxmlLoader = loadWindow("main");
        VBox root = fxmlLoader.getRoot();

        MainController controller = fxmlLoader.getController();
        controller.setCertificateManager(certificateManager);
        controller.setSignatureUnit(signatureUnit);
        controller.setOnSigned((String signedContent) -> {
            onSigned.accept(signedContent);
            windowStage.close();
        });

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(getProperty("application.name"));
        windowStage.setScene(scene);
        windowStage.show();
    }

    public static FXMLLoader loadWindow(String name) {
        var fxmlLoader = new FXMLLoader(Main.class.getResource(name + ".fxml"), bundle);
        try {
            fxmlLoader.load();
        } catch (Exception e) {
            displayError(
                getProperty("text.error.openingFailed"),
                getProperty("text.error.cannotOpenWindow"),
                e
            );
            return null;
        }

        return fxmlLoader;
    }

    /**
     * Display alert
     */
    public static void displayAlert(AlertType type, String title, String header, String description) {
        getAlert(type, title, header, description).showAndWait();
    }

    /**
     * Application version as defined in pom if packaged or dev otherwise
     */
    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }


    // TODO decide about moving this elsewhere
    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();
    private static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
        return String.format(bundle.getString(path), args);
    }
}
