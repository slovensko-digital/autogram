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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.octosign.whitelabel.ui.FX.displayError;
import static com.octosign.whitelabel.ui.FX.displayInfo;
import static java.util.Objects.requireNonNullElse;

public class Main extends Application {

    public enum Status {
        LOADING,
        READY
    }

    private final CertificateManager certificateManager = new CertificateManager();

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            var cliCommand = CommandFactory.fromParameters(getParameters());

            if (cliCommand instanceof ListenCommand) {
                startServer((ListenCommand) cliCommand);

                // Prevent exiting in server mode on last window close
                Platform.setImplicitExit(false);
                return;
            }
        } catch (IntegrationException e) {
            displayError("launchFailed", e);
            return;
        }

        displayInfo("appLaunched");
    }

    private void startServer(ListenCommand command) {
        var version = getVersion();
        Server server;
        try {
            server = new Server(command.getInitialNonce());
        } catch (Throwable e) {
            displayError("cannotListen");
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
                    try {
                    openWindow(signatureUnit, (String signedContent) -> {
                        var signedDocument = signatureUnit.getDocument().clone();
                        signedDocument.setContent(signedContent);
                        future.complete(signedDocument);
                    });
                    } catch (IntegrationException e) {
                        displayError("openingFailed", e);
                    }
                });

            return future;
        });

        server.setInfo(new Info(version, Status.READY));
    }

    private void openWindow(SignatureUnit signatureUnit, Consumer<String> onSigned) throws IntegrationException {
        var windowStage = new Stage();

        var fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"), I18n.bundle);
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new IntegrationException("Unable to load FXMLLoader", e);
        }

        MainController controller = fxmlLoader.getController();
        controller.setCertificateManager(certificateManager);
        controller.setSignatureUnit(signatureUnit);
        controller.setOnSigned((String signedContent) -> {
            onSigned.accept(signedContent);
            windowStage.close();
        });

        controller.loadDocument();

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(getProperty("application.name"));
        windowStage.setScene(scene);
        windowStage.show();
    }


    /**
     * Application version as defined in pom if packaged or dev otherwise
     */
    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }

    public static String getResourceString(String filename) {
        return Objects.requireNonNull(Main.class.getResource(filename)).toExternalForm();
    }

    private static final Locale skLocale = new Locale( "sk");
    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();

    private static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath, skLocale);

    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
        return String.format(bundle.getString(path), args);
    }

    public static void logError(String s, Throwable t) {
        // TODO implement this
    }
}
