package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

public class SignatureDetailsController {
    private final SigningJob signingJob;

    @FXML
    VBox mainBox;
    @FXML
    WebView webView;
    @FXML
    VBox webViewContainer;

    public SignatureDetailsController(SigningJob signingJob) {
        this.signingJob = signingJob;
    }

    public void showHTMLVisualization() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", signingJob.getSimpleValidationReportAsHTML());
            }
        });
        engine.load(getClass().getResource("visualization-html.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-html");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }
}
