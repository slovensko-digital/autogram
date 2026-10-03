package digital.slovensko.autogram.ui.gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsRestartDialogController extends BaseController implements SuppressedFocusController {
    @FXML
    private Node mainBox;

    @FXML
    private Button closeButton;

    /**
     * Shows a modal dialog informing the user that the application needs to be
     * restarted for the changed settings to take effect.
     */
    public static void show() {
        var controller = new SettingsRestartDialogController();
        var root = GUIUtils.loadFXML(controller, "settings-restart-dialog.fxml");
        var stage = new Stage();
        stage.setTitle(controller.i18n("settings.restart.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void initialize() { }

    public void onCloseButtonAction() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
