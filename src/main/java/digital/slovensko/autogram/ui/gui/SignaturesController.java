package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.ValidationReports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SignaturesController extends BaseController implements SuppressedFocusController {
    private final GUI gui;
    private ValidationReports signatureCheckReports;
    private ValidationReports signatureValidationReports;
    private String signatureValidationReportsHTML;

    @FXML
    Text signatureValidationMessage;
    @FXML
    HBox signatureDetailsGroup;
    @FXML
    Button signatureDetailsButton;
    @FXML
    VBox mainBox;
    @FXML
    VBox signaturesContainer;
    @FXML
    Button closeButton;

    public SignaturesController(ValidationReports signatureCheckReports, GUI gui) {
        this.signatureCheckReports = signatureCheckReports;
        this.gui = gui;
    }

    @Override
    public void initialize() {
        renderSignatures();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public void onSignatureValidationCompleted(ValidationReports reports) {
        signatureValidationMessage.setText("");
        signatureValidationMessage.setVisible(false);

        signatureValidationReports = reports;

        renderSignatures();

        gui.onWorkThreadDo(() -> {
            signatureValidationReportsHTML = buildSignatureValidationReportsHTML(signatureValidationReports);
            signatureDetailsButton.setVisible(true);
        });
    }

    public void onSignatureDetailsButtonAction() {
        var controller = new SignatureDetailsController(signatureValidationReportsHTML);
        var root = GUIUtils.loadFXML(controller, "signature-details.fxml");

        var stage = new Stage();
        stage.setTitle(i18n("signature.present.details.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(mainBox.getScene().getWindow());
        stage.setResizable(false);
        stage.show();
    }

    public void onCloseButtonAction() {
        GUIUtils.closeWindow(mainBox);
    }

    public void renderSignatures() {
        if (signatureValidationReports != null && signatureValidationReports.haveSignatures())
            renderSignatures(signatureValidationReports, true);

        else if (signatureCheckReports != null && signatureCheckReports.haveSignatures())
            renderSignatures(signatureCheckReports, false);
    }

    public void renderSignatures(ValidationReports reports, boolean isValidated) {
        signaturesContainer.getChildren().clear();
        var areTLsLoaded = SignatureValidator.getInstance().areTLsLoaded();
        if (isValidated && !areTLsLoaded)
            signaturesContainer.getChildren().add(
                    GUIValidationUtils.createWarningText(i18n("signing.tlsLoading.error")));

        for (var signature : reports.getSignatures())
            signaturesContainer.getChildren().add(GUIValidationUtils.createSignatureBox(resources,
                    signature.documentReport(), isValidated, signature.signatureId(),
                    ignored -> resizeToScene(), areTLsLoaded));
    }

    private void resizeToScene() {
        if (mainBox.getScene() != null && mainBox.getScene().getWindow() instanceof Stage stage)
            stage.sizeToScene();
    }

    private String buildSignatureValidationReportsHTML(ValidationReports reports) {
        if (!reports.isMultiDocumentJob())
            return SignatureValidator.getSignatureValidationReportHTML(reports.getReports());

        var content = new StringBuilder();
        for (var documentReport : reports.getDocumentReports()) {
            if (content.length() > 0)
                content.append("<hr/>");

            content.append("<h2>")
                    .append(escapeHtml(GUIValidationUtils.getDisplayDocumentName(resources, documentReport)))
                    .append("</h2>")
                    .append(SignatureValidator.getSignatureValidationReportBodyHTML(documentReport.reports()));
        }

        return SignatureValidator.wrapSignatureValidationReportHTML(content.toString());
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
