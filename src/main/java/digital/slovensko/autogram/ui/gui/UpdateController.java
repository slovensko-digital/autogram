package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Updater;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.TextFlow;

import static digital.slovensko.autogram.ui.gui.TextHighlighter.highlight;

public class UpdateController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    Hyperlink link;
    @FXML
    TextFlow title;

    public UpdateController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        highlight("Autogram").in(title);
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
