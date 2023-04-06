package digital.slovensko.autogram.ui.gui;

import com.octosign.whitelabel.ui.WebViewLogger;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.util.DSSUtils;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class SigningDialogController {
    private SigningJob signingJob;
    private Autogram autogram;

    @FXML
    TextArea plainTextArea;
    @FXML
    WebView webView;
    @FXML
    VBox webViewContainer;
    @FXML
    public Button mainButton;
    @FXML
    public Button changeKeyButton;

    public SigningDialogController(SigningJob signingJob, Autogram autogram) {
        this.signingJob = signingJob;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();
        if (signingJob.isPlainText()) {
            showPlainTextVisualization();
        } else if (signingJob.isHTML()) {
            showHTMLVisualization();
        } else if (signingJob.isPDF()) {
            showPDFVisualization();
        } else {
            throw new RuntimeException("Don't know how to visualize document!");
        }
    }

    public void onMainButtonPressed(ActionEvent event) {
        if (autogram.getActiveSigningKey() == null) {
            new Thread(() -> {
                autogram.pickSigningKey();
            }).start();
        } else {
            new Thread(() -> {
                autogram.sign(signingJob);
            }).start();
        }
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        new Thread(() -> {
            autogram.resetSigningKey();
            autogram.pickSigningKey();
        }).start();
    }

    public void refreshSigningKey() {
        mainButton.setDisable(false);
        if (autogram.getActiveSigningKey() == null) {
            mainButton.setText("Vybrať podpisový certifikát");
            mainButton.getStyleClass().add("autogram-button--secondary");
            changeKeyButton.setVisible(false);
        } else {
            mainButton.setText("Podpísať ako " + DSSUtils.parseCN(autogram.getActiveSigningKey().getCertificate().getSubject().getRFC2253()));
            mainButton.getStyleClass().removeIf(style -> style.equals("autogram-button--secondary"));
            changeKeyButton.setVisible(true);
        }
    }

    public void hide() {
        var window = mainButton.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public void disableKeyPicking() {
        mainButton.setText("Načítavam certifikáty...");
        mainButton.setDisable(true);
    }

    private void showPlainTextVisualization() {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        plainTextArea.setText(signingJob.getDocumentAsPlainText());
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
    }

    private void showHTMLVisualization() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", signingJob.getDocumentAsHTML());
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
                WebViewLogger.register(engine); // TODO remove?
                engine.executeScript("displayPdf('" + signingJob.getDocumentAsBase64Encoded() + "')");
            }
        });
        engine.load(getClass().getResource("visualization-pdf.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-pdf");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }
}
