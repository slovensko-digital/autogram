package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainMenuController implements SuppressedFocusController {
    private final Autogram autogram;

    private final GUI ui;

    @FXML
    VBox dropZone;

    public MainMenuController(Autogram autogram, GUI ui) {
        this.autogram = autogram;
        this.ui = ui;
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
            for (File file : event.getDragboard().getFiles()) {
                SigningJob job = SigningJob.buildFromFile(file, autogram);
                autogram.sign(job);
            }
        });
    }

    public void onUploadButtonAction() {
        var chooser = new FileChooser();
        var list = chooser.showOpenMultipleDialog(new Stage());

        if (list != null) {
            for (File file : list) {
                SigningJob job = SigningJob.buildFromFile(file, autogram);
                autogram.sign(job);
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
