package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.BatchGuiFileResponder;
import digital.slovensko.autogram.ui.SaveFileResponder;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainMenuController implements SuppressedFocusController {
    private final Autogram autogram;

    @FXML
    VBox dropZone;

    public MainMenuController(Autogram autogram) {
        this.autogram = autogram;
    }

    public void initialize() {
        dropZone.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });

        dropZone.setOnDragEntered(event -> {
            dropZone.getStyleClass().add("autogram-dropzone--entered");
        });

        dropZone.setOnDragExited(event -> {
            dropZone.getStyleClass().removeIf(style -> style.equals("autogram-dropzone--entered"));
        });

        dropZone.setOnDragDropped(event -> {
            processFileList(event.getDragboard().getFiles());
        });
    }

    public void onUploadButtonAction() {
        var chooser = new FileChooser();
        var list = chooser.showOpenMultipleDialog(new Stage());
        processFileList(list);
    }

    public void processFileList(List<File> list) {
        if (list != null) {
            if (list.size() == 1) {
                var file = list.get(0);
                var job = SigningJob.buildFromFile(file, new SaveFileResponder(file, autogram), false);
                autogram.sign(job);
            } else {
                autogram.batchStart(list.size(), new BatchGuiFileResponder(autogram, list));
            }
        }
    }

    public void onAboutButtonAction() {
        autogram.onAboutInfo();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return dropZone;
    }
}
