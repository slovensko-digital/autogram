package digital.slovensko.autogram.ui.gui;

import java.util.function.Consumer;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.BatchManager;
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
    private final BatchManager batchManager;
    private final Autogram autogram;
    private final Consumer<SigningKey> callback;

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
    public Button mainButton;
    @FXML
    public Button changeKeyButton;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public Text progressBarText;

    @FXML
    public Button cancelBatchButton;

    public BatchDialogController(BatchManager batchManager, Consumer<SigningKey> callback, Autogram autogram, GUI gui) {

        this.gui = gui;
        this.autogram = autogram;
        this.batchManager = batchManager;
        this.callback = callback;
    }

    public void initialize() {
        refreshSigningKey();
        showPlainTextVisualization();
        updateProgressBar();
    }

    public void update() {
        updateProgressBar();
    }

    public void onMainButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {

            autogram.pickSigningKeyAndThen(key -> {
                gui.setActiveSigningKey(key);
                refreshSigningKey();
            });
        } else {
            disableSigning();
            getNodeForLoosingFocus().requestFocus();
            callback.accept(signingKey);
        }
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        gui.resetSigningKey();
        autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
    }

    public void onCancelBatchButtonPressed(ActionEvent event) {
        // TODO
        // batchManager.stop();
        close();
    }

    public void refreshSigningKey() {
        mainButton.setDisable(false);
        SigningKey key = gui.getActiveSigningKey();
        if (key == null) {
            mainButton.setText("Vybrať podpisový certifikát");
            mainButton.getStyleClass().add("autogram-button--secondary");
            changeKeyButton.setVisible(false);
        } else {
            mainButton.setText("Podpísať ako " + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
            mainButton.getStyleClass().removeIf(style -> style.equals("autogram-button--secondary"));
            changeKeyButton.setVisible(true);
        }
    }

    public void close() {
        var window = mainButton.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public void disableKeyPicking() {
        mainButton.setText("Načítavam certifikáty...");
        mainButton.setDisable(true);
    }

    public void disableSigning() {
        mainButton.setText("Prebieha hromadné podpisovanie...");
        mainButton.setDisable(true);
    }

    private void showPlainTextVisualization() {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        plainTextArea.setText(String.format(
                "Tu sa bude nachadzat nejaky text o tom co sa ide diat, pripadne ine informacie (popis originu?)",
                batchManager.getProcessedDocumentsCount(), batchManager.getTotalNumberOfDocuments()));
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
    }

    private void updateProgressBar() {
        progressBar.setProgress(
                (double) batchManager.getProcessedDocumentsCount() / batchManager.getTotalNumberOfDocuments());
        progressBarText.setText(String.format("%d / %d", batchManager.getProcessedDocumentsCount(),
                batchManager.getTotalNumberOfDocuments()));
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public Window getMainWindow() {
        return mainBox.getScene().getWindow();
    }
}
