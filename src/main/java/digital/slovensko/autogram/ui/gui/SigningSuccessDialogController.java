package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

import java.io.File;

public class SigningSuccessDialogController implements SuppressedFocusController {
    private final File targetFile;
    private final HostServices hostServices;
    @FXML
    Text filenameText;
    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

    public SigningSuccessDialogController(File targetFile, HostServices hostServices) {
        this.targetFile = targetFile;
        this.hostServices = hostServices;
    }

    public void initialize() {
        filenameText.setText(targetFile.getName());
        folderPathText.setText(targetFile.getParent());
    }

    public void onOpenFolderAction(ActionEvent ignored) {
        hostServices.showDocument("file://" + targetFile.getParent());
    }

    public void onCloseAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
