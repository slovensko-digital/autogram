package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.io.File;

public class MainMenuController implements SuppressedFocusController {
    private final GUI ui;
    private final Autogram autogram;

    @FXML
    VBox dropZone;

    public MainMenuController(GUI ui, Autogram autogram) {
        this.ui = ui;
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
            for (File file : event.getDragboard().getFiles()) {
                autogram.showSigningDialog(SigningJob.buildFromFile(file));
            }
        });
    }

    public void onUploadButtonAction() {
        ui.showPickFileDialog(autogram);
    }

    public void onAboutButtonAction() {
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return dropZone;
    }
}
