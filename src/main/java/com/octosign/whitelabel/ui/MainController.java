package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.*;
import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.*;
import com.octosign.whitelabel.ui.about.AboutDialog;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

import static com.octosign.whitelabel.communication.MimeType.*;
import static com.octosign.whitelabel.signing.Token.getAvailableDrivers;
import static com.octosign.whitelabel.ui.FXUtils.displayError;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.Utils.*;

/**
 * Controller for the signing window
 */
public class MainController {

    @FXML
    public Button aboutButton;

    @FXML
    public Button certSettingsButton;

    @FXML
    private WebView webView;

    @FXML
    private TextArea textArea;

    @FXML
    private Label signLabel;

    /**
     * Bottom-right button used to load/pick certificate and sign
     */
    @FXML
    private Button mainButton;

    /**
     * Signing certificate manager
     */
    private SigningManager signingManager;

    /**
     * Wrapper for document in this window (to be signed) and parameters
     */
    private SignatureUnit signatureUnit;

    /**
     * Consumer of the signed document content on success
     */
    private Consumer<byte[]> onSigned;

    public void initialize() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
    }

    public void setSigningManager(SigningManager signingManager) {
        this.signingManager = signingManager;
    }

    public void setOnSigned(Consumer<byte[]> onSigned) {
        this.onSigned = onSigned;
    }

    public void setSignatureUnit(SignatureUnit signatureUnit) {
        this.signatureUnit = signatureUnit;
    }

    public void loadDocument() {
        mainButton.setText(resolveMainButtonText());

        var document = signatureUnit.getDocument();
        var params = signatureUnit.getSignatureParameters();
        var mimeType = signatureUnit.getMimeType();

        if (mimeType.is(XML)) {
            XMLDocument xmlDocument = (XMLDocument) document;
            xmlDocument.validate(params.getSchema());
            var visualisation = xmlDocument.getTransformed(params.getTransformation());

            MimeType transformationOutput = params.getTransformationOutputMimeType();
            if (transformationOutput.is(PLAIN))
                displayPlainTextVisualisation(visualisation);
            else
                displayHTMLVisualisation(visualisation);

        }

        if (mimeType.is(PDF))
            displayPDFVisualisation(document);

        if (!mimeType.is(PDF) && !mimeType.is(XML))
            displayPlainTextVisualisation(document.getContentString());

    }

    private void displayPlainTextVisualisation(String visualisation) {
        vanish(webView);
        textArea.setText(visualisation);
    }

    private void displayHTMLVisualisation(String visualisation) {
        vanish(textArea);

        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument()
                    .getElementById("frame")
                    .setAttribute("srcdoc", visualisation);
            }
        });
        engine.load(Main.class.getResource("visualization.html").toExternalForm());
    }

    private void displayPDFVisualisation(Document document) {
        vanish(textArea);

        Platform.runLater(() -> {
            var engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);

            engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    WebViewLogger.register(engine);
                    engine.executeScript("displayPdf('" + encodeBase64(document.getContent()) + "')");
                }
            });

            engine.load(Main.class.getResource("pdf.html").toExternalForm());
        });
    }

    @FXML
    private void onMainButtonAction() {
        if (!isSignerReady()) {
            loadSigners();
        } else {
            signDocument();
        }
    }

    private boolean isSignerReady() {
        return signingManager != null && signingManager.getActiveCertificate() != null;
    }

    private void loadSigners() {
        disableAndSet(translate("btn.loading"));

        try {
            var driver = getIfSingle_selectIfMany(getAvailableDrivers());
            var certificates = Token.fromDriver(driver).getCertificates();

            var signingCertificate = getIfSingle_selectIfMany(certificates);
            signingManager.setActiveCertificate(signingCertificate);

        } catch (UserException e) {
            displayError(e);
        } finally {
            enableAndSet(resolveMainButtonText());
        }
        enableAndSet(resolveMainButtonText());
    }

    private <T extends SelectableItem> T getIfSingle_selectIfMany(List<T> items) {
        if (isNullOrEmpty(items))
            throw new RuntimeException("Collection is null or empty!");

        if (items.size() == 1) {
            return first(items);
        } else {
            var currentStage = (Stage) mainButton.getScene().getWindow();
            var selectDialog = new SelectDialog<>(items, currentStage);

            return selectDialog.getResult();
        }
    }

    private void signDocument() {
        disableAndSet(translate("btn.signing"));

        try {
            var signedContent = signingManager.sign(signatureUnit);
            onSigned.accept(signedContent);
        } catch (UserException e) {
            displayError(e);
        }

        enableAndSet(resolveMainButtonText());
    }

    private void disableAndSet(String text) {
        mainButton.setText(text);
        mainButton.setDisable(true);
    }

    private void enableAndSet(String text) {
        mainButton.setText(text);
        mainButton.setDisable(false);
    }

    public String resolveMainButtonText() {
        if (isSignerReady()) {
            return translate("btn.signAs", signingManager.getActiveCertificate().getDisplayedName());
        } else {
            return translate("btn.loadSigners");
        }
    }

    private void vanish(Node node) {
        if (node.isManaged())
            node.setManaged(false);
        if (node.isVisible())
            node.setVisible(false);
    }

    @FXML
    private void onAboutButtonAction() {
        new AboutDialog().show();
    }

    @FXML
    private void onCertSettingsButtonAction() {
        loadSigners();
    }
}
