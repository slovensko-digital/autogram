package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.ui.Visualizer;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.model.CommonDocument;
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

public class SigningDialogController implements SuppressedFocusController, Visualizer {
    private final GUI gui;
    private final Autogram autogram;
    private final Visualization visualization;

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
    public Button mainButton;
    @FXML
    public Button changeKeyButton;
    @FXML
    VBox unsupportedVisualizationInfoBox;

    public SigningDialogController(Visualization visualization, Autogram autogram, GUI gui) {
        this.visualization = visualization;
        this.gui = gui;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();

//        mainBox.setPrefWidth(signingJob.getVisualizationWidth());
//
//        if (signingJob.isPlainText()) {
//            showPlainTextVisualization(signingJob.getDocumentAsPlainText());
//        } else if (signingJob.isHTML()) {
//            showHTMLVisualization(signingJob.getDocumentAsHTML());
//        } else if (signingJob.isPDF()) {
//            showPDFVisualization(signingJob.getDocumentAsBase64Encoded());
//        } else if (signingJob.isImage()) {
//            showImageVisualization(signingJob.getDocument().openStream());
//        } else if (signingJob.isAsice()) {
//            showAsiceVisualization();
//        } else {
//            showUnsupportedVisualization();
//        }
        visualization.initialize(this);
    }

    public void onMainButtonPressed(ActionEvent event) {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            autogram.pickSigningKeyAndThen(gui::setActiveSigningKey);
        } else {
            gui.disableSigning();
            getNodeForLoosingFocus().requestFocus();
            autogram.sign(visualization.getJob(), signingKey);
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
            mainButton.setText("Podpísať ako "
                    + DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
            mainButton.getStyleClass()
                    .removeIf(style -> style.equals("autogram-button--secondary"));
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

    public void showPlainTextVisualization(String text) {
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        plainTextArea.setText(text);
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
    }

    public void showHTMLVisualization(String html) {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", html);
            }
        });
        engine.load(getClass().getResource("visualization-html.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-html");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    public void showPDFVisualization(String base64EncodedPdf) {
        var engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                engine.executeScript(
                        "displayPdf('" + base64EncodedPdf + "')");
            }
        });
        engine.load(getClass().getResource("visualization-pdf.html").toExternalForm());
        webViewContainer.getStyleClass().add("autogram-visualizer-pdf");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    public void showImageVisualization(CommonDocument doc) {
        // TODO what about visualization
        imageVisualization.fitWidthProperty()
                .bind(imageVisualizationContainer.widthProperty().subtract(4));
        imageVisualization.setImage(new Image(doc.openStream()));
        imageVisualization.setPreserveRatio(true);
        imageVisualization.setSmooth(true);
        imageVisualization.setCursor(Cursor.OPEN_HAND);
        imageVisualizationContainer.setPannable(true);
        imageVisualizationContainer.setFitToWidth(true);
        imageVisualizationContainer.setVisible(true);
        imageVisualizationContainer.setManaged(true);
    }

//    private void showAsiceVisualization() {
//        DSSDocument document = getOriginalDocument(signingJob.getDocument());
//        if (DSSDocumentUtils.isPlainText(document)) {
//            showPlainTextVisualization(DSSDocumentUtils.getDocumentAsPlainText(document, signingJob.getParameters().getTransformation()));
//        } else if (DSSDocumentUtils.isPdf(document)) {
//            showPDFVisualization(DSSDocumentUtils.getDocumentAsBase64Encoded(document));
//        } else if (DSSDocumentUtils.isImage(document)) {
//            showImageVisualization(document.openStream());
//        } else if (DSSDocumentUtils.isXML(document)){
//            setMimeTypeFromManifest(document);
//
//            String transformation = signingJob.getParameters().getTransformation();
//            if (transformation == null) {
//                showUnsupportedVisualization();
//            } else if (signingJob.getParameters().getTransformationOutputMimeType() == MimeTypeEnum.HTML) {
//                showHTMLVisualization(DSSDocumentUtils.getDocumentAsPlainText(document, transformation));
//            } else {
//                showPlainTextVisualization(DSSDocumentUtils.getDocumentAsPlainText(document, transformation));
//            }
//        } else {
//            showUnsupportedVisualization();
//        }
//    }
//
//    private DSSDocument getOriginalDocument(DSSDocument document) {
//        SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(document);
//        documentValidator.setCertificateVerifier(new CommonCertificateVerifier());
//        List<AdvancedSignature> signatures = documentValidator.getSignatures();
//        if (signatures.isEmpty()) {
//            throw new RuntimeException("No signatures in document");
//        }
//        AdvancedSignature advancedSignature = signatures.get(0);
//        List<DSSDocument> originalDocuments = documentValidator.getOriginalDocuments(advancedSignature.getId());
//        if (originalDocuments.isEmpty()) {
//            throw new RuntimeException("No original documents found");
//        }
//        return originalDocuments.get(0);
//    }
//
//    private void setMimeTypeFromManifest(DSSDocument document) {
//        DSSDocument manifest = getManifest();
//        if (manifest == null) {
//            throw new RuntimeException("Unable to find manifest.xml");
//        }
//
//        String documentName = document.getName();
//        MimeType mimeType = getMimeTypeFromManifest(manifest, documentName);
//        if (mimeType == null) {
//            throw new RuntimeException("Unable to get mimetype from manifest.xml");
//        }
//
//        document.setMimeType(mimeType);
//    }
//
//    private DSSDocument getManifest() {
//        ASiCWithXAdESContainerExtractor extractor = new ASiCWithXAdESContainerExtractor(signingJob.getDocument());
//        ASiCContent aSiCContent = extractor.extract();
//        List<DSSDocument> manifestDocuments = aSiCContent.getManifestDocuments();
//        if (manifestDocuments.isEmpty()) {
//            return null;
//        }
//        return manifestDocuments.get(0);
//    }
//
//    private MimeType getMimeTypeFromManifest(DSSDocument manifest, String documentName) {
//        NodeList fileEntries = getFileEntriesFromManifest(manifest);
//
//        for (int i = 0; i < fileEntries.getLength(); i++) {
//            String fileName = fileEntries.item(i).getAttributes().item(0).getNodeValue();
//            String fileType = fileEntries.item(i).getAttributes().item(1).getNodeValue();
//
//            if (documentName.equals(fileName)) {
//                return AutogramMimeType.fromMimeTypeString(fileType);
//            }
//        }
//
//        return null;
//    }
//
//    private NodeList getFileEntriesFromManifest(DSSDocument manifest) {
//        try {
//            var builderFactory = DocumentBuilderFactory.newInstance();
//            builderFactory.setNamespaceAware(true);
//            var document = builderFactory.newDocumentBuilder().parse(new InputSource(manifest.openStream()));
//            return document.getDocumentElement().getElementsByTagName("manifest:file-entry");
//        } catch (Exception e) {
//            return null;
//        }
//    }

    public void showUnsupportedVisualization() {
        unsupportedVisualizationInfoBox.setVisible(true);
        unsupportedVisualizationInfoBox.setManaged(true);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    @Override
    public void setPrefWidth(double prefWidth) {
        mainBox.setPrefWidth(prefWidth);
    }
}
