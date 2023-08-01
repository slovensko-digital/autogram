package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.Main;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

public class AboutDialogController implements SuppressedFocusController {
    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    Hyperlink link;
    @FXML
    Text versionText;

    public AboutDialogController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void initialize() {
        versionText.setText(Main.getVersionString());
    }

    public void githubLinkAction(ActionEvent ignored) {
        hostServices.showDocument("https://github.com/slovensko-digital/autogram");
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
