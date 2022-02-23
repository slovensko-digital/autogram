package com.octosign.whitelabel.ui;

import com.google.common.io.Files;
import com.octosign.whitelabel.communication.*;
import com.octosign.whitelabel.communication.document.*;
import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.*;
import com.octosign.whitelabel.signing.token.*;
import com.octosign.whitelabel.ui.about.*;

import com.octosign.whitelabel.ui.picker.*;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.*;

import static com.google.common.io.Files.*;
import static com.google.common.io.Files.getFileExtension;
import static com.octosign.whitelabel.communication.MimeType.*;
import static com.octosign.whitelabel.signing.token.Token.*;
import static com.octosign.whitelabel.ui.ConfigurationProperties.*;
import static com.octosign.whitelabel.ui.I18n.*;
import static com.octosign.whitelabel.ui.utils.FXUtils.*;
import static com.octosign.whitelabel.ui.utils.Utils.*;


/**
 * Controller for the signing window
 */
public class MainController {

    @FXML
    public Button aboutButton;

    @FXML
    public Button certSettingsButton;

    @FXML
    public Button showNativeVisualizationButton;

    @FXML
    private WebView webView;

    @FXML
    private TextArea textArea;

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
        var document = signatureUnit.getDocument();
        var params = signatureUnit.getSignatureParameters();
        var mimeType = signatureUnit.getMimeType();

        if (mimeType.is(XML))
            ((XMLDocument) document).validate(params.getSchema());

        if (isVisualizationSupported(document, mimeType)) {
            displayVisualization(document, params, mimeType);
        } else {
            displayNotSupportedType(document);
        }
    }

    private boolean isVisualizationSupported(Document document, MimeType mimeType) {
        String filename = document.getFilename();

        var extension = isPresent(filename) ? getFileExtension(filename) : mimeType.subType();

        return ALLOWED_TYPES.contains(extension);
    }

    private void displayVisualization(Document document, SignatureParameters params, MimeType mimeType) {
        if (mimeType.is(XML)) {
            displayXMLVisualization((XMLDocument)document, params);

        } else if (mimeType.is(PDF)) {
            displayPDFVisualisation(document);

        } else {
            displayBinaryFileVisualisation(document);
        }
    }

    private void displayXMLVisualization(XMLDocument document, SignatureParameters params) {
        String transformation = params.getTransformation();
        MimeType transformationOutputMimeType = params.getTransformationOutputMimeType();

        String transformationOutput = document.getTransformed(transformation);

        if (transformationOutputMimeType.is(PLAIN))
            displayPlainTextVisualisation(transformationOutput);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
        else
            displayHTMLVisualisation(transformationOutput);

    }

    private void displayNotSupportedType(Document document) {
        hide(webView);
        mainButton.setDisable(true);
        certSettingsButton.setDisable(true);
        textArea.setText(translate("text.visualizationNotSupported", document.getFilename()));
    }

    private void displayPlainTextVisualisation(String visualisation) {
        hide(webView);
        textArea.setText(visualisation);
    }

    private void displayBinaryFileVisualisation(Document document) {
        hide(webView);
        show(showNativeVisualizationButton);

        textArea.setText(translate("text.openBinaryFile"));
        showNativeVisualizationButton.setText(translate("btn.openFile", document.getFilename()));
    }

    private void displayHTMLVisualisation(String visualisation) {
        hide(textArea);

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
        hide(textArea);

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
        hide(showNativeVisualizationButton);
        disableMainButton(translate("btn.loading"));

        try {
            var driver = displaySelectDialogIfMany(getAvailableDrivers());

            var certificates = Token.fromDriver(driver).getCertificates();
            var signingCertificate = displaySelectDialogIfMany(certificates);

            signingManager.setActiveCertificate(signingCertificate);
        } catch (UserException e) {
            displayError(e);
        } finally {
            enableMainButton(getProperMainButtonText());
        }
        enableMainButton(getProperMainButtonText());
    }

    private <T extends SelectableItem> T displaySelectDialogIfMany(List<T> items) {
        if (isNullOrEmpty(items))
            throw new RuntimeException("Collection is null or empty!");

        if (items.size() == 1) {
            return first(items);
        } else {
            var selectDialog = new SelectDialog<>(items, getCurrentStage(mainButton));

            return selectDialog.getResult();
        }
    }

    private void signDocument() {
        disableMainButton(translate("btn.signing"));

        if (fileExists(cachedFile))
            detectFileModifications();

        try {
            var signedContent = signingManager.sign(signatureUnit);
            onSigned.accept(signedContent);
            clearCache();

        } catch (UserException e) {
            displayError(e);
        }

        enableMainButton(getProperMainButtonText());
    }

    private void detectFileModifications() {
        byte[] originalContent = signatureUnit.getDocument().getContent();
        byte[] contentFromDisk = readBytes(cachedFile);

        // consider making classes records (immutable) to prevent such sneaky assignments
        if (not(Arrays.equals(contentFromDisk, originalContent))) {
            signatureUnit.getDocument().setContent(contentFromDisk);
            displayWarning("warn.dataToSignChanged.header", "warn.dataToSignChanged.description");
        }
    }

    private void disableMainButton(String text) {
        mainButton.setText(text);
        mainButton.setDisable(true);
    }

    private void enableMainButton(String text) {
        mainButton.setText(text);
        mainButton.setDisable(false);
    }

    public String getProperMainButtonText() {
        if (isSignerReady()) {
            return translate("btn.signAs", signingManager.getActiveCertificate().getDisplayedName());
        } else {
            return translate("btn.loadSigners");
        }
    }

    private void show(Node node) {
        if (!node.isManaged())
            node.setManaged(true);
        if (!node.isVisible())
            node.setVisible(true);
    }

    private void hide(Node node) {
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

    @FXML
    private void onShowNativeVisualizationButtonAction() {
        cacheAsFile(signatureUnit.getDocument());

        new Thread(() -> {
            try {
                Desktop.getDesktop().open(cachedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static final Set<String> ALLOWED_TYPES = new HashSet<>(List.of("pdf", "doc", "docx", "odt", "txt", "xml", "rtf", "png", "gif", "tif", "tiff", "bmp", "jpg", "jpeg", "xml", "pdf", "xsd", "xls"));

    private Path DOCUMENT_CACHE_DIR = Path.of(System.getProperty("java.io.tmpdir"), getProperty("app.shortName"), "documents");
    private File cachedFile;

    public void cacheAsFile(Document document) {
        DOCUMENT_CACHE_DIR.toFile().mkdirs();

        try {
            if (not(fileExists(cachedFile))) {
                var prefix = Files.getNameWithoutExtension(document.getFilename());
                var suffix = "." + Files.getFileExtension(document.getFilename());

                cachedFile = File.createTempFile(prefix, suffix, DOCUMENT_CACHE_DIR.toFile());
                Files.write(document.getContent(), cachedFile);
            }
            cachedFile.deleteOnExit();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearCache() {
        if (fileExists(cachedFile)) {
            if (not(cachedFile.delete())) {
                displayWarning("warn.fileNotDeleted.header", "warn.fileNotDeleted.description");
            }

            cachedFile = null;
        }
    }
}
