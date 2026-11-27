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

import java.util.List;

import static digital.slovensko.autogram.ui.gui.TextHighlighter.code;
import static digital.slovensko.autogram.ui.gui.TextHighlighter.highlight;

public class UpdateController extends BaseController implements SuppressedFocusController {
    // AUR helpers that update the AUR packages together with the others
    private static final List<String> AUR_UPDATE_COMMANDS = List.of("yay -Syu", "paru -Syu");

    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    TextFlow title;
    @FXML
    TextFlow textFlow;
    @FXML
    Text text;
    @FXML
    TextFlow moreInfo;
    @FXML
    Button storeButton;
    @FXML
    Button mainButton;
    @FXML
    Button closeButton;
    @FXML
    Button guideButton;
    @FXML
    Button cancelButton;

    public UpdateController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        highlight("Autogram").in(title);

        if (Updater.isInstalledFromAur()) {
            // The package manager brings the new version and the user updates in the terminal, so there's no primary
            // action, only closing the window and the way to the guide
            text.setText(i18n("update.newVersion.aur.text", AUR_UPDATE_COMMANDS.toArray()));
            AUR_UPDATE_COMMANDS.forEach(command -> code(command).in(textFlow));
            for (var hidden : List.of(mainButton, cancelButton)) {
                hidden.setManaged(false);
                hidden.setVisible(false);
            }
            for (var shown : List.of(closeButton, guideButton)) {
                shown.setManaged(true);
                shown.setVisible(true);
            }
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

    public void guideAction(ActionEvent ignored) {
        GUIUtils.openDocs(hostServices, resources, i18n("update.aur.help"));
    }

    public void onCancelButtonPressed(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
