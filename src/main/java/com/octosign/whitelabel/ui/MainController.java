package com.octosign.whitelabel.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PdfDocument;
import com.octosign.whitelabel.communication.document.XmlDocument;
import com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity;
import com.octosign.whitelabel.ui.about.AboutDialog;

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
     * Document signed in this window
     */
    private Document document;

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

    public void setDocument(Document document) {
        this.document = document;

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

        if (document instanceof XmlDocument && ((XmlDocument) document).getTransformation() != null) {
            webView.setManaged(true);
            textArea.setManaged(false);
            textArea.setVisible(false);

            CompletableFuture.runAsync(() -> {
                String visualization;
                try {
                    var xmlDocument = (XmlDocument) document;
                    visualization = xmlDocument.getTransformed();
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
                    var engine = webView.getEngine();
                    engine.getLoadWorker().stateProperty().addListener(
                        (observable, oldState, newState) -> {
                            if (newState == Worker.State.SUCCEEDED) {
                                engine.getDocument().getElementById("frame").setAttribute("srcdoc", visualization);
                            }
                        }
                    );
                    engine.load(Main.class.getResource("visualization.html").toExternalForm());
                });
            });
        } else if (document instanceof PdfDocument) {

            // TODO move pdf handler here

        } else {
// TODO keep
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

                // TODO
                //engine.setUserStyleSheetLocation(Main.class.getResource("pdfjs/web/viewer.css").toExternalForm());
                engine.setUserStyleSheetLocation(Main.class.getResource("pdf.viewer.css").toExternalForm());

                engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        engine.executeScript("openFileFromBase64('" + pdf_data + "')");
                    }
                });

                // TODO
                //engine.load(Main.class.getResource("pdfjs/web/viewer.html").toExternalForm());
                engine.load(Main.class.getResource("pdf.viewer.html").toExternalForm());
            });

        }
    }

    // TODO rm
    static final String pdf_data;
    static {
        try {
            pdf_data = Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of("tmp/MED_DeliveryReportAuthorization_Definition.pdf")));
        } catch (IOException e) {
            throw new AssertionError();
        }
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
                    String signedContent = certificateManager.getCertificate().sign(document.getContent());
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

}
