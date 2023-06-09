package digital.slovensko.autogram.ui.gui;

import java.io.IOException;
import java.util.Base64;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.ui.Visualizer;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.enumerations.ImageScaling;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class SigningDialogController implements SuppressedFocusController, Visualizer {
    private final GUI gui;
    private final Autogram autogram;
    private final Visualization visualization;

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
    public Button addSignatureButton;

    private SigningDialogJSInterop jsInterop = new SigningDialogJSInterop();
    private DSSDocument signatureDocument;

    public SigningDialogController(Visualization visualization, Autogram autogram, GUI gui) {
        this.visualization = visualization;
        this.gui = gui;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();
        visualization.initialize(this);
    }

    public void onMainButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
        } else {
            gui.disableSigning();
            getNodeForLoosingFocus().requestFocus();
            // autogram.addSignature(signingJob, signingKey, jsInterop.signatures);
            System.out.println("Signing with signatures: " + jsInterop.signature);
            var signatureImageParameters = new SignatureImageParameters();
            signatureImageParameters.setFieldParameters(jsInterop.getSignatureFieldParameters());
            signatureImageParameters.setImage(signatureDocument);
            signatureImageParameters.setImageScaling(ImageScaling.ZOOM_AND_CENTER);
            signatureImageParameters.setAlignmentHorizontal(
                    eu.europa.esig.dss.enumerations.VisualSignatureAlignmentHorizontal.LEFT);
            signatureImageParameters.setAlignmentVertical(
                    eu.europa.esig.dss.enumerations.VisualSignatureAlignmentVertical.TOP);
            visualization.getJob().setParameters(visualization.getJob().getParameters()
                    .withSignatureImageParameters(signatureImageParameters));
            autogram.sign(visualization.getJob(), signingKey);
        }
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        gui.resetSigningKey();
        autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
    }

    public void onAddSignatureButtonPressed(ActionEvent event) {
        var chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(
                new javafx.stage.FileChooser.ExtensionFilter("signature image", "png", "jpg"));
        var file = chooser.showOpenDialog(new Stage());
        var engine = webView.getEngine();

        if (file != null) {
            signatureDocument = new FileDocument(file);
            try {

                var content = (Base64.getUrlEncoder()
                        .encodeToString(signatureDocument.openStream().readAllBytes()));
                engine.executeScript("initSignature('data:" + signatureDocument.getMimeType()
                        + ";base64," + content + "')");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

                var win = (JSObject) engine.executeScript("window");
                win.setMember("javaInterop", jsInterop);

                engine.executeScript(
                        "displayPdf('" + base64EncodedPdf + "')");
            }
        });
        engine.load(getClass().getResource("visualization-pdf.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-pdf");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
        addSignatureButton.setVisible(true);
        addSignatureButton.setManaged(true);
    }

    public void showImageVisualization(CommonDocument doc) {
        // TODO what about visualization
        imageVisualization.fitWidthProperty()
                .bind(imageVisualizationContainer.widthProperty().subtract(4));
        imageVisualization.setImage(new Image(doc.openStream()));
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

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    @Override
    public void setPrefWidth(double prefWidth) {
        mainBox.setPrefWidth(prefWidth);
    }
}
