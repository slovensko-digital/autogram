package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class SettingsResetDialogController implements SuppressedFocusController {

    @FXML
    private Node mainBox;

    @FXML
    private Button confirmResetButton;

    @FXML
    private Button rejectResetButton;

    private UserSettings userSettings;
    private Button resetButton;


    public SettingsResetDialogController() {
    }


    public void onConfirmResetButtonAction() {

        if (userSettings != null) {

            userSettings.reset();

            var stage = (Stage) confirmResetButton.getScene().getWindow();
            stage.close();

            var parentStage = (Stage) resetButton.getScene().getWindow();
            parentStage.close();
        }
    }

    public void onRejectResetButtonAction() {

        var stage = (Stage) rejectResetButton.getScene().getWindow();
        stage.close();
    }


    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public void setResetButton(Button resetButton) {
        this.resetButton = resetButton;
    }
}