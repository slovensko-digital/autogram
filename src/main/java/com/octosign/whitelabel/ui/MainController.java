package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.error_handling.SignerException;
import com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity;
import com.octosign.whitelabel.ui.about.AboutDialog;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.octosign.whitelabel.ui.FX.displayError;
import static com.octosign.whitelabel.ui.Main.getProperty;
import static com.octosign.whitelabel.ui.Main.getResourceString;


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

    public void initialize() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
    }

    public void setCertificateManager(CertificateManager certificateManager) {
        this.certificateManager = certificateManager;
    }

    public void setSignatureUnit(SignatureUnit signatureUnit) {
        this.signatureUnit = signatureUnit;
    }

    public void loadDocument() {
        var document = signatureUnit.getDocument();

        if (document.getLegalEffect() != null && !document.getLegalEffect().isBlank()) {
            signLabel.setText(document.getLegalEffect());
            signLabel.setVisible(true);
        }

        if (certificateManager.getCertificate() != null) {
            String name = certificateManager
                .getCertificate()
                .getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);
            mainButton.setText(getProperty("btn.signAs", name));
        }

        boolean isXML = document instanceof XMLDocument;
        boolean isPDF = document instanceof PDFDocument;
        final boolean hasSchema = false; //isXML && ((XMLDocument) document).getSchema() != null;
        final boolean hasTransformation = isXML && ((XMLDocument) document).getTransformation() != null;

        if (hasSchema) {
            try { ((XMLDocument) document).validate(); }
            catch (Exception e) {
                Platform.runLater(() -> displayError("invalidFormat", e));
                return;
            }
        }

        if (hasTransformation) {
            webView.setManaged(true);
            textArea.setManaged(false);
            textArea.setVisible(false);

            CompletableFuture.runAsync(() -> {
                String visualization;
                try {
                    var xmlDocument = (XMLDocument) document;
                    visualization = "<pre>" + xmlDocument.getTransformed() + "</pre>";
                } catch (SignerException e) {
                    Platform.runLater(() -> displayError("visualizationError", e));
                    return;
                }

                Platform.runLater(() -> {
                    var webEngine = webView.getEngine();
                    webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                            if (newState == Worker.State.SUCCEEDED) {
                                webEngine.getDocument().getElementById("frame").setAttribute("srcdoc", visualization);
                            }
                        }
                    );
                    webEngine.load(getResourceString("visualization.html"));
                });
            });
        }

        if (isPDF) {
//            // TODO keep - ???
//            webView.setManaged(false);
//            textArea.setManaged(true);
//            textArea.setVisible(true);
//            textArea.setText(document.getContent());

            webView.setManaged(true);
            textArea.setManaged(false);
            textArea.setVisible(false);

            Platform.runLater(() -> {
                var engine = webView.getEngine();
                engine.setJavaScriptEnabled(true);

                // TODO - ???
                //engine.setUserStyleSheetLocation(Main.class.getResource("pdfjs/web/viewer.css").toExternalForm());
                engine.setUserStyleSheetLocation(getResourceString("pdf.viewer.css"));

                engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        engine.executeScript("openFileFromBase64('" + document.getContent() + "')");
                    }
                });

                engine.load(getResourceString("pdfjs/web/viewer.html"));
            });
        }

        if (!isXML && !isPDF) throw new RuntimeException("error.unsupportedDocumentFormat");

    }

    public void setOnSigned(Consumer<String> onSigned) {
        this.onSigned = onSigned;
    }

    @FXML
    private void onMainButtonAction() {
        if (certificateManager.getCertificate() == null) {
            // No certificate means this is loading of certificates
            mainButton.setDisable(true);
            mainButton.setText(getProperty("btn.loading"));

            CompletableFuture.runAsync(() -> {
                String mainButtonText;
                if (certificateManager.useDefault() != null) {
                    String name = certificateManager
                        .getCertificate()
                        .getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);
                    mainButtonText = getProperty("btn.signAs", name);
                } else {
                    mainButtonText = getProperty("btn.loadSigners");
                }
                Platform.runLater(() -> {
                    mainButton.setText(mainButtonText);
                    mainButton.setDisable(false);
                });
            });
        } else {
            // Otherwise this is signing
            String previousButtonText = mainButton.getText();
            mainButton.setDisable(true);
            mainButton.setText(getProperty("btn.signing"));

            CompletableFuture.runAsync(() -> {
                try {
                    String signedContent = certificateManager.getCertificate().sign(signatureUnit);
                    Platform.runLater(() -> onSigned.accept(signedContent));
                } catch (Exception e) {
                    Platform.runLater(() -> displayError("notSigned", e));
                } finally {
                    Platform.runLater(() -> {
                        mainButton.setText(previousButtonText);
                        mainButton.setDisable(false);
                    });
                }
            });
        }
    }

    @FXML
    private void onAboutButtonAction() {
        new AboutDialog().show();
    }

    @FXML
    private void onCertSettingsButtonAction() {
        certificateManager.useDialogPicker();
    }

}
