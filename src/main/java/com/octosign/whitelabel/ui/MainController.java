package com.octosign.whitelabel.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.XmlDocument;
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
import javafx.concurrent.Worker;

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

        if (document.getTitle() != null && !document.getTitle().equals("")) {
            documentLabel.setText(String.format(Main.getProperty("text.document"), document.getTitle()));
        } else {
            documentLabel.setManaged(false);
        }

        if (document.getLegalEffect() != null && !document.getLegalEffect().equals("")) {
            signLabel.setText(document.getLegalEffect());
            signLabel.setVisible(true);
        }

        if (certificateManager.getCertificate() != null) {
            String name = certificateManager
                .getCertificate()
                .getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);
            mainButton.setText(String.format(Main.getProperty("text.sign"), name));
        }

        boolean isXml = document instanceof XmlDocument;
        final boolean hasTransformation = isXml && ((XmlDocument) document).getTransformation() != null;

        if (hasTransformation) {
            textArea.setManaged(false);

            CompletableFuture.runAsync(() -> {
                String visualisation;
                try {
                    var xmlDocument = (XmlDocument) document;
                    visualisation = xmlDocument.getTransformed();
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
                        (ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) -> {
                            if (newState == Worker.State.SUCCEEDED) {
                                webEngine.getDocument().getElementById("frame").setAttribute("srcdoc", visualisation);
                            }
                        } 
                    );
                    webEngine.loadContent(getResourceAsString("visualization.html"));
                });
            });
        } else {
            webView.setManaged(false);

            textArea.setText(document.getContent());
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
