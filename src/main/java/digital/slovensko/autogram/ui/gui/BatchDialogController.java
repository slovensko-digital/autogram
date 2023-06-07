package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.AutogramBatchStartCallback;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.util.DSSUtils;
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
    private final AutogramBatchStartCallback startBatchCallback;

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

    public BatchDialogController(Batch batch, AutogramBatchStartCallback startBatchCallback,
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
        updateProgressBar();
        if (batch.isAllProcessed()) {
            batch.end();
            close();
        }
    }

    public void onChooseKeyButtonPressed(ActionEvent event) {
        autogram.pickSigningKeyAndThen(key -> {
            gui.setActiveSigningKey(key);
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
        getNodeForLoosingFocus().requestFocus();
        gui.onWorkThreadDo(startBatchCallback);
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        if (batch.isKeyChangeAllowed()) {
            gui.resetSigningKey();
            autogram.pickSigningKeyAndThen((key) -> {
                gui.setActiveSigningKey(key);
                refreshSigningKey();
            });
        }
    }

    public void onCancelBatchButtonPressed(ActionEvent event) {
        batch.end();
        close();
    }

    public void refreshSigningKey() {
        SigningKey key = gui.getActiveSigningKey();
        if (key == null) {
            signButton.setVisible(false);
            signButton.setManaged(false);

            chooseKeyButton.setVisible(true);
            chooseKeyButton.setManaged(true);
            chooseKeyButton.setDisable(false);
        } else {
            signButton.setVisible(true);
            signButton.setManaged(true);

            chooseKeyButton.setVisible(false);
            chooseKeyButton.setManaged(false);

            signButton.setDisable(false);
            signButton.setText("Podpísať ako "
                    + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));

            changeKeyButton.setVisible(true);
        }
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

    public void hideVisualization() {
        batchVisualization.setVisible(false);
        batchVisualization.setManaged(false);
    }

    public void disableKeyPicking() {
        chooseKeyButton.setText("Načítavam certifikáty...");
        chooseKeyButton.setDisable(true);
    }

    public void disableSigning() {
        signButton.setText("Prebieha hromadné podpisovanie...");
        signButton.setDisable(true);
    }

    public void disableKeyChange() {
        changeKeyButton.setVisible(false);
    }

    public void enableKeyChange() {
        changeKeyButton.setVisible(true);
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
