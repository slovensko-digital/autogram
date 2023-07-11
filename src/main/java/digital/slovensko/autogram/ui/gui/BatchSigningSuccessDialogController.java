package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.BatchGuiResult;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class BatchSigningSuccessDialogController implements SuppressedFocusController {
    private final HostServices hostServices;
    private final BatchGuiResult result;

    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

    @FXML
    Button showFiles;

    @FXML
    ListView<String> fileList;

    @FXML
    ListView<String> errorList;

    @FXML
    Text h1;

    @FXML
    Text fileListHeading;

    @FXML
    Text errorListHeading;

    @FXML
    VBox fileListBox;

    @FXML
    VBox errorListBox;

    @FXML
    TextArea errorDetails;

    @FXML
    Button showErrorDetailsButton;

    @FXML
    VBox listsBox;

    @FXML
    VBox confirmationBox;

    private BooleanProperty errorDetailsVisible = new SimpleBooleanProperty(false);

    private static final int LIST_CELL_HEIGHT = 24;

    public BatchSigningSuccessDialogController(BatchGuiResult result, HostServices hostServices) {

        this.result = result;
        this.hostServices = hostServices;
    }

    public void initialize() {
        folderPathText.setText(result.getTargetDirectory().toString());
        var signedFileNamesList = result.getTargetFilesSortedList().stream().filter(e -> e != null)
                .map(file -> file.getName())
                .toList();

        fileList.setItems(
                FXCollections.observableArrayList(signedFileNamesList));

        fileList.prefHeightProperty().bind(Bindings.size(fileList.getItems()).multiply(LIST_CELL_HEIGHT).add(2));
        fileListHeading.setText("Úspešne podpísané (" + signedFileNamesList.size() + ")");
        showFiles.setText("Zobraziť  súbory (" + signedFileNamesList.size() + ")");

        if (result.hasErrors()) {
            confirmationBox.getStyleClass().add("autogram-confirmation--warning");
            listsBox.setVisible(true);
            listsBox.setManaged(true);
            showFiles.setVisible(false);
            showFiles.setManaged(false);
            errorListBox.setVisible(true);
            errorListBox.setManaged(true);

            var errorFileNamesList = result.getFailedFilesList().map(file -> file.getName()).toList();
            errorList.setItems(FXCollections
                    .observableArrayList(errorFileNamesList));
            errorList.prefHeightProperty().bind(Bindings.size(errorList.getItems()).multiply(LIST_CELL_HEIGHT).add(2));
            errorListHeading.setText("Neúspešné (" + errorFileNamesList.size() + ")");
            h1.setText("Podpisovanie dokončené s chybami");
        } else {
            confirmationBox.getStyleClass().add("autogram-confirmation--success");
            errorListBox.setVisible(false);
            errorListBox.setManaged(false);
            h1.setText("Dokumenty boli úspešne podpísané");
        }

        errorDetails.visibleProperty().bind(errorDetailsVisible);
        errorDetails.managedProperty().bind(errorDetailsVisible);
        showErrorDetailsButton.textProperty()
                .bind(Bindings.when(errorDetailsVisible).then("Skryť podrobnosti chyby")
                        .otherwise("Zobraziť podrobnosti chyby"));

    }

    public void onOpenFolderAction(ActionEvent ignored) {
        hostServices.showDocument(result.getTargetDirectory().toUri().toString());
    }

    public void onCloseAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    public void onShowFiles(ActionEvent ignored) {
        listsBox.setVisible(true);
        listsBox.setManaged(true);
        showFiles.setVisible(false);
        showFiles.setManaged(false);
        mainBox.getScene().getWindow().sizeToScene();
    }

    public void onShowErrorDetailsButtonAction(ActionEvent ignored) {
        errorDetailsVisible.setValue(!errorDetailsVisible.getValue());

        if (errorDetailsVisible.getValue()) {
            String wholeErrors = result.getErrorsMap().entrySet().stream().filter(e -> e.getValue() != null)
                    .map(e -> "%s\n\nError:\n%s".formatted(e.getKey(), GUIUtils.exceptionToString(e.getValue())))
                    .reduce("", (a, b) -> a + "\n==============\n\n" + b);
            errorDetails.setText(wholeErrors);
        }

        mainBox.getScene().getWindow().sizeToScene();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
