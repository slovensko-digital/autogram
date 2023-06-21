package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationResult;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.model.CommonDocument;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class SigningDialogController implements SuppressedFocusController {
    private final GUI gui;
    private final SigningJob signingJob;
    private final Autogram autogram;

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
    public Button mainButton;
    @FXML
    public Button changeKeyButton;
    @FXML
    VBox unsupportedVisualizationInfoBox;
    @FXML
    VBox loadingContainer;

    public SigningDialogController(SigningJob signingJob, Autogram autogram, GUI gui) {
        this.signingJob = signingJob;
        this.gui = gui;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();
        showLoading();
    }

    public void updateVisualization(DocumentVisualizationResult visualization) {
        hideLoading();
        mainBox.setPrefWidth(signingJob.getVisualizationWidth());
        if (visualization.hasFailedTransformation()) {
            showFailedTransformationError(visualization.getError());
        }

        var visualizedDocument = visualization.getVisualizedDocument();
        visualizedDocument.show(this);

    }

    public void onMainButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
        } else {
            gui.disableSigning();
            getNodeForLoosingFocus().requestFocus();
            autogram.sign(signingJob, signingKey);
        }
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        gui.resetSigningKey();
        autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
    }

    public void refreshSigningKey() {
        mainButton.setDisable(false);
        SigningKey key = gui.getActiveSigningKey();
        if (key == null) {
            mainButton.setText("Vybrať podpisový certifikát");
            mainButton.getStyleClass().add("autogram-button--secondary");
            changeKeyButton.setVisible(false);
        } else {
            mainButton.setText("Podpísať ako "
                    + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
            mainButton.getStyleClass()
                    .removeIf(style -> style.equals("autogram-button--secondary"));
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
        mainButton.setText("Prebieha podpisovanie...");
        mainButton.setDisable(true);
    }

    private void showLoading() {
        loadingContainer.setVisible(true);
        loadingContainer.setManaged(true);
        mainButton.disableProperty().set(true);

        var p = loadingContainer.lookup(".autogram-loading-icon");
        RotateTransition rotate = new RotateTransition(javafx.util.Duration.millis(100), p);
        rotate.setAxis(Rotate.Z_AXIS);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setDuration(javafx.util.Duration.seconds(3));
        rotate.setFromAngle(0);
        rotate.setToAngle(180);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(Timeline.INDEFINITE);
        rotate.play();
    }

    private void hideLoading() {
        loadingContainer.setVisible(false);
        loadingContainer.setManaged(false);
        mainButton.disableProperty().set(false);
    }

    public void showPlainTextVisualization(String text) {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        plainTextArea.setText(text);
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
    }

    public void showHTMLVisualization(String html) {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {

                engine.getDocument().getElementById("frame").setAttribute("srcdoc", html);

            }
        });
        engine.load(getClass().getResource("visualization-html.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-html");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    public void showPDFVisualization(String base64EncodedPdf) {
        var engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.executeScript(
                        "displayPdf('" + base64EncodedPdf + "')");
            }
        });
        engine.load(getClass().getResource("visualization-pdf.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-pdf");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    public void showImageVisualization(CommonDocument imageDocument) {
        // TODO what about visualization
        imageVisualization.fitWidthProperty()
                .bind(imageVisualizationContainer.widthProperty().subtract(4));
        imageVisualization.setImage(new Image(imageDocument.openStream()));
        imageVisualization.setPreserveRatio(true);
        imageVisualization.setSmooth(true);
        imageVisualization.setCursor(Cursor.OPEN_HAND);
        imageVisualizationContainer.setPannable(true);
        imageVisualizationContainer.setFitToWidth(true);
        imageVisualizationContainer.setVisible(true);
        imageVisualizationContainer.setManaged(true);

    }

    public void showUnsupportedVisualization() {
        unsupportedVisualizationInfoBox.setVisible(true);
        unsupportedVisualizationInfoBox.setManaged(true);
    }

    private void showFailedTransformationError(Exception exception) {
        // showUnsupportedVisualization();
        exception.printStackTrace();
        gui.onTransformationFailed(signingJob, exception);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
