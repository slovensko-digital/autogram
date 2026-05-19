package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.BatchUiResult;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class BatchSigningFailureDialogController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    private final BatchUiResult result;

    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

    @FXML
    ListView<String> errorList;

    @FXML
    Text errorListHeading;

    @FXML
    TextArea errorDetails;

    @FXML
    Button showErrorDetailsButton;

    @FXML
    Text successCount;

    @FXML
    Text failureCount;

    private BooleanProperty errorDetailsVisible = new SimpleBooleanProperty(false);

    private static final int LIST_CELL_HEIGHT = 24;

    public BatchSigningFailureDialogController(BatchUiResult result, HostServices hostServices) {

        this.result = result;
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() {
        folderPathText.setText(result.getTargetDirectory().toString());

        successCount.setText(String.valueOf(result.getTargetFilesSortedList().size()));

        var errorFileNamesList = result.getFailedFilesList().map(file -> file.getName()).toList();
        errorList.setItems(FXCollections
                .observableArrayList(errorFileNamesList));
        errorList.prefHeightProperty().bind(Bindings.min(4, Bindings.size(errorList.getItems())).multiply(LIST_CELL_HEIGHT).add(2));
        errorListHeading.setText(i18n("batchSigning.failure.errorList.title", errorFileNamesList.size()));
        failureCount.setText(String.valueOf(errorFileNamesList.size()));

        errorDetails.visibleProperty().bind(errorDetailsVisible);
        errorDetails.managedProperty().bind(errorDetailsVisible);
        showErrorDetailsButton.textProperty()
                .bind(Bindings.when(errorDetailsVisible).then(i18n("batchSigning.failure.hideDetails.btn"))
                        .otherwise(i18n("batchSigning.failure.showDetails.btn")));

    }

    public void onOpenFolderAction(ActionEvent ignored) {
        hostServices.showDocument(result.getTargetDirectory().toUri().toString());
    }

    public void onCloseAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    public void onShowErrorDetailsButtonAction(ActionEvent ignored) {
        errorDetailsVisible.setValue(!errorDetailsVisible.getValue());

        if (errorDetailsVisible.getValue()) {
            String wholeErrors = result.getErrorsMap().entrySet().stream().filter(e -> e.getValue() != null)
                    .map(e -> "%s\n\nError:\n%s".formatted(e.getKey(), GUIUtils.exceptionToString(e.getValue())))
                    .reduce("", (a, b) -> a + "\n==============\n\n" + b);
            errorDetails.setText(wholeErrors);
            showErrorDetailsButton.getStyleClass().add("autogram-error-summary__more-open");
        } else {
            showErrorDetailsButton.getStyleClass().remove("autogram-error-summary__more-open");
        }

        mainBox.getScene().getWindow().sizeToScene();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
