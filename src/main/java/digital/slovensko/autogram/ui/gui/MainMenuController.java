package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.EmptyDirectorySelectedException;
import digital.slovensko.autogram.core.errors.NoFilesSelectedException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.ui.BatchGuiFileResponder;
import digital.slovensko.autogram.ui.SaveFileResponder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainMenuController implements SuppressedFocusController {
    private final Autogram autogram;
    private final UserSettings userSettings;

    @FXML
    VBox dropZone;

    @FXML
    MenuBar menuBar;

    public MainMenuController(Autogram autogram, UserSettings userSettings) {
        this.autogram = autogram;
        this.userSettings = userSettings;
    }

    public void initialize() {
        if (menuBar != null) {
            menuBar.useSystemMenuBarProperty().set(true);
        }
        
        // Enhanced keyboard navigation
        dropZone.setFocusTraversable(true);
        dropZone.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("SPACE") || event.getCode().toString().equals("ENTER")) {
                onUploadButtonAction();
                event.consume();
            }
        });
        
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                if (!dropZone.getStyleClass().contains("autogram-dropzone-active")) {
                    dropZone.getStyleClass().add("autogram-dropzone-active");
                }
            }
            event.consume();
        });
        
        dropZone.setOnDragExited(event -> {
            dropZone.getStyleClass().remove("autogram-dropzone-active");
            event.consume();
        });

        dropZone.setOnDragEntered(event -> {
            dropZone.getStyleClass().add("autogram-dropzone--entered");
        });

        dropZone.setOnDragExited(event -> {
            dropZone.getStyleClass().removeIf(style -> style.equals("autogram-dropzone--entered"));
        });

        dropZone.setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasFiles()) {
                var files = dragboard.getFiles();
                try {
                    onFilesSelected(files);
                    success = true;
                } catch (Exception e) {
                    // Handle file processing errors gracefully
                    System.err.println("Error processing dropped files: " + e.getMessage());
                    autogram.onSigningFailed(new UnrecognizedException(e));
                }
            }
            
            // Remove active styling
            dropZone.getStyleClass().remove("autogram-dropzone-active");
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void onUploadButtonAction() {
        var chooser = new FileChooser();
        var list = chooser.showOpenMultipleDialog(new Stage());

        try {
            onFilesSelected(list);
        } catch (Exception e) {
            autogram.onSigningFailed(new UnrecognizedException(e));
        }
    }

    public void onFilesSelected(List<File> list) {
        if (list == null)
            return;

        try {
            if (list.size() == 0)
                throw new NoFilesSelectedException();

            var dirsList = list.stream().filter(f -> f.isDirectory()).toList();
            var filesList = list.stream().filter(f -> f.isFile()).toList();

            if (dirsList.size() == 1 && filesList.size() == 0)
                signDirectory(dirsList.get(0));

            if (dirsList.size() == 0 && filesList.size() > 0)
                signFiles(list);

            if (dirsList.size() > 1)
                throw new AutogramException("Zvolili ste viac ako jeden priečinok",
                        "Priečinky musíte podpísať po jednom",
                        "Podpisovanie viacerých priečinkov ešte nepodporujeme");

            if (dirsList.size() > 0 && filesList.size() > 0)
                throw new AutogramException("Zvolili ste zmiešaný výber súborov a priečinkov",
                        "Podpisovanie zmesi súborov a priečinkov nepodporujeme",
                        "Priečinky musíte podpísať po jednom, súbory môžete po viacerých");

        } catch (AutogramException e) {
            autogram.onSigningFailed(e);
        }
    }

    private List<File> getFilesList(List<File> list) {
        var filesList = list.stream().filter(f -> f.isFile()).toList();
        if (filesList.size() == 0)
            throw new NoFilesSelectedException();

        return filesList;
    }

    private void signFiles(List<File> list) {
        // send null tspSource if signature shouldn't be timestamped
        var tspSource = userSettings.getTsaEnabled() ? userSettings.getTspSource() : null;

        var filesList = getFilesList(list);
        if (filesList.size() == 1) {
            var file = filesList.get(0);
            var job = SigningJob.buildFromFile(file,
                    new SaveFileResponder(file, autogram, userSettings.shouldSignPDFAsPades()),
                    userSettings.isPdfaCompliance(), userSettings.getSignatureLevel(), userSettings.isEn319132(), tspSource, userSettings.isPlainXmlEnabled());
            autogram.sign(job);
        } else {
            autogram.batchStart(filesList.size(), new BatchGuiFileResponder(autogram, filesList,
                    filesList.get(0).toPath().getParent().resolve("signed"), userSettings.isPdfaCompliance(),
                    userSettings.getSignatureLevel(), userSettings.shouldSignPDFAsPades(), userSettings.isEn319132(), tspSource, userSettings.isPlainXmlEnabled()));
        }
    }

    private void signDirectory(File dir) {
        var directoryFiles = List.of(dir.listFiles());
        if (directoryFiles.size() == 0)
            throw new EmptyDirectorySelectedException(dir.getAbsolutePath());

        var filesList = getFilesList(directoryFiles);
        var targetDirectoryName = dir.getName() + "_signed";
        var targetDirectory = dir.toPath().getParent().resolve(targetDirectoryName);

        // send null tspSource if signature shouldn't be timestamped
        var tspSource = userSettings.getTsaEnabled() ? userSettings.getTspSource() : null;

        autogram.batchStart(filesList.size(),
                new BatchGuiFileResponder(autogram, filesList, targetDirectory, userSettings.isPdfaCompliance(),
                        userSettings.getSignatureLevel(), userSettings.shouldSignPDFAsPades(),
                        userSettings.isEn319132(), tspSource, userSettings.isPlainXmlEnabled()));
    }

    public void onAboutButtonAction() {
        autogram.onAboutInfo();
    }

    @FXML
    public void onSettingButtonAction() {
        var controller = new SettingsDialogController(userSettings);
        var root = GUIUtils.loadFXML(controller, "settings-dialog.fxml");

        var stage = new Stage();
        stage.setTitle("Nastavenia");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    public void onQuitButtonAction() {
        Platform.exit();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return dropZone;
    }
}
