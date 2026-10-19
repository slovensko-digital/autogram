package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.BatchUiResult;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.VBox;

public class BatchSigningSuccessDialogController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    private final BatchUiResult result;

    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

    @FXML
    Text successCount;
    @FXML
    VBox confirmationBox;
    @FXML
    Text heading;
    @FXML
    TextFlow successCountFlow;
    @FXML
    TextFlow skippedCountFlow;
    @FXML
    Text skippedCount;
    @FXML
    TextFlow folderFlow;


    public BatchSigningSuccessDialogController(BatchUiResult result, HostServices hostServices)  {

        this.result = result;
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        folderPathText.setText(GUIUtils.wrappablePath(result.getTargetDirectory().toString()));
        var signedFileNamesList = result.getTargetFilesSortedList().stream().filter(e -> e != null)
                .map(file -> file.getName())
                .toList();
        successCount.setText(String.valueOf(signedFileNamesList.size()));

        var signed = result.getSignedCount();
        var skipped = result.getSkippedCount();
        skippedCount.setText(String.valueOf(skipped));
        show(skippedCountFlow, skipped > 0);
        show(successCountFlow, signed > 0);
        show(folderFlow, signed > 0);
        show(folderPathText, signed > 0);
        if (signed == 0)
            heading.setText(i18n("batchSigning.nothingSigned.title"));
        if (skipped > 0) {
            confirmationBox.getStyleClass().remove("autogram-confirmation--success");
            confirmationBox.getStyleClass().add("autogram-confirmation--warning");
        }
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
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
