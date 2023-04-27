package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

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
            for (File file : event.getDragboard().getFiles()) {
                autogram.sign(SigningJob.buildFromFile(file));
            }
        });
    }

    public void onUploadButtonAction() {
        var chooser = new FileChooser();
        var list = chooser.showOpenMultipleDialog(new Stage());

        if (list != null) {
            for (File file : list) {
                autogram.sign(SigningJob.buildFromFile(file));
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
