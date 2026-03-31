package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.ui.BatchModeGuiFileResponder;
import digital.slovensko.autogram.ui.OneByOneModeGuiFileResponder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class BatchMethodSelectionDialogController extends BaseController implements SuppressedFocusController {
    private final Batch batch;
    private final BatchResponder batchModeResponder;
    private final Autogram autogram;
    private final GUI gui;
    private final List<File> filesList;
    private final Path targetDirectory;
    private final UserSettings userSettings;

    @FXML
    VBox mainBox;
    @FXML
    public Button signAllButton;
    @FXML
    public Button signOneByOneButton;

    public BatchMethodSelectionDialogController(Batch batch, BatchResponder batchModeResponder, Autogram autogram, GUI gui, List<File> filesList, Path targetDirectory, UserSettings userSettings) {
        this.batch = batch;
        this.batchModeResponder = batchModeResponder;
        this.autogram = autogram;
        this.gui = gui;
        this.filesList = filesList;
        this.targetDirectory = targetDirectory;
        this.userSettings = userSettings;
    }

    @Override
    public void initialize() {
    }

    public void onSignAllButtonPressed(ActionEvent event) {
        close();
        gui.startBatchWithDialog(batch, autogram, batchModeResponder);
    }

    public void onSignOneByOneButtonPressed(ActionEvent event) {
        close();
        var tspSource = userSettings.getTsaEnabled() ? userSettings.getTspSource() : null;
        var oneByOneResponder = new OneByOneModeGuiFileResponder(
                autogram, filesList, targetDirectory,
                userSettings.isPdfaCompliance(), userSettings.getSignatureLevel(),
                userSettings.shouldSignPDFAsPades(), userSettings.isEn319132(),
                tspSource, userSettings.isPlainXmlEnabled());
        gui.startBatchOneByOne(batch, autogram, oneByOneResponder);
    }

    public void close() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public Window getMainWindow() {
        return mainBox.getScene().getWindow();
    }
}
