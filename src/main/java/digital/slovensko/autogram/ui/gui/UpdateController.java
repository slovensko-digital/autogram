package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Updater;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;

public class UpdateController implements SuppressedFocusController {
    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    Hyperlink link;

    public UpdateController(HostServices hostServices) {
        this.hostServices = hostServices;
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
