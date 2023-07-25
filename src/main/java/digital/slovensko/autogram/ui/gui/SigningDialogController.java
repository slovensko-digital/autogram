package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.util.DSSUtils;
import digital.slovensko.autogram.util.DSSDocumentUtils;
import eu.europa.esig.dss.asic.common.ASiCContent;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESContainerExtractor;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.*;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.List;

public class SigningDialogController implements SuppressedFocusController {
    private final GUI gui;
    private final SigningJob signingJob;
    private final Autogram autogram;

    @FXML
    VBox mainBox;
    @FXML
    TextArea plainTextArea;
    @FXML
    WebView webView;
    @FXML
    VBox webViewContainer;
    @FXML
    ImageView imageVisualization;
    @FXML
    ScrollPane imageVisualizationContainer;
    @FXML
    VBox unsupportedVisualizationInfoBox;
    @FXML
    public Button mainButton;
    @FXML
    public Button changeKeyButton;

    public SigningDialogController(SigningJob signingJob, Autogram autogram, GUI gui) {
        this.signingJob = signingJob;
        this.gui = gui;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();

        mainBox.setPrefWidth(signingJob.getVisualizationWidth());

        if (signingJob.isPlainText()) {
            showPlainTextVisualization();
        } else if (signingJob.isHTML()) {
            showHTMLVisualization();
        } else if (signingJob.isPDF()) {
            showPDFVisualization();
        } else if (signingJob.isImage()) {
            showImageVisualization();
        } else if (signingJob.isAsice()) {
            showAsiceVisualization();
        } else {
            showUnsupportedVisualization();
        }
    }

    public void onMainButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
        } else {
            gui.disableSigning();
            getNodeForLoosingFocus().requestFocus();
            autogram.sign(signingJob, signingKey);
        }
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        gui.resetSigningKey();
        autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
    }

    public void refreshSigningKey() {
        mainButton.setDisable(false);
        SigningKey key = gui.getActiveSigningKey();
        if (key == null) {
            mainButton.setText("Vybrať podpisový certifikát");
            mainButton.getStyleClass().add("autogram-button--secondary");
            changeKeyButton.setVisible(false);
        } else {
            mainButton.setText("Podpísať ako " + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
            mainButton.getStyleClass().removeIf(style -> style.equals("autogram-button--secondary"));
            changeKeyButton.setVisible(true);
        }
    }

    public void close() {
        var window = mainButton.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public void disableKeyPicking() {
        mainButton.setText("Načítavam certifikáty...");
        mainButton.setDisable(true);
    }

    public void disableSigning() {
        mainButton.setText("Prebieha podpisovanie...");
        mainButton.setDisable(true);
    }

    private void showPlainTextVisualization() {
        showPlainTextVisualization(signingJob.getDocumentAsPlainText());
    }

    private void showPlainTextVisualization(String document) {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        plainTextArea.setText(document);
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
    }

    private void showHTMLVisualization() {
        showHTMLVisualization(signingJob.getDocumentAsHTML());
    }

    private void showHTMLVisualization(String document) {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", document);
            }
        });
        engine.load(getClass().getResource("visualization-html.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-html");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    private void showPDFVisualization() {
        showPDFVisualization(signingJob.getDocumentAsBase64Encoded());
    }

    private void showPDFVisualization(String document) {
        var engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.executeScript("displayPdf('" + document + "')");
            }
        });
        engine.load(getClass().getResource("visualization-pdf.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-pdf");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }
    private void showImageVisualization() {
        showImageVisualization(signingJob.getDocument().openStream());
    }

    private void showImageVisualization(InputStream image) {
        imageVisualization.fitWidthProperty().bind(imageVisualizationContainer.widthProperty().subtract(4));
        imageVisualization.setImage(new Image(image));
        imageVisualization.setPreserveRatio(true);
        imageVisualization.setSmooth(true);
        imageVisualization.setCursor(Cursor.OPEN_HAND);
        imageVisualizationContainer.setPannable(true);
        imageVisualizationContainer.setFitToWidth(true);
        imageVisualizationContainer.setVisible(true);
        imageVisualizationContainer.setManaged(true);
    }

    private void showAsiceVisualization() {
        DSSDocument document = getOriginalDocument();
        if (DSSDocumentUtils.isPlainText(document)) {
            showPlainTextVisualization(DSSDocumentUtils.getDocumentAsPlainText(document, signingJob.getParameters().getTransformation()));
        } else if (DSSDocumentUtils.isPdf(document)) {
            showPDFVisualization(DSSDocumentUtils.getDocumentAsBase64Encoded(document));
        } else if (DSSDocumentUtils.isImage(document)) {
            showImageVisualization(document.openStream());
        } else if (DSSDocumentUtils.isXML(document)){
            setMimeTypeFromManifest(document);

            String transformation = signingJob.getParameters().getTransformation();
            if (transformation != null) {
                if(signingJob.getParameters().getTransformationOutputMimeType() == MimeTypeEnum.HTML) {
                    showHTMLVisualization(DSSDocumentUtils.getDocumentAsPlainText(document, transformation));
                } else {
                    showPlainTextVisualization(DSSDocumentUtils.getDocumentAsPlainText(document, transformation));
                }
            } else {
                showUnsupportedVisualization();
            }
        } else {
            showUnsupportedVisualization();
        }
    }

    private DSSDocument getOriginalDocument() {
        SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(signingJob.getDocument());
        documentValidator.setCertificateVerifier(new CommonCertificateVerifier());
        List<AdvancedSignature> signatures = documentValidator.getSignatures();
        AdvancedSignature advancedSignature = signatures.get(0);
        List<DSSDocument> originalDocuments = documentValidator.getOriginalDocuments(advancedSignature.getId());
        return originalDocuments.get(0);
    }

    private void setMimeTypeFromManifest(DSSDocument document) {
        DSSDocument manifest = getManifest();
        if (manifest == null) {
            throw new RuntimeException("Unable to find manifest.xml");
        }

        String documentName = document.getName();
        MimeType mimeType = getMimeTypeFromManifest(manifest, documentName);
        if (mimeType == null) {
            throw new RuntimeException("Unable to get mimetype from manifest.xml");
        }

        document.setMimeType(mimeType);
    }

    private DSSDocument getManifest() {
        ASiCWithXAdESContainerExtractor extractor = new ASiCWithXAdESContainerExtractor(signingJob.getDocument());
        ASiCContent aSiCContent = extractor.extract();
        List<DSSDocument> manifestDocuments = aSiCContent.getManifestDocuments();
        if (manifestDocuments.isEmpty()) {
            return null;
        }
        return manifestDocuments.get(0);
    }

    private MimeType getMimeTypeFromManifest(DSSDocument manifest, String documentName) {
        NodeList fileEntries = getFileEntriesFromManifest(manifest);

        for (int i = 0; i < fileEntries.getLength(); i++) {
            String fileName = fileEntries.item(i).getAttributes().item(0).getNodeValue();
            String fileType = fileEntries.item(i).getAttributes().item(1).getNodeValue();

            if (documentName.equals(fileName)) {
                return AutogramMimeType.fromMimeTypeString(fileType);
            }
        }

        return null;
    }

    private NodeList getFileEntriesFromManifest(DSSDocument manifest) {
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(manifest.openStream()));
            return document.getDocumentElement().getElementsByTagName("manifest:file-entry");
        } catch (Exception e) {
            return null;
        }
    }

    private void showUnsupportedVisualization() {
        unsupportedVisualizationInfoBox.setVisible(true);
        unsupportedVisualizationInfoBox.setManaged(true);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
