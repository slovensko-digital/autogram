package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.Autogram;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
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

        var proxyCleared = !userSettings.getProxyUrl().isEmpty();
        userSettings.reset();
        if (proxyCleared) {
            var notice = new Alert(Alert.AlertType.INFORMATION);
            notice.initOwner(confirmResetButton.getScene().getWindow());
            notice.setHeaderText(i18n("settings.restartNeeded.text"));
            notice.setContentText(i18n("settings.other.proxy.reset"));
            notice.showAndWait();
        }
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
