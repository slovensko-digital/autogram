package digital.slovensko.autogram.ui.gui;

import com.octosign.whitelabel.ui.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;

public class GUIApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);

        var windowStage = new Stage();

        var fxmlLoader = GUI.loadFXML("main-menu.fxml");
        VBox root = fxmlLoader.getRoot();
        MainMenuController controller = fxmlLoader.getController();
        controller.initialize(GUI.ui, GUI.autogram); // use hardcoded singleton because javafx does not allow passing on launch

        var scene = new Scene(root, 320, 160);
        windowStage.setTitle("Autogram");
        windowStage.setScene(scene);
        //windowStage.setIconified(true);
        windowStage.show();
    }
}
