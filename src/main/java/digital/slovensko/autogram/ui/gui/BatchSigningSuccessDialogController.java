package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

import java.io.File;
import java.util.List;

public class BatchSigningSuccessDialogController implements SuppressedFocusController {
    private final List<File> targetFiles;
    private final File targetDirectory;
    private final HostServices hostServices;
    @FXML
    Text filenameText;
    @FXML
    Hyperlink folderPathText;
    @FXML
    Node mainBox;

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
        filenameText.setText("Všetky súbory boli úspešne podpísané.");
        folderPathText.setText(targetDirectory.getPath());

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
