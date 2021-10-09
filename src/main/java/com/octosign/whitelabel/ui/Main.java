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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNullElse;

public class Main extends Application {

    public enum Status {
        LOADING,
        READY
    }

    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();
    private static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

    private final CertificateManager certificateManager = new CertificateManager();

    private Server server;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Command cliCommand;
        try {
            cliCommand = CommandFactory.fromParameters(getParameters());
        } catch (Throwable e) {
            displayError(
                getProperty("text.error.openingFailed"),
                getProperty("text.error.cannotUseParameters"),
                e
            );
            return;
        }

        if (cliCommand instanceof ListenCommand) {
            startServer((ListenCommand) cliCommand);

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

        try {
            server = new Server(hostname, port, command.getInitialNonce());
        } catch (Throwable e) {
            displayError(
                getProperty("text.error.openingFailed"),
                getProperty("text.error.cannotListen"),
                e
            );
            return;
        }

        server.setDevMode(version.equals("dev"));
        server.setInfo(new Info(version, Status.LOADING));
        if (command.getOrigin() != null) server.setAllowedOrigin(command.getOrigin());
        if (command.getSecretKey() != null) server.setSecretKey(command.getSecretKey());

        try {
            server.start();
        } catch (Throwable e) {
            displayError(
                getProperty("text.error.openingFailed"),
                getProperty("text.error.cannotListen"),
                e
            );
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

    private void openWindow(SignatureUnit signatureUnit, Consumer<String> onSigned) {
        var windowStage = new Stage();

        var fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"), bundle);
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            displayError(
                getProperty("text.error.openingFailed"),
                getProperty("text.error.cannotOpenWindow"),
                e
            );
            return;
        }

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

    /**
     * Display alert
     */
    public static void displayAlert(AlertType type, String title, String header, String description) {
        getAlert(type, title, header, description).showAndWait();
    }

    /**
     * Display error alert
     */
    public static void displayError(String header, String description) {
        var alert = getAlert(AlertType.ERROR, getProperty("text.error"), header, description);

        alert.showAndWait();
    }

    /**
     * Display error alert with exception details
     */
    public static void displayError(String header, String description, Throwable e) {
        var alert = getAlert(AlertType.ERROR, getProperty("text.error"), header, description);

        if (e != null) {
            e.printStackTrace();
            var stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            var stackTrace = stringWriter.toString();

            var details = new GridPane();
            details.setMaxWidth(Double.MAX_VALUE);

            var label = new Label(getProperty("text.error.details"));
            details.add(label, 0, 0);

            var textArea = new TextArea(stackTrace);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            details.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(details);
        }

        alert.showAndWait();
    }

    /**
     * Create unified Alert
     */
    public static Alert getAlert(AlertType type, String title, String header, String description) {
        var alert = new Alert(type);

        var dialogPane = alert.getDialogPane();
        var stylesheets = dialogPane.getStylesheets();
        stylesheets.add(Main.class.getResource("shared.css").toExternalForm());
        stylesheets.add(Main.class.getResource("dialog.css").toExternalForm());
        stylesheets.add(Main.class.getResource("overrides.css").toExternalForm());

        if (title != null) alert.setTitle(title);
        if (header != null) alert.setHeaderText(header);
        if (description != null) alert.setContentText(description);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        return alert;
    }

    /**
     * Get string property from the bundle
     */
    public static String getProperty(String path) {
        return bundle.getString(path);
    }

    public static String getProperty(String path, Object... args) {
        return String.format(bundle.getString(path), args);

    }

    public static String[] getExceptionProperties(String exceptionType) {
        String base = "exc." + exceptionType;
        String titleProp = base + ".title";
        String headerProp = base + ".header";
        String descriptionProp = base + ".description";

        return new String[] { getProperty(titleProp), getProperty(headerProp), getProperty(descriptionProp) };
    }

    /**
     * Application version as defined in pom if packaged or dev otherwise
     */
    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }

}
