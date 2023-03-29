package digital.slovensko.autogram.ui.gui;

import com.octosign.whitelabel.ui.Main;
import digital.slovensko.autogram.core.Autogram;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;

public class GUIApp extends Application {
    static Autogram autogram;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);

        var windowStage = new Stage();

        var controller = new MainMenuController((GUI) GUIApp.autogram.getUI(), GUIApp.autogram);
        var root = GUI.loadFXML(controller, "main-menu.fxml");

        var scene = new Scene(root, 620, 360);
        scene.getStylesheets().add(controller.getClass().getResource("idsk.css").toExternalForm());

        windowStage.setTitle("Autogram");
        windowStage.setScene(scene);
        //windowStage.setIconified(true);
        windowStage.show();
    }
}
