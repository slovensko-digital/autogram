package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.BatchUiResult;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class BatchSigningSuccessDialogController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    private final BatchUiResult result;

    @FXML
    TextFlow folderTextFlow;
    @FXML
    Node mainBox;

    @FXML
    Text successCount;


    public BatchSigningSuccessDialogController(BatchUiResult result, HostServices hostServices)  {

        this.result = result;
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        initHyperlink();
        var signedFileNamesList = result.getTargetFilesSortedList().stream().filter(e -> e != null)
                .map(file -> file.getName())
                .toList();
        successCount.setText(String.valueOf(signedFileNamesList.size()));
    }

    public void initHyperlink() {
        var path = result.getTargetDirectory().toString().split("((?<=/|\\\\))");
        for (int i = 0; i < path.length; i++) {
            var hyperlink = new Hyperlink(path[i]);
            hyperlink.getStyleClass().add("autogram-body");
            hyperlink.getStyleClass().add("autogram-link");
            hyperlink.getStyleClass().add("autogram-font-weight-bold");
            hyperlink.setOnAction(this::onOpenFolderAction);
            folderTextFlow.getChildren().add(folderTextFlow.getChildren().size() - 1, hyperlink);
        }
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
