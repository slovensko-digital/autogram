package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;

import java.io.File;
import java.util.List;

public class BatchSigningSuccessDialogController implements SuppressedFocusController {
    private final List<File> targetFiles;
    private final File targetDirectory;
    private final HostServices hostServices;

    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

    @FXML
    Button showFiles;

    @FXML
    ListView<File> fileList;

    public BatchSigningSuccessDialogController(List<File> targetFiles, HostServices hostServices) {
        this(targetFiles, targetFiles.get(0).getParentFile(), hostServices);
    }

    public BatchSigningSuccessDialogController(List<File> targetFiles, File targetDirectory,
            HostServices hostServices) {
        this.targetFiles = targetFiles;
        this.targetDirectory = targetDirectory;
        this.hostServices = hostServices;
    }

    public void initialize() {
        // TODO handle list of files written
        folderPathText.setText(targetDirectory.getPath());
        fileList.setItems(FXCollections.observableArrayList(targetFiles));
    }

    public void onOpenFolderAction(ActionEvent ignored) {
        hostServices.showDocument(targetDirectory.toURI().toString());
    }

    public void onCloseAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    public void onShowFiles(ActionEvent ignored) {

    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
