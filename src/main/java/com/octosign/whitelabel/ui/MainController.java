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

import static com.octosign.whitelabel.signing.Token.getAvailableDrivers;
import static com.octosign.whitelabel.ui.FXUtils.displayError;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.Utils.first;
import static com.octosign.whitelabel.ui.Utils.isNullOrEmpty;

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
    private Consumer<String> onSigned;

    public void initialize() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
    }

    public void setSigningManager(SigningManager signingManager) {
        this.signingManager = signingManager;
    }

    public void setOnSigned(Consumer<String> onSigned) {
        this.onSigned = onSigned;
    }

    public void setSignatureUnit(SignatureUnit signatureUnit) {
        this.signatureUnit = signatureUnit;
    }

    public void loadDocument() {
        mainButton.setText(resolveMainButtonText());

        var document = signatureUnit.getDocument();
        var parameters = signatureUnit.getSignatureParameters();

        if (document.getLegalEffect() != null && !document.getLegalEffect().isBlank()) {
            signLabel.setText(document.getLegalEffect());
            signLabel.setVisible(true);
        }

        boolean isXML = document instanceof XMLDocument;
        boolean isPDF = document instanceof PDFDocument;
        final boolean hasSchema = isXML && ((XMLDocument) document).getSchema() != null;
        final boolean hasTransformation = isXML && ((XMLDocument) document).getTransformation() != null;

        if (hasSchema) {
            ((XMLDocument) document).validate();
        }

        if (hasTransformation) {
            var visualisation = ((XMLDocument) document).getTransformed();

            MimeType transformationOutputType;
            try {
                transformationOutputType = parameters.getTransformationOutputMimeType() == null
                    ? MimeType.HTML
                    : MimeType.parse(parameters.getTransformationOutputMimeType());
            } catch (MalformedMimetypeException e) {
                throw new IntegrationException(Code.MALFORMED_MIMETYPE, e);
            }

            if (transformationOutputType.equalsTypeSubtype(MimeType.HTML)) {
                displayHTMLVisualisation(visualisation);
            } else {
                displayPlainTextVisualisation(visualisation);
            }
        } else if (isPDF) {
            displayPDFVisualisation(document);
        } else {
            displayPlainTextVisualisation(document.getContent());
        }
    }

    private void displayPlainTextVisualisation(String visualisation) {
        vanish(webView);
        textArea.setText(visualisation.translateEscapes());
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
                    engine.executeScript("displayPdf('" + document.getContent() + "')");
                }
            });

            engine.load(Main.class.getResource("pdf.html").toExternalForm());
        });
    }

    @FXML
    private void onMainButtonAction() {
        if (!isSignerReady()) {
            disableAndSet(translate("btn.loading"));
            loadSigners();

        } else {
            disableAndSet(translate("btn.signing"));
            signDocument();
        }

        enableAndSet(resolveMainButtonText());
    }

    private boolean isSignerReady() {
        return signingManager != null && signingManager.getActiveCertificate() != null;
    }

    private void loadSigners() {
        try {
            var driver = getIfSingle_selectIfMany(getAvailableDrivers());
            var certificates = driver.createToken().getCertificates();

            var signingCertificate = getIfSingle_selectIfMany(certificates);
            signingManager.setActiveCertificate(signingCertificate);

        } catch (UserException e) {
            displayError(e);
        }
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
        try {
            var signedContent = signingManager.sign(signatureUnit);
            onSigned.accept(signedContent);
        } catch (UserException e) {
            displayError(e);
        }
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
            return translate("btn.signAs", signingManager.getActiveCertificate().getSimpleName());
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

    private void materialize(Node node) {
        if (!node.isManaged())
            node.setManaged(true);
        if (!node.isVisible())
            node.setVisible(true);
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
