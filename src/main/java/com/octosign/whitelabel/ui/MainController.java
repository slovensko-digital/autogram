package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.communication.MimeType;
import com.octosign.whitelabel.communication.SignatureUnit;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.MalformedMimetypeException;
import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.ui.about.AboutDialog;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity.NAME;
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

    public void setOnSigned(Consumer<String> onSigned) {this.onSigned = onSigned;}

    public void setSignatureUnit(SignatureUnit signatureUnit) {this.signatureUnit = signatureUnit;}

    public void loadDocument() {
        var document = signatureUnit.getDocument();
        var parameters = signatureUnit.getSignatureParameters();

        if (document.getLegalEffect() != null && !document.getLegalEffect().isBlank()) {
            signLabel.setText(document.getLegalEffect());
            signLabel.setVisible(true);
        }

        resolveMainButtonText();

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
                transformationOutputType = parameters.getTransformationOutputMimeType() == null ?
                    MimeType.HTML :
                    MimeType.parse(parameters.getTransformationOutputMimeType());
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
        textArea.setText(visualisation);
    }

    private void displayHTMLVisualisation(String visualisation) {
        vanish(textArea);

        var webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.getDocument()
                    .getElementById("frame")
                    .setAttribute("srcdoc", visualisation);
            }
        });
        webEngine.loadContent(getResourceAsString("visualization.html"));
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
        Platform.runLater(() -> {
            if (!isSignerReady()) {
                loadSigners();
            } else {
                signDocument();
            }
        });
    }

    private boolean isSignerReady() {
        return certificateManager != null && certificateManager.getCertificate() != null;
    }

    private void loadSigners() {
        disableWithText(translate("btn.loading"));

        try {
            var certificate = CertificateManager.getDefaulCertificate();
            certificateManager.setCertificate(certificate);
        } catch (UserException e) {
            displayError(e);
        } finally {
            enableWithDefaultText();
        }
    }

    private void signDocument() {
        disableWithText(translate("btn.signing"));

            try {
                var signedContent = certificateManager.getCertificate().sign(signatureUnit);
                onSigned.accept(signedContent);
            } catch (UserException e) {
                displayError(e);
            } finally {
                enableWithDefaultText();
            }
    }

    private void displayTokenSelectDialog() {
        Stage stage = new Stage();
        PopupControl pp = new PopupControl();
        List<Button> buttons = new ArrayList<>();
        var params = CertificateManager.getSelectionParameters();

        for (var entry : params.entrySet()) {
            var button = new Button(entry.getKey());
            button.setOnAction(e -> {
                var certificates = entry.getValue().get();
                if (certificates.isEmpty())
                    throw new RuntimeException("no certs!!");
                var keys = certificates.stream()
                    .map(SigningCertificate::getAvailablePrivateKeys)
                    .map(list -> list.toArray(DSSPrivateKeyEntry[]::new))
                    .flatMap(Stream::of)
                    .toList();
                if (keys.isEmpty())
                    throw new RuntimeException("neni PKs!!!");
                certificateManager.setCertificate(certificates.get(0));
                if (keys.size() > 1) {
                    stage.setScene(getCertSelectScene(keys));
                }
                stage.close();
            });
            buttons.add(button);
        }
        pp.setWidth(280);
        pp.setHeight(120);
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(16));
        pane.setHgap(16);
        pane.setVgap(8);
        for (int i = 0; i < buttons.size(); i++) {
            pane.add(buttons.get(i), i, 0);
        }
        var scene = new Scene(pane, 280, 120);
        stage.setScene(scene);
        stage.show();
        pp.show(stage, 280, 120);
    }

    public Scene getCertSelectScene(List<DSSPrivateKeyEntry> keys) {
        GridPane pane = new GridPane();
        var scene = new Scene(pane, 420, 200);
        List<Button> buttons = new ArrayList<>();
        for (var key : keys) {
            var button = new Button(SigningCertificate.getNicePrivateKeyDescription(key,
                SigningCertificate.KeyDescriptionVerbosity.SHORT));
            button.setOnAction(event -> {
                ((Stage) scene.getWindow()).close();
            });
            buttons.add(button);
        }
        for (int i = 0; i < buttons.size(); i++) {
            pane.add(buttons.get(i), i, 0);
        }
        return scene;
    }

    private void disableWithText(String newText) {
        mainButton.setText(newText);
        mainButton.setDisable(true);
    }

    private void enableWithDefaultText() {
        resolveMainButtonText();
        mainButton.setDisable(false);
    }

    public void resolveMainButtonText() {
        if (isSignerReady()) {
            mainButton.setText(translate("btn.signAs", getCachedName()));
        } else {
            mainButton.setText(translate("btn.loadSigners"));
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

    private String getCachedName() {
        if (isNullOrBlank(certificateName)) {
            certificateName = certificateManager.getCertificate()
                .getNicePrivateKeyDescription(NAME);
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
            if (inputStream == null)
                throw new Exception("Resource not found");
            try (
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader);) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource " + resourceName, e);
        }
    }
}
