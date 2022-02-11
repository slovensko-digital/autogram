package com.octosign.whitelabel.ui;

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
import javafx.scene.image.*;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import static com.octosign.whitelabel.communication.MimeType.*;
import static com.octosign.whitelabel.signing.token.Token.*;
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
    public Button hiddenOpenFileButton;

    @FXML
    private WebView webView;

    @FXML
    private TextArea textArea;

    @FXML
    private ImageView imageView;

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
        mainButton.setText(resolveMainButtonText());

        var document = signatureUnit.getDocument();
        var params = signatureUnit.getSignatureParameters();
        var mimeType = signatureUnit.getMimeType();

        if (mimeType.is(XML)) {
            XMLDocument xmlDocument = (XMLDocument) document;
            xmlDocument.validate(params.getSchema());
            var visualisation = xmlDocument.getTransformed(params.getTransformation());

            MimeType transformationOutput = params.getTransformationOutputMimeType();
            if (transformationOutput.is(PLAIN))
                displayPlainTextVisualisation(visualisation);
            else
                displayHTMLVisualisation(visualisation);

        } else if (mimeType.is(PDF)) {
            displayPDFVisualisation(document);
        } else if (document.isPermitted()) {
            displayBinaryFileVisualisation(document);
        } else {
            displayForbiddenTypeVisualization(document, mimeType);
        }
    }

    public File saveToFilesystem(Document document) {
        Path location = Path.of(System.getProperty("java.io.tmpdir"), "Autogram", "documents").toAbsolutePath();
        location.toFile().mkdirs();

        int counter = 0;
        String filename = document.getFilename();
        File outputFile;

        while ((outputFile = buildFile(location, counter, filename)).exists())
            counter++;

        FileUtil.touch(outputFile);

        try (OutputStream stream = new FileOutputStream(outputFile)) {
            stream.write(document.getContent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to save file!");
        }

        return outputFile;
    }

    private File buildFile(Path location, int i, String filename) {
        int dot = filename.lastIndexOf(".");
        String basename = filename.substring(0, dot);
        String extension = filename.substring(dot + 1);

        if (i == 0)
            filename = String.format("%s.%s", basename, extension);
        else
            filename = String.format("%s_%d.%s", basename, i, extension);

        return Paths.get(location.toString(), filename).toFile();
    }

    private void displayForbiddenTypeVisualization(Document document, MimeType mimeType) {
        vanish(webView);
        mainButton.setDisable(true);
        certSettingsButton.setDisable(true);
        textArea.setText(translate("text.visualizationNotSupported",mimeType.subType()));
    }

    private void displayPlainTextVisualisation(String visualisation) {
        vanish(webView);
        textArea.setText(visualisation);
    }

    private void displayBinaryFileVisualisation(Document document) {
        vanish(webView);
        materialize(hiddenOpenFileButton);

        textArea.setText(translate("text.openBinaryFile"));
        File documentFile = saveToFilesystem(document);

        hiddenOpenFileButton.setUserData(documentFile.getAbsolutePath());
        hiddenOpenFileButton.setText(translate("btn.openFile", documentFile.getName()));
    }

    private void displayHTMLVisualisation(String visualisation) {
        vanish(textArea);

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
        vanish(textArea);

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
        vanish(hiddenOpenFileButton);
        disableAndSet(translate("btn.loading"));

        try {
            var driver = getDriverOrSelectIfMany(getAvailableDrivers());
            var certificates = Token.fromDriver(driver).getCertificates();

            var signingCertificate = getCertificateOrSelectIfMany(certificates);
            signingManager.setActiveCertificate(signingCertificate);

        } catch (UserException e) {
            displayError(e);
        } finally {
            enableAndSet(resolveMainButtonText());
        }
        enableAndSet(resolveMainButtonText());
    }

    private Driver getDriverOrSelectIfMany(List<Driver> items) {
        if (isNullOrEmpty(items))
            throw new RuntimeException("Collection is null or empty!");

        if (items.size() == 1) {
            return first(items);
        } else {
            var selectDialog = new SelectDialog<>(items, getCurrentStage(mainButton));

            return selectDialog.getResult();
        }
    }

    private Certificate getCertificateOrSelectIfMany(List<Certificate> items) {
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
        disableAndSet(translate("btn.signing"));

        try {
            var signedContent = signingManager.sign(signatureUnit);
            onSigned.accept(signedContent);
        } catch (UserException e) {
            displayError(e);
        }

        enableAndSet(resolveMainButtonText());
    }

    private void disableAndSet(String text) {
        mainButton.setText(text);
        mainButton.setDisable(true);
    }

    private void enableAndSet(String text) {
        mainButton.setText(text);
        mainButton.setDisable(false);
    }

    public String resolveMainButtonText() {
        if (isSignerReady()) {
            return translate("btn.signAs", signingManager.getActiveCertificate().getDisplayedName());
        } else {
            return translate("btn.loadSigners");
        }
    }

    private void materialize(Node node) {
        if (!node.isManaged())
            node.setManaged(true);
        if (!node.isVisible())
            node.setVisible(true);
    }

    private void vanish(Node node) {
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
    private void onHiddenOpenFileButtonAction() {
        textArea.setText("");

        File targetFile = new File((String)hiddenOpenFileButton.getUserData());

        new Thread(() -> {
            try {
                Desktop.getDesktop().open(targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
