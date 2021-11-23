package com.octosign.whitelabel.ui;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.error_handling.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.octosign.whitelabel.cli.command.CommandFactory;
import com.octosign.whitelabel.cli.command.ListenCommand;
import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.server.Server;
import javafx.stage.Window;
import org.slf4j.LoggerFactory;

import static com.octosign.whitelabel.ui.FXUtils.*;
import static com.octosign.whitelabel.ui.I18n.translate;

import static java.util.Objects.requireNonNullElse;

public class Main extends Application {

    public enum Status {
        LOADING,
        READY,
    }

    private final CertificateManager certificateManager = new CertificateManager();

    private StatusIndication statusIndication;

    private Server server;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        var cliCommand = CommandFactory.fromParameters(getParameters());

        if (cliCommand instanceof ListenCommand listenCommand) {
            startServer(listenCommand);
            statusIndication = new StatusIndication(this::exit);

            // Prevent exiting in server mode on last window close
            Platform.setImplicitExit(false);
        } else {
            // No CLI command means standalone GUI mode
            // TODO: Show something more useful, this should be either:
            // 1. Standalone mode once it gets implemented
            // 2. Info about the app and how it is launched from the web
            displayInfo("info.appLaunched.header", "info.appLaunched.description");
        }
    }

    private void startServer(ListenCommand command) {
        var version = getVersion();

        server = new Server(command.getInitialNonce());
        server.setDevMode(version.equals("dev"));
        server.setInfo(new Info(version, Status.LOADING));

        if (command.getOrigin() != null) server.setAllowedOrigin(command.getOrigin());
        if (command.getSecretKey() != null) server.setSecretKey(command.getSecretKey());

        server.start();
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
        controller.loadDocument();

        var scene = new Scene(root, 640, 480);
        windowStage.setTitle(translate("app.name"));
        windowStage.setScene(scene);
        windowStage.show();
    }

    public static FXMLLoader loadWindow(String name) {
        var fxmlLoader = new FXMLLoader(Main.class.getResource(name + ".fxml"), I18n.getBundle());
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fxmlLoader;
    }

    /**
     * Application version as defined in pom if packaged or dev otherwise
     */
    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }

    private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            if (throwable instanceof IntegrationException ie) {
                LOGGER.error("IntegrationException handled, code: " + ie.getCode().toString(), ie);
                if (ie.shouldDisplay())
                    Platform.runLater(() -> displayError(ie));
                closeAllWindows();

            } else if (throwable instanceof UserException ue) {
                LOGGER.error("UserException handled: ", ue);
                Platform.runLater(() -> displayError(ue));

            } else {
                LOGGER.error("ATTENTION - unexpected exception handled: " + throwable.getClass().getName(), throwable);
                Platform.runLater(FXUtils::displaySimpleError);
                System.exit(1);
            }
        }

        private void closeAllWindows() {
            Stage.getWindows().stream().filter(Window::isShowing).forEach(window -> ((Stage)window).close());
        }
    }
}
