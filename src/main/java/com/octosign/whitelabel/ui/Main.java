package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.cli.command.CommandFactory;
import com.octosign.whitelabel.cli.command.ListenCommand;
import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.IntegrationException;
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

import static com.octosign.whitelabel.ui.FX.displayInfo;
import static java.util.Objects.requireNonNullElse;

public class Main extends Application {
    
    public enum Status {
        LOADING,
        READY,
    }

    private static final Locale skLocale = new Locale( "sk");
    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();

    private static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath, skLocale);

    private final CertificateManager certificateManager = new CertificateManager();

    private StatusIndication statusIndication;

    private Server server;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        try {
            var cliCommand = CommandFactory.fromParameters(getParameters());

            if (cliCommand instanceof ListenCommand) {
                startServer((ListenCommand) cliCommand);

            statusIndication = new StatusIndication(() -> this.exit());

            // Prevent exiting in server mode on last window close
            Platform.setImplicitExit(false);
            return;
        }

        displayInfo("info.appLaunched.header", "info.appLaunched.description");
    }

    private void startServer(ListenCommand command) throws Exception {
        var version = getVersion();
        Server server;
//        try {
            server = new Server(command.getInitialNonce());
//        } catch (Throwable e) {
//            displayError("error.cannotListen.header", "error.cannotListen.description", e);
//            return;
//        }

        server.setDevMode(version.equals("dev"));
        server.setInfo(new Info(version, Status.LOADING));
        if (command.getOrigin() != null) server.setAllowedOrigin(command.getOrigin());
        if (command.getSecretKey() != null) server.setSecretKey(command.getSecretKey());

//        try {
            server.start();
//        } catch (Throwable e) {
//            displayError("error.cannotOpenWindow.header", "error.cannotOpenWindow.description", e);
//            return;
//        }

        System.out.println(translate("app.runningOn", server.getAddress()));
        if (server.isDevMode()) {
            var docsAddress = "http:/" + server.getAddress().toString() + "/documentation";
            System.out.println(translate("text.docsAvailableAt", docsAddress));
        }

        server.setOnSign((SignatureUnit signatureUnit) -> {
            var future = new CompletableFuture<Document>();

                Platform.runLater(() -> {
//                    try {
                    try {
                        openWindow(signatureUnit, (String signedContent) -> {
                            var signedDocument = signatureUnit.getDocument().clone();
                            signedDocument.setContent(signedContent);
                            future.complete(signedDocument);
                        });
                    } catch (IntegrationException e) {
                        e.printStackTrace();
                    }
//                    } catch (IntegrationException e) {
//                        displayError("error.openingFailed.header", "error.openingFailed.description", e);
//                    }
                });

            return future;
        });

        server.setInfo(new Info(version, Status.READY));
    }

//    private void exit() {
//        if (statusIndication != null) statusIndication.dispose();
//        if (server != null) server.stop();
//        Platform.exit();
//    }

    private void openWindow(SignatureUnit signatureUnit, Consumer<String> onSigned) {
        var windowStage = new Stage();
        var fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"), bundle);

        var fxmlLoader = loadWindow("main");
        VBox root = fxmlLoader.getRoot();

        System.setProperty("javafx.sg.warn", "true");
        MainController controller = fxmlLoader.getController();
        controller.setCertificateManager(certificateManager);
        controller.setSignatureUnit(signatureUnit);
        controller.loadDocument();
        controller.setOnSigned((String signedContent) -> {
            onSigned.accept(signedContent);
            windowStage.close();
        });

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(getProperty("app.name"));
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

    public static String getResourceString(String filename) {
        return Objects.requireNonNull(Main.class.getResource(filename)).toExternalForm();
    }

    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
        return String.format(bundle.getString(path), args);
    }

    public static String translate(String path, Object... args) {
        if (args.length == 0) return getProperty(path);
        else return getProperty(path, args);
    }
}
