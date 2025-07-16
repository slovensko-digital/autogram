package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.ui.Visualizer;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static digital.slovensko.autogram.ui.gui.GUIValidationUtils.createSignatureTableRows;
import static digital.slovensko.autogram.ui.gui.GUIValidationUtils.createWarningText;

public class SigningDialogController extends BaseController implements SuppressedFocusController, Visualizer {
    private final GUI gui;
    private final Autogram autogram;
    private final String title;
    private SignaturesController signaturesController;
    private SignaturesNotValidatedDialogController signaturesNotValidatedDialogController;
    private boolean signatureValidationCompleted = false;
    private boolean signatureCheckCompleted = false;
    private final Visualization visualization;
    private Reports signatureValidationReports;
    private Reports signatureCheckReports;
    private final boolean shouldCheckValidityBeforeSigning;

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
    public Button mainButton;
    @FXML
    public Button changeKeyButton;
    @FXML
    VBox unsupportedVisualizationInfoBox;
    @FXML
    VBox signaturesTable;
    @FXML
    Text headerText;

    public SigningDialogController(Visualization visualization, Autogram autogram, GUI gui, String title,
            boolean shouldCheckValidityBeforeSigning) {
        this.visualization = visualization;
        this.gui = gui;
        this.autogram = autogram;
        this.title = title;
        this.shouldCheckValidityBeforeSigning = shouldCheckValidityBeforeSigning;
    }

    @Override
    public void initialize() throws IOException {
        headerText.setText(title);
        signaturesTable.setManaged(false);
        signaturesTable.setVisible(false);
        refreshSigningKey();
        visualization.initialize(this);
        autogram.checkPDFACompliance(visualization.getJob());
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

        if ((!signatureCheckCompleted) || ((signatureCheckReports != null) && !signatureValidationCompleted)) {
            showSignaturesNotValidatedDialog();
            return;
        }

        if (signatureCheckReports == null) {
            sign();
            return;
        }

        for (var signatureId : signatureValidationReports.getSimpleReport().getSignatureIdList()) {
            if (!signatureValidationReports.getSimpleReport().isValid(signatureId)) {
                showSignaturesInvalidDialog();
                return;
            }
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

    public void onSignatureCheckCompleted(Reports reports) {
        signatureCheckReports = reports;
        signatureCheckCompleted = true;
        renderSignatures(reports, false, true);

        if (signaturesNotValidatedDialogController != null)
            signaturesNotValidatedDialogController.close();
    }

    public void onSignatureValidationCompleted(Reports reports) {
        signatureValidationCompleted = true;
        signatureValidationReports = reports;
        renderSignatures(reports, true, SignatureValidator.getInstance().areTLsLoaded());
        if (signaturesController != null)
            signaturesController.onSignatureValidationCompleted(reports);

        if (signaturesNotValidatedDialogController != null)
            signaturesNotValidatedDialogController.close();
    }

    public void renderSignatures(Reports reports, boolean isValidated, boolean areTLsLoaded) {
        if (reports == null)
            return;

        signaturesTable.setManaged(true);
        signaturesTable.setVisible(true);
        signaturesTable.getChildren().clear();

        if (!areTLsLoaded)
            signaturesTable.getChildren().add(
                    createWarningText(i18n("signing.tlsLoading.error")));

        signaturesTable.getChildren().add(
                createSignatureTableRows(reports, isValidated, e -> onShowSignaturesButtonPressed(null), 3));

        var stage = (Stage) mainButton.getScene().getWindow();
        stage.sizeToScene();

        // Magic code to make the window resize to the correct size
        signaturesTable.setManaged(true);
        signaturesTable.setVisible(true);
        stage.sizeToScene();
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
