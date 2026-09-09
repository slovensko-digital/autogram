package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.ui.Visualizer;
import digital.slovensko.autogram.util.DSSUtils;
import digital.slovensko.autogram.util.AsicContainerUtils;
import eu.europa.esig.dss.model.DSSDocument;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SigningDialogController extends BaseController implements SuppressedFocusController, Visualizer {
    private static final double DEFAULT_TAB_HEADER_HEIGHT = 36;
    private static final int MAX_DOCUMENT_TAB_TITLE_LENGTH = 20;

    private final GUI gui;
    private final Autogram autogram;
    private final String title;
    private final UserSettings userSettings;
    private SignaturesController signaturesController;
    private SignaturesNotValidatedDialogController signaturesNotValidatedDialogController;
    private boolean signatureValidationCompleted = false;
    private boolean signatureCheckCompleted = false;
    private final Visualization visualization;
    private ValidationReports signatureValidationReports;
    private ValidationReports signatureCheckReports;
    private final boolean shouldCheckValidityBeforeSigning;
    private List<DSSDocument> previewDocuments = List.of();
    private List<Node> signatureSummaries = List.of();

    @FXML
    VBox mainBox;
    @FXML
    TextArea plainTextArea;
    @FXML
    WebView webView;
    @FXML
    VBox webViewContainer;
    @FXML
    ScrollPane pdfVisualizationContainer;
    @FXML
    VBox pdfVisualizationBox;
    @FXML
    ImageView imageVisualization;
    @FXML
    ScrollPane imageVisualizationContainer;
    @FXML
    VBox singleDocumentVisualizationContainer;
    @FXML
    VBox visualizationHost;
    @FXML
    TabPane documentTabPane;
    @FXML
    public Button mainButton;
    @FXML
    public Button changeKeyButton;
    @FXML
    VBox unsupportedVisualizationInfoBox;
    @FXML
    VBox signaturesTable;
    @FXML
    VBox signaturesSummaryHost;
    @FXML
    Text headerText;

    public SigningDialogController(Visualization visualization, Autogram autogram, GUI gui, String title,
            UserSettings userSettings,
            boolean shouldCheckValidityBeforeSigning) {
        this.visualization = visualization;
        this.gui = gui;
        this.autogram = autogram;
        this.title = title;
        this.userSettings = userSettings;
        this.shouldCheckValidityBeforeSigning = shouldCheckValidityBeforeSigning;
    }

    @Override
    public void initialize() throws IOException {
        headerText.setText(title);
        plainTextArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        singleDocumentVisualizationContainer.setMinHeight(0);
        visualizationHost.setMinHeight(0);
        webViewContainer.setMinHeight(0);
        pdfVisualizationContainer.setMinHeight(0);
        imageVisualizationContainer.setMinHeight(0);
        plainTextArea.setMinHeight(0);
        signaturesTable.setManaged(false);
        signaturesTable.setVisible(false);
        previewDocuments = resolvePreviewDocuments();
        documentTabPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null)
                return;

            Platform.runLater(this::refreshDocumentTabPaneHeight);
        });
        setupDocumentTabs();
        refreshSigningKey();
        autogram.checkPDFACompliance(visualization.getJob());
    }

    private void setupDocumentTabs() {
        var documents = previewDocuments;
        if (documents.size() <= 1) {
            initializeVisualization(visualization);
            return;
        }

        documentTabPane.setManaged(true);
        documentTabPane.setVisible(true);

        documentTabPane.getTabs().clear();
        for (int i = 0; i < documents.size(); i++)
            documentTabPane.getTabs().add(createDocumentTab(documents.get(i), i + 1));

        documentTabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() < 0)
                return;

            showVisualizationForDocument(newValue.intValue());
        });

        showVisualizationForDocument(0);
        documentTabPane.getSelectionModel().select(0);
        refreshDocumentTabPaneHeight();
    }

    private void showVisualizationForDocument(int documentIndex) {
        initializeVisualization(createVisualization(documentIndex));
        showSignatureSummaryForDocument(documentIndex);
    }

    private Visualization createVisualization(int documentIndex) {
        if (documentIndex == 0)
            return visualization;

        try {
            return DocumentVisualizationBuilder.fromDocument(visualization.getJob(),
                    previewDocuments.get(documentIndex), userSettings);
        } catch (Exception e) {
            return null;
        }
    }

    private List<DSSDocument> resolvePreviewDocuments() {
        var jobDocuments = visualization.getJob().getDocuments();
        if (jobDocuments.size() > 1)
            return jobDocuments;

        if (!AutogramMimeType.isAsice(visualization.getJob().getDocument().getMimeType()))
            return jobDocuments;

        try {
            var originalDocuments = AsicContainerUtils.getOriginalDocuments(visualization.getJob().getDocument());
            if (originalDocuments.size() > 1)
                return originalDocuments;
        } catch (Exception e) {
            // Fall back to the original job document when the input is not a signed ASiC container.
        }

        return jobDocuments;
    }

    private void initializeVisualization(Visualization currentVisualization) {
        clearVisualization();
        if (currentVisualization == null) {
            showUnsupportedVisualization();
            refreshLayout();
            return;
        }

        try {
            currentVisualization.initialize(this);
        } catch (Exception e) {
            clearVisualization();
            showUnsupportedVisualization();
        }

        refreshLayout();
    }

    private void clearVisualization() {
        plainTextArea.clear();
        plainTextArea.setManaged(false);
        plainTextArea.setVisible(false);

        webView.getEngine().loadContent("");
        webViewContainer.setManaged(false);
        webViewContainer.setVisible(false);

        pdfVisualizationBox.getChildren().clear();
        pdfVisualizationContainer.setManaged(false);
        pdfVisualizationContainer.setVisible(false);

        imageVisualization.fitWidthProperty().unbind();
        imageVisualization.setImage(null);
        imageVisualizationContainer.setManaged(false);
        imageVisualizationContainer.setVisible(false);

        unsupportedVisualizationInfoBox.setManaged(false);
        unsupportedVisualizationInfoBox.setVisible(false);
    }

    private void refreshLayout() {
        mainBox.requestLayout();
        if (mainButton.getScene() == null)
            return;

        mainButton.getScene().getRoot().applyCss();
        mainButton.getScene().getRoot().layout();
    }

    private void refreshDocumentTabPaneHeight() {
        if (!documentTabPane.isManaged() || documentTabPane.getScene() == null)
            return;

        documentTabPane.applyCss();
        documentTabPane.layout();

        var headerArea = documentTabPane.lookup(".tab-header-area");
        double headerHeight = DEFAULT_TAB_HEADER_HEIGHT;

        if (headerArea instanceof Region region) {
            headerHeight = Math.max(headerArea.getBoundsInLocal().getHeight(), region.prefHeight(-1));
        }

        documentTabPane.setMinHeight(headerHeight);
        documentTabPane.setPrefHeight(headerHeight);
        documentTabPane.setMaxHeight(headerHeight);

        refreshLayout();
    }

    private String getDisplayDocumentName(DSSDocument document, int index) {
        var name = document.getName();
        if (name != null && !name.isBlank())
            return name;

        return i18n("signing.multiDocument.unnamedDocument", index);
    }

    private Tab createDocumentTab(DSSDocument document, int index) {
        var fullName = getDisplayDocumentName(document, index);
        var tab = new Tab(abbreviateMiddle(fullName, MAX_DOCUMENT_TAB_TITLE_LENGTH));
        tab.setClosable(false);
        tab.setTooltip(new Tooltip(fullName));
        return tab;
    }

    private static String abbreviateMiddle(String text, int maxLength) {
        if (text.length() <= maxLength)
            return text;

        var extensionIndex = text.lastIndexOf('.');
        var extension = extensionIndex > 0 && text.length() - extensionIndex <= 8 ? text.substring(extensionIndex) : "";
        var baseName = extension.isEmpty() ? text : text.substring(0, extensionIndex);
        var available = maxLength - extension.length() - 3;
        if (available <= 4)
            return text.substring(0, Math.max(0, maxLength - 3)) + "...";

        var prefixLength = (available + 1) / 2;
        var suffixLength = available / 2;
        var suffixStart = Math.max(prefixLength, baseName.length() - suffixLength);
        return baseName.substring(0, prefixLength) + "..." + baseName.substring(suffixStart) + extension;
    }

    public void onMainButtonPressed(ActionEvent event) {
        checkExistingSignatureValidityAndSign();
    }

    private void showSignaturesNotValidatedDialog() {
        if (signaturesNotValidatedDialogController == null)
            signaturesNotValidatedDialogController = new SignaturesNotValidatedDialogController(this);

        var root = GUIUtils.loadFXML(signaturesNotValidatedDialogController, "signatures-not-validated-dialog.fxml");
        var stage = new Stage();
        stage.setTitle(i18n("signature.notValidated.title"));
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(mainButton.getScene().getWindow());
        stage.setOnCloseRequest(event -> signaturesNotValidatedDialogController.close());

        GUIUtils.suppressDefaultFocus(stage, signaturesNotValidatedDialogController);

        stage.show();
    }

    private void showSignaturesInvalidDialog() {
        var signaturesInvalidDialogController = new SignaturesInvalidDialogController(this, signatureValidationReports);

        var root = GUIUtils.loadFXML(signaturesInvalidDialogController, "signatures-invalid-dialog.fxml");
        var stage = new Stage();
        stage.setTitle(i18n("signature.invalid.title"));
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(mainButton.getScene().getWindow());
        stage.setOnCloseRequest(event -> signaturesInvalidDialogController.close());;

        GUIUtils.suppressDefaultFocus(stage, signaturesInvalidDialogController);

        stage.show();
    }

    private void checkExistingSignatureValidityAndSign() {
        if (!shouldCheckValidityBeforeSigning) {
            sign();
            return;
        }

        if (!signatureCheckCompleted
                || (signatureCheckReports != null && signatureCheckReports.haveSignatures() && !signatureValidationCompleted)) {
            showSignaturesNotValidatedDialog();
            return;
        }

        if (signatureCheckReports == null || !signatureCheckReports.haveSignatures()) {
            sign();
            return;
        }

        if (signatureValidationReports != null && signatureValidationReports.haveInvalidSignatures()) {
            showSignaturesInvalidDialog();
            return;
        }

        sign();
    }

    public void sign() {
        var signingKey = gui.getActiveSigningKey();
        if (signingKey == null) {
            autogram.pickSigningKeyAndThen(key -> {
                gui.setActiveSigningKeyAndThen(key, k -> {
                    gui.disableSigning();
                    getNodeForLoosingFocus().requestFocus();
                    autogram.sign(visualization.getJob(), k);
                });
            });
        } else {
            gui.disableSigning();
            getNodeForLoosingFocus().requestFocus();
            autogram.sign(visualization.getJob(), signingKey);
        }
    }

    public void onChangeKeyButtonPressed(ActionEvent event) {
        gui.resetSigningKey();
        checkExistingSignatureValidityAndSign();
    }

    public void onShowSignaturesButtonPressed(ActionEvent event) {
        if (signaturesController == null)
            signaturesController = new SignaturesController(signatureCheckReports, gui);

        var root = GUIUtils.loadFXML(signaturesController, "present-signatures-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(i18n("signature.present.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(mainButton.getScene().getWindow());
        GUIUtils.suppressDefaultFocus(stage, signaturesController);
        stage.show();
        stage.setResizable(false);
        stage.show();

        if (signatureValidationCompleted)
            signaturesController.onSignatureValidationCompleted(signatureValidationReports);
    }

    public void onSignatureCheckCompleted(ValidationReports reports) {
        signatureCheckReports = reports;
        signatureCheckCompleted = true;
        renderSignatures(reports, false, true);

        if (signaturesNotValidatedDialogController != null)
            signaturesNotValidatedDialogController.close();
    }

    public void onSignatureValidationCompleted(ValidationReports reports) {
        signatureValidationCompleted = true;
        signatureValidationReports = reports;
        renderSignatures(reports, true, SignatureValidator.getInstance().areTLsLoaded());
        if (signaturesController != null)
            signaturesController.onSignatureValidationCompleted(reports);

        if (signaturesNotValidatedDialogController != null)
            signaturesNotValidatedDialogController.close();
    }

    public void renderSignatures(ValidationReports reports, boolean isValidated, boolean areTLsLoaded) {
        if (reports == null || !reports.haveSignatures())
            return;

        var summaries = new ArrayList<Node>();
        for (int index = 0; index < previewDocuments.size(); index++) {
            var summary = new VBox(8);
            if (!areTLsLoaded)
                summary.getChildren().add(GUIValidationUtils.createWarningText(i18n("signing.tlsLoading.error")));
            if (reports.hasIncompleteContainerCoverage())
                summary.getChildren().add(GUIValidationUtils.createWarningText(
                        i18n("signature.table.incompleteCoverage.warning")));

            var signatures = reports.getSignaturesForPreviewDocument(previewDocuments.get(index), index);
            var table = new VBox(GUIValidationUtils.createSignatureTableRows(resources, signatures, false,
                    isValidated, ignored -> onShowSignaturesButtonPressed(null), 3));
            table.getStyleClass().add("autogram-signatures-table");
            summary.getChildren().add(table);
            summaries.add(summary);
        }
        signatureSummaries = List.copyOf(summaries);

        signaturesTable.setManaged(true);
        signaturesTable.setVisible(true);
        var selectedIndex = documentTabPane.isManaged()
                ? documentTabPane.getSelectionModel().getSelectedIndex()
                : 0;
        showSignatureSummaryForDocument(Math.max(0, selectedIndex));

        if (mainButton.getScene() != null && mainButton.getScene().getWindow() instanceof Stage stage)
            stage.sizeToScene();
    }

    private void showSignatureSummaryForDocument(int documentIndex) {
        if (documentIndex < 0 || documentIndex >= signatureSummaries.size())
            return;

        signaturesSummaryHost.getChildren().setAll(signatureSummaries.get(documentIndex));
    }

    public void refreshSigningKey() {
        var key = gui.getActiveSigningKey();
        if (key == null) {
            mainButton.setText(i18n("general.sign.btn"));
            changeKeyButton.setVisible(false);
        } else {
            mainButton.setText(i18n("signing.signAs.btn", DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253())));
            changeKeyButton.setVisible(true);
        }
    }

    public void enableSigning() {
        refreshSigningKey();
        mainButton.setDisable(false);
        changeKeyButton.setDisable(false);
    }

    public void enableSigningOnAllJobs() {
        gui.enableSigningOnAllJobs();
    }

    public void close() {
        var window = mainButton.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public void disableKeyPicking() {
        mainButton.setText(i18n("signing.keyPicking.btn"));
        mainButton.setDisable(true);
        changeKeyButton.setDisable(true);
    }

    public void disableSigning() {
        mainButton.setText(i18n("signing.signing.btn"));
        mainButton.setDisable(true);
        changeKeyButton.setDisable(true);
    }

    public void showPlainTextVisualization(String text) {
        plainTextArea.setText(text);
        plainTextArea.setVisible(true);
        plainTextArea.setManaged(true);
    }

    public void showHTMLVisualization(String html) {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        var engine = webView.getEngine();
        ChangeListener<Worker.State> listener = new ChangeListener<>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Worker.State> observable,
                    Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                engine.getDocument().getElementById("frame").setAttribute("srcdoc", html);
                    engine.getLoadWorker().stateProperty().removeListener(this);
            }
            }
        };
        engine.getLoadWorker().stateProperty().addListener(listener);
        engine.load(getClass().getResource("visualization-html.html").toExternalForm());
        if (!webViewContainer.getStyleClass().contains("autogram-visualizer-html"))
            webViewContainer.getStyleClass().add("autogram-visualizer-html");
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);
    }

    public void showPDFVisualization(ArrayList<byte[]> data) {
        data.forEach(page -> {
            var imgView = new ImageView();
            imgView.fitWidthProperty().bind(pdfVisualizationContainer.widthProperty().subtract(30));
            imgView.setImage(new Image(new ByteArrayInputStream(page)));
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);

            pdfVisualizationBox.getChildren().add(new HBox(imgView));
        });

        pdfVisualizationContainer.setFitToWidth(true);
        pdfVisualizationContainer.setVisible(true);
        pdfVisualizationContainer.setManaged(true);
    }

    public void showImageVisualization(DSSDocument doc) {
        // TODO what about visualization
        imageVisualization.fitWidthProperty().unbind();
        imageVisualization.fitWidthProperty().bind(imageVisualizationContainer.widthProperty().subtract(4));
        imageVisualization.setImage(new Image(doc.openStream()));
        imageVisualization.setPreserveRatio(true);
        imageVisualization.setSmooth(true);
        imageVisualization.setCursor(Cursor.OPEN_HAND);
        imageVisualizationContainer.setPannable(true);
        imageVisualizationContainer.setFitToWidth(true);
        imageVisualizationContainer.setVisible(true);
        imageVisualizationContainer.setManaged(true);
    }

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
