package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.BatchUiResult;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

public class BatchSigningSuccessDialogController implements SuppressedFocusController {
    private final HostServices hostServices;
    private final BatchUiResult result;

    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

    @FXML
    Text successCount;


    public BatchSigningSuccessDialogController(BatchUiResult result, HostServices hostServices)  {

        this.result = result;
        this.hostServices = hostServices;
    }

    public void initialize() {
        folderPathText.setText(result.getTargetDirectory().toString());
        var signedFileNamesList = result.getTargetFilesSortedList().stream().filter(e -> e != null)
                .map(file -> file.getName())
                .toList();
        successCount.setText(String.valueOf(signedFileNamesList.size()));
    }

    public void onOpenFolderAction(ActionEvent ignored) {
        hostServices.showDocument(result.getTargetDirectory().toUri().toString());
    }

    public void onCloseAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    public void onShowFiles(ActionEvent ignored) {
        mainBox.getScene().getWindow().sizeToScene();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
