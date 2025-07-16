package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;

public class SigningSuccessDialogController extends BaseController implements SuppressedFocusController {
    private final File targetFile;
    private final File targetDirectory;
    private final HostServices hostServices;
    @FXML
    Text filenameText;
    @FXML
    TextFlow successTextFlow;
    @FXML
    Node mainBox;

    public SigningSuccessDialogController(File targetFile, HostServices hostServices) {
        this(targetFile, targetFile.getParentFile(), hostServices);
    }

    public SigningSuccessDialogController(File targetFile, File targetDirectory, HostServices hostServices) {
        this.targetFile = targetFile;
        this.targetDirectory = targetDirectory;
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        filenameText.setText(targetFile.getName());
        initHyperlink();
    }


    public void initHyperlink() {
        var path = targetFile.getParent().split("((?<=/|\\\\))");
        for (int i = 0; i < path.length; i++) {
            var hyperlink = new Hyperlink(path[i]);
            hyperlink.getStyleClass().add("autogram-body");
            hyperlink.getStyleClass().add("autogram-link");
            hyperlink.getStyleClass().add("autogram-font-weight-bold");
            hyperlink.setOnAction(this::onOpenFolderAction);
            successTextFlow.getChildren().add(successTextFlow.getChildren().size() - 1, hyperlink);
        }
    }

    public void onOpenFolderAction(ActionEvent ignored) {
        hostServices.showDocument(targetDirectory.toURI().toString());
    }

    public void onCloseAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
