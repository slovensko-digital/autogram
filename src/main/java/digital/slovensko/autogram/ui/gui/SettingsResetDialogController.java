package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.Autogram;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class SettingsResetDialogController extends BaseController implements SuppressedFocusController {

    @FXML
    private Node mainBox;

    @FXML
    private Button confirmResetButton;

    @FXML
    private Button rejectResetButton;

    private Autogram autogram;
    private UserSettings userSettings;
    private Button resetButton;


    public SettingsResetDialogController(Autogram autogram, UserSettings userSettings, Button resetButton) {
        this.autogram = autogram;
        this.userSettings = userSettings;
        this.resetButton = resetButton;
    }

    @Override
    public void initialize() { }

    public void onConfirmResetButtonAction() {
        if (userSettings == null)
            return;

        userSettings.reset();
        autogram.updateSignatureValidatorLotl(userSettings.getTrustedList());

        ((Stage)confirmResetButton.getScene().getWindow()).close();
        ((Stage) resetButton.getScene().getWindow()).close();
    }

    public void onRejectResetButtonAction() {
        ((Stage) rejectResetButton.getScene().getWindow()).close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
