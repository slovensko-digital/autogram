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
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

import static com.google.common.io.Files.getFileExtension;
import static com.octosign.whitelabel.communication.MimeType.*;
import static com.octosign.whitelabel.signing.token.Token.*;
import static com.octosign.whitelabel.ui.I18n.*;
import static com.octosign.whitelabel.ui.utils.FXUtils.*;
import static com.octosign.whitelabel.ui.utils.Utils.*;
import static javafx.scene.control.ButtonBar.ButtonData.*;

public class MainController {
    public static final Set<String> ALLOWED_TYPES = new HashSet<>(List.of("pdf", "doc", "docx", "odt", "txt", "xml", "rtf", "png", "gif", "tif", "tiff", "bmp","jpg", "jpeg", "xml", "pdf", "xsd", "xls", "xlsx"));

    @FXML
    public Button aboutButton;

    @FXML
    public Button certSettingsButton;

    @FXML
    public Button nativeVisualizationButton;

    @FXML
    private WebView webView;

    @FXML
    private TextArea plaintextView;

    @FXML
    private Node nativeViewPrompt;

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

    private volatile File savedDocument;

    private boolean documentWasVisualized;

    /**
     * Consumer of the signed document content on success
     */
    private Consumer<byte[]> onSigned;

    public void initialize() {
        mainButton.setText(getProperMainButtonText());
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
        mainButton.setText(getProperMainButtonText());

        var document = signatureUnit.getDocument();
        var params = signatureUnit.getSignatureParameters();
        var mimeType = signatureUnit.getMimeType();

        if (mimeType.is(XML))
            ((XMLDocument) document).validate(params.getSchema());

        if (isVisualizationSupported(document, mimeType)) {
            displayVisualization(document, params, mimeType);
        } else {
            exitOnUnsupportedFormat();
        }

        Platform.runLater(() -> bringToForeground(getCurrentStage(mainButton)));
    }

    private boolean isVisualizationSupported(Document document, MimeType mimeType) {
        String filename = document.getFilename();
        var extension = isPresent(filename) ? getFileExtension(filename) : mimeType.subType();

        return ALLOWED_TYPES.contains(extension);
    }

    private void displayVisualization(Document document, SignatureParameters params, MimeType mimeType) {
        if (mimeType.is(XML)) {
            displayXMLVisualization((XMLDocument) document, params);

        } else if (mimeType.is(PDF)) {
            displayPDFVisualisation(document);

        } else {
            displayBinaryFileVisualisation();
        }
    }

    private void displayXMLVisualization(XMLDocument document, SignatureParameters params) {
        show(webView);

        MimeType transformationOutputMimeType = params.getTransformationOutputMimeType();
        String transformationOutput = document.getTransformed(params.getTransformation());

        if (transformationOutputMimeType.is(PLAIN)) {
            displayPlainTextVisualisation(transformationOutput);

        } else {
            displayHTMLVisualisation(transformationOutput);
        }
    }

    private void exitOnUnsupportedFormat() {
        String prettyTypes = String.join(", ", ALLOWED_TYPES);

        throw new IntegrationException(Code.UNSUPPORTED_FORMAT, translate("error.typeNotSupported.description", prettyTypes));
    }

    private void displayPlainTextVisualisation(String visualisation) {
        show(plaintextView);
        hide(webView);
        plaintextView.setText(visualisation);
    }

    private void displayBinaryFileVisualisation() {
        show(nativeViewPrompt);
        hide(webView);
        saveToFile(signatureUnit.getDocument());
    }

    private void displayHTMLVisualisation(String visualisation) {
        show(webView);

        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", visualisation);
            }
        });
        engine.load(Main.class.getResource("visualization.html").toExternalForm());
    }

    private void displayPDFVisualisation(Document document) {
        show(webView);

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
        disableMainButton(translate("btn.loading"));

        try {
            var driver = displaySelectDialogIfMany(getAvailableDrivers());
            var certificates = Token.fromDriver(driver).getCertificates();
            var signingCertificate = displaySelectDialogIfMany(certificates);
            signingManager.setActiveCertificate(signingCertificate);
        } catch (UserException e) {
            displayError(e);
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
        boolean smoothProcess = true;

        if (signatureUnit.isBinary()) {
            if (not(documentWasVisualized)) {
                smoothProcess = false;
                skipVisualizationDialog().showAndWait().ifPresent(response -> {
                    if (response.getButtonData() == NO) {
                        requireVisualization();
                    }
                });
            }

            boolean documentWasModified = fileExists(savedDocument) && checkDocumentModifications();

                if (documentWasModified) {
                    syncAllWithLatest();
                    displayWarning("warn.contentModified.header", "warn.contentModified.description");
                } else if (not(smoothProcess) && documentWasVisualized) {
                    displayInfo("info.previewSuccess.header", "info.previewSuccess.description");
                }
        }

        try {
            var signedContent = signingManager.sign(signatureUnit);
            onSigned.accept(signedContent);

        } catch (UserException e) {
            displayError(e);
        }

        enableMainButton(getProperMainButtonText());
        clearDocumentCache();
    }

    private void requireVisualization() {
        FutureTask<Boolean> task = new FutureTask<Boolean>(() -> {
        if (performRuntimeVisualization())
            return true;

        if (performDesktopVisualization())
            return true;

        if (not(documentWasVisualized)) {
            exitOnUnsupportedFormat();
        }
        return false;
        });

        task.run();
        try {
            task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private Alert skipVisualizationDialog() {
        Alert alert = buildWarning("warn.skipVisualization.header", "warn.skipVisualization.description");
        ButtonType yesType = new ButtonType(translate("btn.skipVisualization.yes"), ButtonBar.ButtonData.YES);
        ButtonType noType = new ButtonType(translate("btn.skipVisualization.no"), ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesType, noType);

        Button noButton = (Button) alert.getDialogPane().lookupButton(noType);
//        alert.setOnCloseRequest(Event::consume);
        return alert;
    }

    private boolean performRuntimeVisualization() {
        saveToFile(signatureUnit.getDocument());
        boolean wasSuccessful = false;

        for (var command: getPlatformSpecificCommands()) {
            var process = executeNative(command);

            if (process != null) {
                try {
                    wasSuccessful = process.onExit().thenApply(p -> p.exitValue() == 0).get();
                } catch (InterruptedException | ExecutionException ignored) {}

            } else {
                continue;
            }

            if (wasSuccessful) break;
        }

        return wasSuccessful;
    }

    private String[][] getPlatformSpecificCommands() {
        String path = savedDocument.getAbsolutePath();

        return switch (OperatingSystem.current()) {
            case WINDOWS -> new String[][] {
                            {"cmd.exe", "start", path},
                            {"cmd.exe", "/c", path},
                            {"cmd.exe", "\"" + path + "\""},
                            {"rundll32", "url.dll,FileProtocolHandler" + path},
                            {"rundll32", "SHELL32.DLL,ShellExec_RunDLL \"" + path + "\""},
                    };
            case MAC -> new String[][] {{"/usr/bin/open", path}};
            case LINUX -> new String[][] {{"xdg-open", path}};
        };
    }

    private Process executeNative(String... commands) {
        try {
            return new ProcessBuilder(commands).inheritIO().start();
        } catch (IOException e) {
            // continue
        }

        try {
            return Runtime.getRuntime().exec(commands);
        } catch (IOException ex) {
            // continue
        }

        return null;
    }


    private boolean performDesktopVisualization() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(savedDocument);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public void saveToFile(Document document) {
        try {
            if (not(fileExists(savedDocument))) {
                var prefix = Files.getNameWithoutExtension(document.getFilename());
                var suffix = "." + Files.getFileExtension(document.getFilename());

                savedDocument = File.createTempFile(prefix, suffix);
//                cachedFile.setExecutable(true, false);
                Files.write(document.getContent(), savedDocument);
            }
            savedDocument.deleteOnExit();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkDocumentModifications() {
        byte[] originalContent = signatureUnit.getDocument().getContent();
        byte[] contentFromDisk = readBytes(savedDocument);

        return not(Arrays.equals(contentFromDisk, originalContent));
    }

    private void syncAllWithLatest() {
        // consider making classes records (immutable) to prevent such sneaky assignments
        byte[] modifiedContent = readBytes(savedDocument);
        signatureUnit.getDocument().setContent(modifiedContent);
    }

    private void clearDocumentCache() {
        if (fileExists(savedDocument)) {
            if (not(savedDocument.delete())) {
                displayWarning("warn.fileNotDeleted.header", "warn.fileNotDeleted.description");
            }
        }
        savedDocument = null;
        documentWasVisualized = false;
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
    private void onNativeVisualizationButtonAction() {
        requireVisualization();
        documentWasVisualized = true;
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
