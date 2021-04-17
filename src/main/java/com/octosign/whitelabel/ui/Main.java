package com.octosign.whitelabel.ui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.octosign.whitelabel.Launcher;
import com.octosign.whitelabel.communication.Document;
import com.octosign.whitelabel.communication.Server;
import com.octosign.whitelabel.communication.ServerInfo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    public enum Status {
        LOADING,
        READY
    };

    private static final String bundlePath = Main.class.getCanonicalName().toLowerCase();
    private static final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);

    private final String version;

    private final CertificateManager certificateManager = new CertificateManager();

    private final Server server;

    public Main() {
        String packageVersion = Launcher.class.getPackage().getImplementationVersion();
        version = packageVersion != null ? packageVersion : "dev";

        // TODO: Use passed CLI arguments including launch URI
        var port = Integer.parseInt(getProperty("server.defaultPort"));
        server = new Server(port);
        server.setInfo(new ServerInfo(version, Status.LOADING));
        // Prevent exiting in server mode on last window close
        Platform.setImplicitExit(false);
    }

    @Override
    public void start(Stage stage) throws IOException {
        server.setOnSign((Document document) -> {
            var future = new CompletableFuture<Document>();

            Platform.runLater(() -> {
                openWindow(document, (String signedContent) -> {
                    Document signedDocument = document.clone();
                    signedDocument.setContent(signedContent);
                    future.complete(signedDocument);
                });
            });

            return future;
        });
        server.setInfo(new ServerInfo(version, Status.READY));

        // TODO: We can hide loader here
    }

    private void openWindow(Document document, Consumer<String> onSigned) {
        var windowStage = new Stage();

        var fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"), bundle);
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            Main.displayAlert(
                AlertType.ERROR,
                "Otvorenie zlyhalo",
                "Nepodarilo sa otvoriť okno",
                "Okno so súborom sa nepodarilo otvoriť. Detail chyby: " + e
            );
            return;
        }

        MainController controller = fxmlLoader.getController();
        controller.setCertificateManager(certificateManager);
        controller.setDocument(document);
        controller.setOnSigned((String signedContent) -> { 
            onSigned.accept(signedContent);
            windowStage.close();
        });

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(bundle.getString("application.name"));
        windowStage.setScene(scene);
        windowStage.show();
    }

    /**
     * Display alert
     *
     * TODO: Add errorAlert with stacktrace
     */
    public static void displayAlert(AlertType type, String title, String header, String description) {
        var alert = new Alert(type);

        var dialogPane = alert.getDialogPane();
        var stylesheets = dialogPane.getStylesheets();
        stylesheets.add(Main.class.getResource("shared.css").toExternalForm());
        stylesheets.add(Main.class.getResource("dialog.css").toExternalForm());
        stylesheets.add(Main.class.getResource("overrides.css").toExternalForm());

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(description);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Get string property from the bundle
     */
    public static String getProperty(String path) {
        return bundle.getString(path);
    }
}
