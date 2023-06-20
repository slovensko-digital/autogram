package digital.slovensko.autogram.ui.gui;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.util.DSSUtils;
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

    public SigningDialogController(SigningJob signingJob, Autogram autogram, GUI gui) {
        this.signingJob = signingJob;
        this.gui = gui;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();

        mainBox.setPrefWidth(signingJob.getVisualizationWidth());

        if (signingJob.hasFailedTransformation()) {
            showFailedTransformationError(signingJob.getParameters().getTransformationException());
        } else if (signingJob.isPlainText()) {
            showPlainTextVisualization();
        } else if (signingJob.isHTML()) {
            showHTMLVisualization();
        } else if (signingJob.isPDF()) {
            showPDFVisualization();
        } else if (signingJob.isImage()) {
            showImageVisualization();
        } else {
            showUnsupportedVisualization();
        }
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

    private void showPlainTextVisualization() {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        gui.onWorkThreadDo(() -> {
            try {
                var text = signingJob.getDocumentAsPlainText();
                gui.onUIThreadDo(() -> {
                    plainTextArea.setText(text);
                    plainTextArea.setVisible(true);
                    plainTextArea.setManaged(true);
                });

            } catch (IOException | ParserConfigurationException | SAXException
                    | TransformerException e) {
                gui.onUIThreadDo(() -> {
                    plainTextArea.setVisible(false);
                    plainTextArea.setManaged(false);
                    showFailedTransformationError(e);
                });
            }
        });
    }

    private void showHTMLVisualization() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                gui.onWorkThreadDo(() -> {
                    try {
                        var html = signingJob.getDocumentAsHTML();
                        gui.onUIThreadDo(() -> {
                            engine.getDocument().getElementById("frame").setAttribute("srcdoc",
                                    html);
                        });
                    } catch (IOException | ParserConfigurationException | SAXException
                            | TransformerException e) {

                        gui.onUIThreadDo(() -> {
                            webViewContainer.setVisible(false);
                            webViewContainer.setManaged(false);
                            showFailedTransformationError(e);
                        });
                    }
                });
            }
        });
        engine.load(getClass().getResource("visualization-html.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-html");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    private void showPDFVisualization() {
        var engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.executeScript(
                        "displayPdf('" + signingJob.getDocumentAsBase64Encoded() + "')");
            }
        });
        engine.load(getClass().getResource("visualization-pdf.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-pdf");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    private void showImageVisualization() {
        imageVisualization.fitWidthProperty()
                .bind(imageVisualizationContainer.widthProperty().subtract(4));
        imageVisualization.setImage(new Image(signingJob.getDocument().openStream()));
        imageVisualization.setPreserveRatio(true);
        imageVisualization.setSmooth(true);
        imageVisualization.setCursor(Cursor.OPEN_HAND);
        imageVisualizationContainer.setPannable(true);
        imageVisualizationContainer.setFitToWidth(true);
        imageVisualizationContainer.setVisible(true);
        imageVisualizationContainer.setManaged(true);

    }

    private void showUnsupportedVisualization() {
        unsupportedVisualizationInfoBox.setVisible(true);
        unsupportedVisualizationInfoBox.setManaged(true);
    }

    private void showFailedTransformationError(Exception exception) {
        showUnsupportedVisualization();
        gui.onUIThreadDo(() -> gui.onTransformationFailed(signingJob, exception));
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
