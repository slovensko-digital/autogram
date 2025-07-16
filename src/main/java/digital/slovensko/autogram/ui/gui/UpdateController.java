package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Updater;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class UpdateController implements SuppressedFocusController {
    private final HostServices hostServices;
    private Updater.UpdateInfo updateInfo;
    
    @FXML
    Node mainBox;
    @FXML
    Hyperlink link;
    @FXML
    Button mainButton;
    @FXML
    Button cancelButton;
    @FXML
    ProgressBar progressBar;
    @FXML
    Text progressText;
    @FXML
    Text versionText;
    @FXML
    TextArea releaseNotesArea;

    public UpdateController(HostServices hostServices) {
        this.hostServices = hostServices;
        this.updateInfo = Updater.getUpdateInfo();
    }
    
    @FXML
    public void initialize() {
        if (updateInfo != null) {
            if (versionText != null) {
                versionText.setText("Verzia " + updateInfo.version);
            }
            if (releaseNotesArea != null && !updateInfo.releaseNotes.isEmpty()) {
                releaseNotesArea.setText(updateInfo.releaseNotes);
                releaseNotesArea.setVisible(true);
                releaseNotesArea.setManaged(true);
            }
        }
        
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
        }
        if (progressText != null) {
            progressText.setVisible(false);
            progressText.setManaged(false);
        }
    }

    public void downloadAction(ActionEvent ignored) {
        if (updateInfo != null && updateInfo.downloadUrl.endsWith(".dmg")) {
            // Start automatic download
            startAutomaticDownload();
        } else {
            // Fallback to web browser
            hostServices.showDocument(Updater.getDownloadUrl());
        }
    }
    
    private void startAutomaticDownload() {
        // Show progress UI
        if (progressBar != null) {
            progressBar.setVisible(true);
            progressBar.setManaged(true);
            progressBar.setProgress(0);
        }
        if (progressText != null) {
            progressText.setVisible(true);
            progressText.setManaged(true);
            progressText.setText("Sťahovanie...");
        }
        
        // Disable buttons during download
        mainButton.setDisable(true);
        mainButton.setText("Sťahuje sa...");
        
        // Start download
        CompletableFuture<Path> downloadFuture = Updater.downloadUpdate(updateInfo, progress -> {
            Platform.runLater(() -> {
                if (progressBar != null) {
                    progressBar.setProgress(progress);
                }
                if (progressText != null) {
                    int percentage = (int) (progress * 100);
                    progressText.setText("Sťahovanie... " + percentage + "%");
                }
            });
        });
        
        downloadFuture.whenComplete((path, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    // Handle download error
                    showError("Chyba pri sťahovaní: " + throwable.getMessage());
                    resetUI();
                } else {
                    // Download completed successfully
                    if (progressText != null) {
                        progressText.setText("Sťahovanie dokončené!");
                    }
                    mainButton.setText("Inštalovať");
                    mainButton.setDisable(false);
                    
                    // Set up install action
                    mainButton.setOnAction(e -> installUpdate(path));
                }
            });
        });
    }
    
    private void installUpdate(Path updateFile) {
        try {
            Updater.installUpdate(updateFile);
            // Close the dialog after opening the installer
            GUIUtils.closeWindow(mainBox);
        } catch (Exception e) {
            showError("Chyba pri inštalácii: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba aktualizácie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void resetUI() {
        mainButton.setDisable(false);
        mainButton.setText("Stiahnuť novú verziu");
        mainButton.setOnAction(this::downloadAction);
        
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
        }
        if (progressText != null) {
            progressText.setVisible(false);
            progressText.setManaged(false);
        }
    }

    public void onCancelButtonPressed(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
