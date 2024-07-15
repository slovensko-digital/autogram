package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.OperatingSystem;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GUIUtils {

    private static Logger logger = LoggerFactory.getLogger(GUIUtils.class);
    static Parent loadFXML(Object controller, String fxml) {
        try {
            var loader = new FXMLLoader();
            loader.setLocation(controller.getClass().getResource(fxml));
            loader.setController(controller);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void showOnTop(Stage stage) {
        stage.requestFocus();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.show();

        new Thread(() -> {
            try {
                Thread.sleep(42);
            } catch (InterruptedException ignored) {
                // noop
            }

            Platform.runLater(() -> stage.setAlwaysOnTop(false));
        }).start();
    }

    public static void suppressDefaultFocus(Stage stage, SuppressedFocusController controller) {
        stage.focusedProperty().addListener(((observable, old, isFocused) -> {
            if (isFocused) controller.getNodeForLoosingFocus().requestFocus(); // everything else looses focus
        }));
    }

    public static void closeWindow(Node node) {
        ((Stage) node.getScene().getWindow()).close();
    }

    public static void startIconified(Stage stage) {
        if (OperatingSystem.current() == OperatingSystem.LINUX) {
            stage.setIconified(true);
        } else {
            // WINDOWS & MAC need to set iconified after showing primary stage, otherwise it starts blank
            stage.setOpacity(0); // prevents startup blink
            stage.setOnShown((e) -> Platform.runLater(() -> {
                stage.setIconified(true);
                stage.setOpacity(1);
            }));
        }
    }

    public static String exceptionToString(Exception exception) {
        var writer = new StringWriter();
        var printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        printWriter.flush();
        printWriter.close();

        return writer.toString();
    }

    public static void hackToForceRelayout(Stage stage) {
        // this MUST be run after stage was shown to work on all platforms
        var w = stage.getScene().getWindow();
        // This forces layout recalculation and fixes issue https://github.com/slovensko-digital/autogram/issues/172
        w.setHeight(w.getHeight() - 1);
    }

    public static void showError(AutogramException e, String buttonText, boolean wait) {
        showError(e, buttonText, wait, false);
    }

    public static void showError(AutogramException e, String buttonText, boolean wait, boolean errorDetailsDisabled) {
        logger.debug("GUI showing error", e);
        var controller = new ErrorController(e, errorDetailsDisabled);
        var root = GUIUtils.loadFXML(controller, "error-dialog.fxml");
        controller.setMainButtonText(buttonText);

        var stage = new Stage();
        stage.setTitle(e.getHeading());
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        GUIUtils.suppressDefaultFocus(stage, controller);

        if (wait) {
            stage.showAndWait();
        } else {
            stage.show();
        }
    }
}
