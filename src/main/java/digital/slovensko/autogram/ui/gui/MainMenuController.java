package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.ui.SaveFileResponder;
import eu.europa.esig.dss.model.FileDocument;
import javafx.fxml.FXML;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class MainMenuController {
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
            event.getDragboard().getContentTypes();
            event.getDragboard().getFiles();

            for(File file: event.getDragboard().getFiles()) {
                var document = new FileDocument(file.getPath());
                document.setName(file.getName());
                SigningParameters parameters = SigningParameters.buildFromFilename(file.getName());
                var responder = new SaveFileResponder(parameters.getContainerFilename());
                autogram.showSigningDialog(new SigningJob(document, parameters, responder));
            }
        });
    }

    public void onUploadButtonAction() {
        ui.showPickFileDialog(autogram);
    }

    public void onAboutButtonAction() {

    }
}
