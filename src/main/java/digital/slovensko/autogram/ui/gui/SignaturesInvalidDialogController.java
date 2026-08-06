package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.ValidationReports;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignaturesInvalidDialogController extends BaseController implements SuppressedFocusController {
    private final SigningDialogController signingDialogController;
    private final ValidationReports reports;

    @FXML
    Button cancelButton;
    @FXML
    Button continueButton;
    @FXML
    Node mainBox;
    @FXML
    VBox signaturesTable;
    @FXML
    Text signaturesSummaryTitle;
    @FXML
    Button showSignaturesButton;
    @FXML
    WebView signaturesSummaryWebView;

    public SignaturesInvalidDialogController(SigningDialogController controller, ValidationReports reports) {
        this.signingDialogController = controller;
        this.reports = reports;
    }

    public void initialize() {
        signaturesSummaryTitle.setText(i18n(
                reports.shouldShowDocumentContext() ? "signature.table.title.documents" : "signature.table.title"));

        signaturesSummaryWebView.setContextMenuEnabled(false);
        signaturesSummaryWebView.getEngine().setJavaScriptEnabled(true);
        signaturesSummaryWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldState,
                newState) -> {
            if (newState == Worker.State.SUCCEEDED)
                Platform.runLater(this::resizeSignaturesSummaryWebView);
        });

        var summaryDocument = SignatureHtmlRenderer.buildSummaryDocument(resources, reports, true,
                SignatureValidator.getInstance().areTLsLoaded(), 6);
        signaturesSummaryWebView.getEngine().loadContent(summaryDocument.html(), "text/html");
        showSignaturesButton.setText(
                i18n(summaryDocument.truncated() ? "signature.table.more.btn" : "signature.table.show.btn"));
    }

    public void onShowSignaturesButtonAction() {
        signingDialogController.onShowSignaturesButtonPressed(null);
    }

    private void resizeSignaturesSummaryWebView() {
        try {
            var contentHeight = signaturesSummaryWebView.getEngine()
                    .executeScript("Math.max(document.body.scrollHeight, document.documentElement.scrollHeight)");
            if (contentHeight instanceof Number number) {
                signaturesSummaryWebView.setPrefHeight(Math.max(42, Math.min(300, number.doubleValue() + 6)));
                if (mainBox.getScene() != null && mainBox.getScene().getWindow() instanceof Stage stage)
                    stage.sizeToScene();
            }
        } catch (Exception ignored) {
            // Keep the default WebView height if the document is not measurable yet.
        }
    }

    public void close() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage)
            ((Stage) window).close();

        signingDialogController.enableSigningOnAllJobs();
    }

    public void onCancelAction() {
        close();
        signingDialogController.close();
    }

    public void onContinueAction() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage)
            ((Stage) window).close();
        signingDialogController.sign();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
