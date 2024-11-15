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
import eu.europa.esig.dss.model.DSSException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
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

    public MainMenuController(Autogram autogram, UserSettings userSettings) {
        this.autogram = autogram;
        this.userSettings = userSettings;
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

            autogram.wrapInWorkThread(() -> {
                try {
                    var job = SigningJob.buildFromFile(file, autogram,
                            new SaveFileResponder(file, autogram, userSettings.shouldSignPDFAsPades()),
                            userSettings.isPdfaCompliance(), userSettings.getSignatureLevel(), userSettings.isEn319132(), tspSource, userSettings.isPlainXmlEnabled());

                    autogram.sign(job);
                } catch (DSSException e) {
                    autogram.onSigningFailed(AutogramException.createFromDSSException(e));
                } catch (AutogramException e) {
                    autogram.onSigningFailed(e);
                }
            });
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

    @Override
    public Node getNodeForLoosingFocus() {
        return dropZone;
    }
}
