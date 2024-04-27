package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.Main;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.AccessibleRole;
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

    public void initAccessible() {
        mainBox.setAccessibleText("This is the main box of the About Dialog");
        //mainBox.setAccessibleRole(AccessibleRole.CON);
        mainBox.setAccessibleHelp("This box contains information about the project, authors, sponsors, and license");

        link.setAccessibleText("This is a link to the GitHub page of the project");
        //link.setAccessibleRole(AccessibleRole.LINK);
        link.setAccessibleHelp("Click this link to open the GitHub page of the project in your web browser");
        versionText.setAccessibleText("This is the version of the application");
        versionText.setAccessibleRole(AccessibleRole.TEXT);
        versionText.setAccessibleHelp("This text displays the current version of the application");
    }

    public void githubLinkAction(ActionEvent ignored) {
        //versionText.setAccessibleRole(AccessibleRole.);
        hostServices.showDocument("https://github.com/slovensko-digital/autogram");
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
