package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.AutogramBatchStartCallback;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.util.DSSUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
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
    TextArea plainTextArea;
    @FXML
    WebView webView;
    @FXML
    VBox webViewContainer;
    @FXML
    ImageView imageVisualization;
    @FXML
    ScrollPane imageVisualizationContainer;
    @FXML
    VBox unsupportedVisualizationInfoBox;
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
        showPlainTextVisualization();
        updateProgressBar();
    }

    public void update() {
        updateProgressBar();
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

    private void showPlainTextVisualization() {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        plainTextArea.setText(String.format(
                "Tu sa bude nachadzat nejaky text o tom co sa ide diat, pripadne ine informacie (popis originu?)",
                batch.getProcessedDocumentsCount(), batch.getTotalNumberOfDocuments()));
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
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
