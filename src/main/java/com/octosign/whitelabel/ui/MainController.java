package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity;
import com.octosign.whitelabel.ui.about.AboutDialog;
import javafx.application.Platform;
import javafx.concurrent.Worker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity;
import com.octosign.whitelabel.ui.about.AboutDialog;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebView;


/**
 * Controller for the signing window
 */
public class MainController {

    @FXML
    private Label documentLabel;

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
        var document = signatureUnit.getDocument();

        if (document.getTitle() != null && !document.getTitle().isBlank()) {
            documentLabel.setText(String.format(Main.getProperty("text.document"), document.getTitle()));
        } else {
            documentLabel.setManaged(false);
        }

        if (document.getLegalEffect() != null && !document.getLegalEffect().isBlank()) {
            signLabel.setText(document.getLegalEffect());
            signLabel.setVisible(true);
        }

        if (certificateManager.getCertificate() != null) {
            String name = certificateManager
                .getCertificate()
                .getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);
            mainButton.setText(String.format(Main.getProperty("text.sign"), name));
        }

        boolean isXML = document instanceof XMLDocument;
        boolean isPDF = document instanceof PDFDocument;
        final boolean hasSchema = false; //isXML && ((XMLDocument) document).getSchema() != null;
        final boolean hasTransformation = isXML && ((XMLDocument) document).getTransformation() != null;

        if (hasSchema) {
            try {
                ((XMLDocument) document).validate();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Main.displayAlert(
                        AlertType.ERROR,
                        "Neplatný formát",
                        "XML súbor nie je validný",
                        "Dokument na podpísanie nevyhovel požiadavkám validácie podľa XSD schémy. Detail chyby: " + e
                    );
                });
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
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Main.displayAlert(
                            AlertType.ERROR,
                            "Chyba zobrazenia",
                            "Získanie zobraziteľnej podoby zlyhalo",
                            "Pri zostavovaní zobraziteľnej podoby došlo k chybe a načítavaný súbor nemôže byť zobrazený. Detail chyby: " + e
                        );
                    });
                    return;
                }

                Platform.runLater(() -> {
                    var webEngine = webView.getEngine();
                    webEngine.getLoadWorker().stateProperty().addListener(
                        (observable, oldState, newState) -> {
                            if (newState == Worker.State.SUCCEEDED) {
                                webEngine.getDocument().getElementById("frame").setAttribute("srcdoc", visualization);
                            }
                        }
                    );
                    webEngine.load(Main.class.getResource("visualization.html").toExternalForm());
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
                engine.setUserStyleSheetLocation(Main.class.getResource("pdf.viewer.css").toExternalForm());

                engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        engine.executeScript("openFileFromBase64('" + document.getContent() + "')");
                    }
                });

                // TODO - ???
                //engine.load(Main.class.getResource("pdfjs/web/viewer.html").toExternalForm());
                engine.load(Main.class.getResource("pdf.viewer.html").toExternalForm());
            });
        }

        if (!isXML && !isPDF) throw new RuntimeException("Unsupported document format");

    }

    public void setOnSigned(Consumer<String> onSigned) {
        this.onSigned = onSigned;
    }

    @FXML
    private void onMainButtonAction() {
        if (certificateManager.getCertificate() == null) {
            // No certificate means this is loading of certificates
            mainButton.setDisable(true);
            mainButton.setText(Main.getProperty("text.loading"));

            CompletableFuture.runAsync(() -> {
                String mainButtonText;
                if (certificateManager.useDefault() != null) {
                    String name = certificateManager
                        .getCertificate()
                        .getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);
                    mainButtonText = String.format(Main.getProperty("text.sign"), name);
                } else {
                    mainButtonText = Main.getProperty("text.loadSigners");
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
            mainButton.setText(Main.getProperty("text.signing"));

            CompletableFuture.runAsync(() -> {
                try {
                    String signedContent = certificateManager.getCertificate().sign(signatureUnit);
                    Platform.runLater(() -> onSigned.accept(signedContent));
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Main.displayAlert(
                            AlertType.ERROR,
                            "Nepodpísané",
                            "Súbor nebol podpísaný",
                            "Podpísanie zlyhalo alebo bolo zrušené. Detail chyby: " + e
                        );
                    });
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

    /**
     * Get resource from the ui resources as string using name
     */
    private String getResourceAsString(String resourceName) {
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
