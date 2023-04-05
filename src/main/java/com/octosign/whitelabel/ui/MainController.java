package com.octosign.whitelabel.ui;

import com.google.common.io.Files;
import com.octosign.whitelabel.communication.*;
import com.octosign.whitelabel.communication.document.*;
import com.octosign.whitelabel.error_handling.*;
import com.octosign.whitelabel.signing.*;
import com.octosign.whitelabel.signing.token.*;
import com.octosign.whitelabel.ui.about.*;

import com.octosign.whitelabel.ui.picker.*;
import com.octosign.whitelabel.ui.utils.FXUtils;
import digital.slovensko.autogram.util.OperatingSystem;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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

    @FXML
    private Button mainButton;

    private SigningManager signingManager;

    private SignatureUnit signatureUnit;

    private volatile File savedDocument;

    private boolean documentAlreadyVisualized = false;

    // Consumer of the signed document content on success
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

        requireVisualizationToContinue();

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
            documentAlreadyVisualized = true;

        } else if (mimeType.is(PDF)) {
            displayPDFVisualisation(document);
            documentAlreadyVisualized = true;

        } else {
            saveToFile(document);
            displayBinaryFileVisualisationPrompt();
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

    private void displayBinaryFileVisualisationPrompt() {
        show(nativeViewPrompt);
        hide(webView);
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
            List<Driver> allDrivers = getAvailableDrivers();
            if (isNullOrEmpty(allDrivers)) {
                throw new UserException("error.noDriverInstalled.header", "error.noDriverInstalled.description");
            }
          
            var driver = selectDialog(allDrivers);      
            var allTokenCertificates = Token.fromDriver(driver).getCertificates();
            if (isNullOrEmpty(allTokenCertificates)) {
                throw new UserException("error.tokenEmpty.header", "error.tokenEmpty.description");
            }
          
            var certificate = selectDialog(allTokenCertificates);
          
            signingManager.setActiveCertificate(certificate);

        } catch (UserException e) {
            signingManager.setActiveCertificate(null);
            displayError(e);
        }

        enableMainButton(getProperMainButtonText());
    }

    private void requireVisualizationToContinue() {
        if (!documentAlreadyVisualized) {
            disableMainButton(translate("btn.visualizationRequired"));
        } else {
            enableMainButton(getProperMainButtonText());
        }
    }


    private <T extends SelectableItem> T selectDialog(List<T> items) {
        if (isNullOrEmpty(items))
            return null;

        if (items.size() == 1) {
            return first(items);

        } else {
            var dialog = new SelectDialog<>(items, getStage());

            return dialog.getResult();
        }
    }

    private Stage getStage() {
        return FXUtils.getCurrentStage(mainButton);
    }

    private void signDocument() {
        disableMainButton(translate("btn.signing"));

        if (signatureUnit.isBinary() && wasDocumentFileModified()) {
            updateInMemoryDocumentFromModifiedFile();
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

    private boolean wasDocumentFileModified() {
        if (!fileExists(savedDocument))
            return false;

        byte[] inMemoryContent = signatureUnit.getDocument().getContent();
        byte[] contentFromDisk = readBytes(savedDocument);

        return !(Arrays.equals(contentFromDisk, inMemoryContent));
    }

    private void updateInMemoryDocumentFromModifiedFile() {
        // consider making classes records (immutable) to prevent such sneaky assignments
        byte[] modifiedContent = readBytes(savedDocument);
        signatureUnit.getDocument().setContent(modifiedContent);
    }

    private void visualizeOrTerminate() {
        if (performRuntimeVisualization() || performDesktopVisualization()) {
            documentAlreadyVisualized = true;
            return;
        }

        if (!(documentAlreadyVisualized)) {
            exitOnUnsupportedFormat();
        }
    }

    private boolean performRuntimeVisualization() {
        saveToFile(signatureUnit.getDocument());
        boolean success = false;

        for (var command: getPlatformSpecificCommands()) {
            var process = executeNative(command);

            if (process != null) {
                try {
                    success = process.onExit().thenApply(p -> p.exitValue() == 0).get();
                } catch (InterruptedException | ExecutionException ignored) {}

            } else {
                continue;
            }

            if (success) break;
        }

        return success;
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
        } catch (IOException ignored) {
            // continue
        }

        try {
            return Runtime.getRuntime().exec(commands);
        } catch (IOException ignored) {
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
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public void saveToFile(Document document) {
        try {
            if (!(fileExists(savedDocument))) {
                var prefix = Files.getNameWithoutExtension(document.getFilename());
                var suffix = "." + Files.getFileExtension(document.getFilename());

                savedDocument = File.createTempFile(prefix, suffix);
                savedDocument.setExecutable(true, false);
                Files.write(document.getContent(), savedDocument);
            }
            savedDocument.deleteOnExit();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearDocumentCache() {
        if (fileExists(savedDocument)) {
            if (!(savedDocument.delete())) {
                displayWarning("warn.fileNotDeleted.header", "warn.fileNotDeleted.description");
            }
        }
        savedDocument = null;
        documentAlreadyVisualized = false;
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
        visualizeOrTerminate();
        requireVisualizationToContinue();
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
