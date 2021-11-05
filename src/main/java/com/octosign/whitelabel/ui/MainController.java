package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity;
import com.octosign.whitelabel.ui.about.AboutDialog;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.octosign.whitelabel.ui.Main.getProperty;

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
        var parameters = signatureUnit.getSignatureParameters();

        if (document.getTitle() != null && !document.getTitle().isBlank()) {
            documentLabel.setText(String.format(getProperty("text.document"), document.getTitle()));
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
            mainButton.setText(String.format(getProperty("text.sign"), name));
        }

        //TODO consider simplifying this part to avoid tedious casting
        boolean isXml = document instanceof XMLDocument;
        boolean isPDF = document instanceof PDFDocument;
        final boolean hasSchema = isXml && ((XMLDocument) document).getSchema() != null;
        final boolean hasTransformation = isXml && ((XMLDocument) document).getTransformation() != null;

        if (hasSchema) {
            try {
                ((XMLDocument) document).validate();
            } catch (Exception e) {
                displayAlert(
                        "Neplatný formát",
                        "XML súbor nie je validný",
                        "Dokument na podpísanie nevyhovel požiadavkám validácie podľa XSD schémy. Detail chyby: " + e
                );
                return;
            }
        }

        if (hasTransformation) {
            String visualisation;
            try {
                var xmlDocument = (XMLDocument) document;
                visualisation = xmlDocument.getTransformed();
            } catch (Exception e) {
                displayAlert(
                        "Chyba zobrazenia",
                        "Získanie zobraziteľnej podoby zlyhalo",
                        "Pri zostavovaní zobraziteľnej podoby došlo k chybe a načítavaný súbor nemôže byť zobrazený. Detail chyby: " + e
                );
                return;
            }

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
        webEngine.getLoadWorker().stateProperty().addListener(
            (ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    webEngine.getDocument().getElementById("frame").setAttribute("srcdoc", visualisation);
                }
            }
        );
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

    private void displayAlert(String title, String header, String description) {
        Platform.runLater(() -> {
            Main.displayAlert(
                    AlertType.ERROR,
                    title,
                    header,
                    description
            );
        });
    }

    public void setOnSigned(Consumer<String> onSigned) {
        this.onSigned = onSigned;
    }

    @FXML
    private void onMainButtonAction() {
        if (certificateManager.getCertificate() == null) {
            // No certificate means this is loading of certificates
            mainButton.setDisable(true);
            mainButton.setText(getProperty("text.loading"));

            CompletableFuture.runAsync(() -> {
                String mainButtonText;
                if (certificateManager.useDefault() != null) {
                    String name = certificateManager
                        .getCertificate()
                        .getNicePrivateKeyDescription(KeyDescriptionVerbosity.NAME);
                    mainButtonText = String.format(getProperty("text.sign"), name);
                } else {
                    mainButtonText = getProperty("text.loadSigners");
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
            mainButton.setText(getProperty("text.signing"));

            CompletableFuture.runAsync(() -> {
                try {
                    String signedContent = certificateManager.getCertificate().sign(signatureUnit);
                    Platform.runLater(() -> onSigned.accept(signedContent));
                } catch (Exception e) {
                    displayAlert(
                            "Nepodpísané",
                            "Súbor nebol podpísaný",
                            "Podpísanie zlyhalo alebo bolo zrušené. Detail chyby: " + e
                    );
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
            if (inputStream == null) throw new Exception(getProperty("exc.resourceNotFound"));
            try (
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader)
            ) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new RuntimeException(getProperty("exc.resourceLoadingFailed", resourceName), e);
        }
    }
}
