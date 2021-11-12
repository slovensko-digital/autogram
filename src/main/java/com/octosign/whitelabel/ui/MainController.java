package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.ui.about.AboutDialog;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity.*;
import static com.octosign.whitelabel.ui.FXUtils.displayError;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;

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
     * Bottom-right button used to load/pick certificate
     */
    @FXML
    private Button loadSignersButton;

    /**
     * Bottom-right button used to sign
     */
    @FXML
    private Button signDocumentButton;

    @FXML
    private ResourceBundle resources;

    /**
     * Signing certificate manager
     */
    private CertificateManager certificateManager;

    /**
     * Wrapper for document in this window (to be signed) and parameters
     */
    private SignatureUnit signatureUnit;

    /**
     * Consumer of the signed document content on success
     */
    private Consumer<String> onSigned;

    /**
     * Name of the currently used signing certificate owner
     */
    private String certificateName;

    public void initialize() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
    }

    public void setCertificateManager(CertificateManager certificateManager) {
        this.certificateManager = certificateManager;
    }

    public void setOnSigned(Consumer<String> onSigned) { this.onSigned = onSigned; }

    public void setSignatureUnit(SignatureUnit signatureUnit) { this.signatureUnit = signatureUnit; }

    public void loadDocument() {
        var document = signatureUnit.getDocument();
        var parameters = signatureUnit.getSignatureParameters();

        if (document.getLegalEffect() != null && !document.getLegalEffect().isBlank()) {
            signLabel.setText(document.getLegalEffect());
            signLabel.setVisible(true);
        }

        determineButtonAction();

        boolean isXML = document instanceof XMLDocument;
        boolean isPDF = document instanceof PDFDocument;
        final boolean hasSchema = isXML && ((XMLDocument) document).getSchema() != null;
        final boolean hasTransformation = isXML && ((XMLDocument) document).getTransformation() != null;

        if (hasSchema) {
            ((XMLDocument) document).validate();
        }

        if (hasTransformation) {
            var visualisation = ((XMLDocument) document).getTransformed();

            var transformationOutputType = parameters.getTransformationOutputMimeType() == null
                ? MimeType.HTML
                : MimeType.parse(parameters.getTransformationOutputMimeType());

            if (transformationOutputType.equalsTypeSubtype(MimeType.HTML)) {
                displayHTMLVisualisation(visualisation);
            } else {
                displayPlainTextVisualisation(visualisation);
            }
        } else if(isPDF) {
            displayPDFVisualisation(document);
        } else {
            displayPlainTextVisualisation(document.getContent());
        }
    }

    private void displayPlainTextVisualisation(String visualisation) {
        webView.setManaged(false);
        textArea.setText(visualisation);
    }

    private void displayHTMLVisualisation(String visualisation) {
        textArea.setManaged(false);

        var webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.getDocument().getElementById("frame").setAttribute("srcdoc", visualisation);
            }
        });
        webEngine.loadContent(getResourceAsString("visualization.html"));
    }

    private void displayPDFVisualisation(Document document) {
        textArea.setManaged(false);
        textArea.setVisible(false);

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
    private void onLoadSignersButtonAction() {
        startLoading();

        Platform.runLater(() -> {
            try {
                certificateManager.useDefault();
            } catch (UserException e) {
                displayError(e);
            } finally {
                finishLoading();
                determineButtonAction();
            }
        });
    }

    @FXML
    private void onSignDocumentButtonAction() {
        startSigning();

        Platform.runLater(() -> {
            try {
                var signedContent = certificateManager.getCertificate().sign(signatureUnit);
                onSigned.accept(signedContent);
            } catch (UserException e) {
                displayError(e);
            } finally {
                finishSigning();
                determineButtonAction();
            }
        });
    }

    private void startLoading() {
        loadSignersButton.setDisable(true);
        loadSignersButton.setText(translate("btn.loading"));
    }

    private void finishLoading() {
        loadSignersButton.setDisable(false);
        loadSignersButton.setText(translate("btn.loadSigners"));
    }

    private void startSigning() {
//        if (!signDocumentButton.isDisabled()) {
            signDocumentButton.setDisable(true);
            signDocumentButton.setText(translate("btn.signing"));
//        }
    }

    private void finishSigning() {
//        if (signDocumentButton.isDisabled()) {
            signDocumentButton.setDisable(false);
            signDocumentButton.setText(translate("btn.signAs", getCachedName()));
//        }
    }

    private void determineButtonAction() {
        if (certificateManager.getCertificate() != null) {
            vanish(loadSignersButton);
            materialize(signDocumentButton);

            if (isNullOrBlank(signDocumentButton.getText())) {
                signDocumentButton.setText(translate("btn.signAs", getCachedName()));
            }
        } else {
            vanish(signDocumentButton);
            materialize(loadSignersButton);
        }
    }

    private void materialize(Node node) {
        if (!node.isManaged()) node.setManaged(true);
        if (!node.isVisible()) node.setVisible(true);
    }

    private void vanish(Node node) {
        if (node.isManaged()) node.setManaged(false);
        if (node.isVisible()) node.setVisible(false);
    }

    private String getCachedName() {
        if (isNullOrBlank(certificateName)) {
            certificateName = certificateManager.getCertificate().getNicePrivateKeyDescription(NAME);
        }

        return certificateName;
    }

    @FXML
    private void onAboutButtonAction() {
        new AboutDialog().show();
    }

    @FXML
    private void onCertSettingsButtonAction() {
        certificateManager.useDialogPicker();
    }

    /**
     * Get resource from the ui resources as string using name
     */
    private static String getResourceAsString(String resourceName) {
        try (InputStream inputStream = MainController.class.getResourceAsStream(resourceName)) {
            if (inputStream == null) throw new Exception("Resource not found");
            try (
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader);
            ) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource " + resourceName, e);
        }
    }
}
