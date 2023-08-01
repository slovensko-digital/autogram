package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.EmptyDirectorySelectedException;
import digital.slovensko.autogram.core.errors.NoFilesSelectedException;
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
            onFilesSelected(event.getDragboard().getFiles());
        });
    }

    public void onUploadButtonAction() {
        var chooser = new FileChooser();
        var list = chooser.showOpenMultipleDialog(new Stage());
        onFilesSelected(list);
    }

    public void onFilesSelected(List<File> list) {
        try {
            if (list != null) {
                if (list.size() == 0) {
                    throw new NoFilesSelectedException();
                }

                var dirsList = list.stream().filter(f -> f.isDirectory()).toList();
                var filesList = list.stream().filter(f -> f.isFile()).toList();
                if (dirsList.size() == 1 && filesList.size() == 0) {
                    signDirectory(dirsList.get(0));
                } else if (dirsList.size() > 1) {
                    throw new AutogramException("Zvolili ste viac ako jeden priečinok",
                            "Priečinky musíte podpísať po jednom",
                            "Podpisovanie viacerých priečinkov ešte nepodporujeme");
                } else if (dirsList.size() == 0 && filesList.size() > 0) {
                    signFiles(list);
                } else {
                    throw new AutogramException("Zvolili ste zmiešaný výber súborov a priečinkov",
                            "Podpisovanie zmesi súborov a priečinkov nepodporujeme",
                            "Priečinky musíte podpísať po jednom, súbory môžete po viacerých");
                }
            }
        } catch (AutogramException e) {
            autogram.onSigningFailed(e);
        } catch (Exception e) {
            throw e;
        }
    }

    private List<File> getFilesList(List<File> list) {
        var filesList = list.stream().filter(f -> f.isFile()).toList();
        if (filesList.size() == 0) {
            throw new NoFilesSelectedException();
        }
        return filesList;
    }

    private void signFiles(List<File> list) {
        var filesList = getFilesList(list);
        if (filesList.size() == 1) {
            var file = filesList.get(0);
            var job = SigningJob.buildFromFile(file, new SaveFileResponder(file, autogram), false);
            autogram.sign(job);
        } else {
            autogram.batchStart(filesList.size(), new BatchGuiFileResponder(autogram, filesList,
                    filesList.get(0).toPath().getParent().resolve("signed")));
        }
    }

    private void signDirectory(File dir) {
        var directoryFiles = List.of(dir.listFiles());
        if (directoryFiles.size() == 0) {
            throw new EmptyDirectorySelectedException(dir.getAbsolutePath());
        }
        var filesList = getFilesList(directoryFiles);

        var targetDirectoryName = dir.getName() + "_signed";
        var targetDirectory = dir.toPath().getParent().resolve(targetDirectoryName);
        autogram.batchStart(filesList.size(),
                new BatchGuiFileResponder(autogram, filesList, targetDirectory));
    }

    public void onAboutButtonAction() {
        autogram.onAboutInfo();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return dropZone;
    }
}
