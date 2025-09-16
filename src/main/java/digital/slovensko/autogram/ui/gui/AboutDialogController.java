package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.Main;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import static digital.slovensko.autogram.ui.gui.TextHighlighter.highlight;

public class AboutDialogController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    @FXML
    Node mainBox;
    @FXML
    Hyperlink link;
    @FXML
    Text versionText;
    @FXML
    TextFlow title;

    public AboutDialogController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        versionText.setText(Main.getVersionString());
        highlight("Autogram").in(title);
    }

    public void githubLinkAction(ActionEvent ignored) {
        hostServices.showDocument("https://github.com/slovensko-digital/autogram");
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
