package digital.slovensko.autogram.ui.gui;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class SignatureDetailsController {
    @FXML
    VBox mainBox;
    @FXML
    WebView webView;
    @FXML
    VBox webViewContainer;

    public SignatureDetailsController() {
    }

    public void showHTMLReport(String htmlReport) {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(true);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", htmlReport);
            }
        });
        engine.load(getClass().getResource("signature-details.html").toExternalForm());
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }
}
