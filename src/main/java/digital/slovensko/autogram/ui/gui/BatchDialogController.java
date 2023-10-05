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
    public Button chooseKeyButton;
    @FXML
    public Button signButton;
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

    public void onChooseKeyButtonPressed(ActionEvent event) {
        autogram.pickSigningKeyAndThen(key -> {
            gui.setActiveSigningKeyAndThen(key, null);
            refreshSigningKey();
            enableKeyChange();
        });
    }

    public void onSignButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            return;
        }
        disableSigning();
        disableKeyChange();
        hideVisualization();
        showProgress();
        showCancelButton();
        getNodeForLoosingFocus().requestFocus();
        gui.onWorkThreadDo(() -> {
            startBatchCallback.accept(signingKey);
        });
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        if (batch.isKeyChangeAllowed()) {
            gui.resetSigningKey();
            autogram.pickSigningKeyAndThen((key) -> {
                gui.setActiveSigningKeyAndThen(key, null);
                refreshSigningKey();
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
                signButton.setVisible(false);
                signButton.setManaged(false);

                chooseKeyButton.setVisible(true);
                chooseKeyButton.setManaged(true);

                disableKeyChange();
            } else {
                signButton.setVisible(true);
                signButton.setManaged(true);

                chooseKeyButton.setVisible(false);
                chooseKeyButton.setManaged(false);

                signButton.setText("Podpísať ako "
                        + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));

                enableKeyChange();
            }
        }
    }

    public void enableSigning() {
        signButton.setDisable(false);
        chooseKeyButton.setDisable(false);
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
        chooseKeyButton.setText("Načítavam certifikáty…");
        chooseKeyButton.setDisable(true);
    }

    public void disableSigning() {
        signButton.setVisible(false);
        signButton.setManaged(false);
        // signButton.setText("Prebieha hromadné podpisovanie...");
        // signButton.setDisable(true);
    }

    public void disableKeyChange() {
        changeKeyButton.setVisible(false);
        changeKeyButton.setManaged(false);
    }

    public void enableKeyChange() {
        changeKeyButton.setVisible(true);
        changeKeyButton.setManaged(true);
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
