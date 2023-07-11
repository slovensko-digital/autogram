package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.util.OperatingSystem;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

public class GUIUtils {
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

    static void setUserFriendlyPosition(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double x = bounds.getMinX() + (bounds.getWidth() - stage.getScene().getWidth()) / 2 + (new Random()).nextInt(100) - 50;
        double y = bounds.getMinY() + (bounds.getHeight() - stage.getScene().getHeight()) / 2 + (new Random()).nextInt(100) - 50;

        stage.setX(x);
        stage.setY(y);
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
}
