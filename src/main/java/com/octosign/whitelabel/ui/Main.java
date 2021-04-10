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

    private final String bundlePath = getClass().getCanonicalName().toLowerCase();
    private final ResourceBundle bundle = ResourceBundle.getBundle(bundlePath);
    private final String version;

    private final CertificateManager certificateManager = new CertificateManager();

    private final Server server;

    public Main() {
        String packageVersion = Launcher.class.getPackage().getImplementationVersion();
        version = packageVersion != null ? packageVersion : "dev";

        // TODO: Use passed CLI arguments including launch URI
        var port = Integer.parseInt(bundle.getString("server.defaultPort"));
        server = new Server(port);
        server.setInfo(new ServerInfo(version, Status.LOADING));
    }

    @Override
    public void start(Stage stage) throws IOException {
        // TODO: Add loader here using the default stage as loading takes some time
        certificateManager.useDefault();

        server.setOnSign((Document document) -> {
            var future = new CompletableFuture<Document>();

            Platform.runLater(() -> {
                openWindow(document, (Document signed) -> future.complete(signed));
            });

            return future;
        });
        server.setInfo(new ServerInfo(version, Status.READY));

        // TODO: We can hide loader here
    }

    private void openWindow(Document document, Consumer<Document> onSigned) {
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
        controller.setOnSigned(onSigned);

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(bundle.getString("application.name"));
        windowStage.setScene(scene);
        windowStage.show();
    }

    /**
     * Display alert
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
}
