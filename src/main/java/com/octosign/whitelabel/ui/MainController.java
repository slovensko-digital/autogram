package com.octosign.whitelabel.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.octosign.whitelabel.communication.Document;
import com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;

/**
 * Controller for the signing window
 */
public class MainController {

    @FXML
    private Label certificateLabel;

    @FXML
    private Button certificateButton;

    @FXML
    private Label documentLabel;

    @FXML
    private WebView webView;

    @FXML
    private Label signLabel;

    @FXML
    private Button signButton;

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
     * Consumer of the signed document on success
     */
    private Consumer<Document> onSigned;

    public void initialize() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
    }

    public void setCertificateManager(CertificateManager certificateManager) {
        this.certificateManager = certificateManager;
        String certificateName = certificateManager.getCertificate().getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);

        certificateLabel.setText(String.format(certificateLabel.getText(), certificateName));
    }

    public void setDocument(Document document) {
        String transformedContent;
        try {
            transformedContent = document.getTransformed();
        } catch (Exception e) {
            Main.displayAlert(
                AlertType.ERROR,
                "Chyba zobrazenia",
                "Získanie zobraziteľnej podoby zlyhalo",
                "Pri zostavovaní zobraziteľnej podoby došlo k chybe a načítavaný súbor nemôže byť zobrazený. Detail chyby: " + e
            );
            return;
        }

        this.document = document;

        documentLabel.setText(String.format(documentLabel.getText(), document.getTitle()));

        var webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(
            (ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    webEngine.getDocument().getElementById("frame").setAttribute("srcdoc", transformedContent);
                }
            } 
        );
        webEngine.loadContent(this.getResourceAsString("visualization.html"));
    }

    public void setOnSigned(Consumer<Document> onSigned) {
        this.onSigned = onSigned;
    }

    @FXML
    private void pickCertificate() {
        System.out.println("Picking certificate");
        // TODO: Implement
    }

    @FXML
    private void sign() {
        try {
            String signedContent = certificateManager.getCertificate().sign(document.getContent());
            Document signedDocument = document.clone();
            signedDocument.setContent(signedContent);
            onSigned.accept(signedDocument);
        } catch (Exception e) {
            Main.displayAlert(
                AlertType.ERROR,
                "Nepodpísané",
                "Súbor nebol podpísaný",
                "Podpísanie zlyhalo alebo bolo zrušené. Detail chyby: " + e
            );
            throw e;
        }

        Main.displayAlert(
            AlertType.CONFIRMATION,
            "Podpísané",
            "Súbor bol podpísaný",
            "Súbor bol úspešne podpísaný. Môžete sa vrátiť."
        );
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
