package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SignatureValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static digital.slovensko.autogram.ui.gui.GUIValidationUtils.createSignatureBox;

public class SignaturesController extends BaseController implements SuppressedFocusController {
    private final GUI gui;
    private Reports signatureCheckReports;
    private Reports signatureValidationReports;
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
    VBox signaturesBox;
    @FXML
    Button closeButton;

    public SignaturesController(Reports signatureCheckReports, GUI gui) {
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

    public void onSignatureValidationCompleted(Reports reports) {
        signatureValidationMessage.setText("");
        signatureValidationMessage.setVisible(false);

        signatureValidationReports = reports;

        renderSignatures();

        gui.onWorkThreadDo(() -> {
            signatureValidationReportsHTML = SignatureValidator
                    .getSignatureValidationReportHTML(signatureValidationReports);
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
        if (signatureValidationReports != null)
            renderSignatures(signatureValidationReports, true);

        else
            renderSignatures(signatureCheckReports, false);
    }

    public void renderSignatures(Reports reports, boolean isValidated) {
        signaturesBox.getChildren().clear();

        for (var signatureId : reports.getDiagnosticData().getSignatureIdList())
            signaturesBox.getChildren().add(createSignatureBox(resources, reports, isValidated, signatureId, e -> {
                getNodeForLoosingFocus().requestFocus();
            }, isValidated && SignatureValidator.getInstance().areTLsLoaded()));
    }
}
