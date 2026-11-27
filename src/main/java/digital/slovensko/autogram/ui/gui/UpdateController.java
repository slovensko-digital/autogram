package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Updater;
import digital.slovensko.autogram.util.OperatingSystem;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import static digital.slovensko.autogram.ui.gui.TextHighlighter.highlight;

public class UpdateController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    TextFlow title;
    @FXML
    Text text;
    @FXML
    TextFlow moreInfo;
    @FXML
    Button storeButton;
    @FXML
    Button mainButton;
    @FXML
    Button cancelButton;

    public UpdateController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        highlight("Autogram").in(title);

        if (Updater.isInstalledFromAur()) {
            // Nothing to download, the package manager brings the new version, so the window only says how
            text.setText(i18n("update.newVersion.aur.text"));
            mainButton.setText(i18n("general.close.btn"));
            mainButton.setOnAction(this::onCancelButtonPressed);
            cancelButton.setManaged(false);
            cancelButton.setVisible(false);
        } else if (OperatingSystem.current() == OperatingSystem.WINDOWS) {
            text.setText(i18n("update.newVersion.store.text"));
            moreInfo.setManaged(true);
            moreInfo.setVisible(true);
            storeButton.setManaged(true);
            storeButton.setVisible(true);
            mainButton.setManaged(false);
            mainButton.setVisible(false);
        }
    }

    public void storeAction(ActionEvent ignored) {
        hostServices.showDocument(Updater.MICROSOFT_STORE_URL);
    }

    public void downloadAction(ActionEvent ignored) {
        hostServices.showDocument(Updater.LATEST_RELEASE_URL);
    }

    public void onCancelButtonPressed(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
