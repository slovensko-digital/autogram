package digital.slovensko.autogram.ui.gui;

import java.util.function.Consumer;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.util.DSSUtils;
import digital.slovensko.autogram.util.Logging;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

public class BatchDialogController implements SuppressedFocusController {
    private final GUI gui;
    private final Batch batch;
    private final Autogram autogram;
    private final Consumer<SigningKey> startBatchCallback;

    @FXML
    VBox mainBox;
    @FXML
    VBox progressBarBox;
    @FXML
    VBox batchVisualization;
    @FXML
    Text batchVisualizationCount;
    @FXML
    public Button mainButton;
    @FXML
    public Button changeKeyButton;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Text progressBarText;
    @FXML
    public Button cancelBatchButton;

    public BatchDialogController(Batch batch, Consumer<SigningKey> startBatchCallback,
            Autogram autogram, GUI gui) {

        this.gui = gui;
        this.autogram = autogram;
        this.batch = batch;
        this.startBatchCallback = startBatchCallback;
    }

    public void initialize() {
        refreshSigningKey();
        updateVisualizationCount();
    }

    public void update() {
        Logging.log("BatchDialogController.update() " + Platform.isFxApplicationThread());
        batch.log();
        updateProgressBar();
        if (batch.isAllProcessed()) {
            batch.end();
            close();
        }
    }

    public void onMainButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            autogram.pickSigningKeyAndThen(key -> {
                gui.setActiveSigningKeyAndThen(key, k -> {
                    hideVisualization();
                    showProgress();
                    showCancelButton();
                    getNodeForLoosingFocus().requestFocus();
                    gui.onWorkThreadDo(() -> {
                        startBatchCallback.accept(k);
                    });
                });
            });
        } else {
            hideVisualization();
            showProgress();
            showCancelButton();
            getNodeForLoosingFocus().requestFocus();
            gui.onWorkThreadDo(() -> {
                startBatchCallback.accept(signingKey);
            });
        }
    }


    public void onChangeKeyButtonPressed(ActionEvent event) {
        if (batch.isKeyChangeAllowed()) {
            gui.resetSigningKey();
            autogram.pickSigningKeyAndThen(key -> {
                gui.setActiveSigningKeyAndThen(key, k -> {
                    hideVisualization();
                    showProgress();
                    showCancelButton();
                    getNodeForLoosingFocus().requestFocus();
                    gui.onWorkThreadDo(() -> {
                        startBatchCallback.accept(k);
                    });
                });
            });
        }
    }

    public void onCancelBatchButtonPressed(ActionEvent event) {
        batch.end();
        close();
    }

    public void refreshSigningKey() {
        if (batch.isKeyChangeAllowed()) {
            var key = gui.getActiveSigningKey();

            if (key == null) {
                mainButton.setText("Podpísať");
                changeKeyButton.setVisible(false);

            } else {
                mainButton.setText("Podpísať ako " + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
                changeKeyButton.setVisible(true);
            }
        }
    }

    public void enableSigning() {
        mainButton.setDisable(false);
        changeKeyButton.setDisable(false);
    }

    public void close() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public void showProgress() {
        progressBarBox.setVisible(true);
        progressBarBox.setManaged(true);
        updateProgressBar();
    }

    public void showCancelButton() {
        cancelBatchButton.setVisible(true);
        cancelBatchButton.setManaged(true);
    }

    public void hideVisualization() {
        batchVisualization.setVisible(false);
        batchVisualization.setManaged(false);
    }

    public void disableKeyPicking() {
        mainButton.setText("Načítavam certifikáty…");
        mainButton.setDisable(true);
        changeKeyButton.setDisable(true);
    }

    public void disableSigning() {
        mainButton.setText("Prebieha podpisovanie…");
        mainButton.setDisable(true);
        changeKeyButton.setDisable(true);
    }

    private void updateVisualizationCount() {
        batchVisualizationCount.setText(batch.getTotalNumberOfDocuments() + "");
    }

    private void updateProgressBar() {
        progressBar.setProgress(
                (double) batch.getProcessedDocumentsCount() / batch.getTotalNumberOfDocuments());
        progressBarText.setText(String.format("%d / %d", batch.getProcessedDocumentsCount(),
                batch.getTotalNumberOfDocuments()));
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public Window getMainWindow() {
        return mainBox.getScene().getWindow();
    }
}
