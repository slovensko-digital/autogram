package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SignatureValidator;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static digital.slovensko.autogram.ui.gui.GUIValidationUtils.*;

import java.util.ArrayList;

public class SignaturesController implements SuppressedFocusController {
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
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public void onSignatureValidationCompleted(Reports reports) {
        signatureValidationMessage.setText("");
        signatureValidationMessage.setVisible(false);

        signatureValidationReports = reports;

        showSignatures();

        gui.onWorkThreadDo(() -> {
            signatureValidationReportsHTML = SignatureValidator
                    .getSignatureValidationReportHTML(signatureValidationReports);
            signatureDetailsButton.setVisible(true);
        });
    }

    public void onSignatureDetailsButtonAction() {
        var controller = new SignatureDetailsController();
        var root = GUIUtils.loadFXML(controller, "signature-details.fxml");

        var stage = new Stage();
        stage.setTitle("Detaily podpisov");
        stage.setScene(new Scene(root));
        controller.showHTMLReport(signatureValidationReportsHTML);
        stage.setResizable(false);
        stage.show();
    }

    public void onCloseButtonAction() {
        GUIUtils.closeWindow(mainBox);
    }

    public void showSignatures() {
        if (signatureValidationReports != null)
            renderSignatures(signatureValidationReports, true);

        else
            renderSignatures(signatureCheckReports, false);
    }

    public void renderSignatures(Reports reports, boolean isValidated) {
        signaturesBox.getChildren().clear();

        var simple = reports.getSimpleReport();
        var diagnostic = reports.getDiagnosticData();

        for (var signatureId : diagnostic.getSignatureIdList()) {
            var isValid = isValidated && simple.isValid(signatureId);

            var name = simple.getSignedBy(signatureId);
            var signingTime = format.format(simple.getSigningTime(signatureId));
            var subject = getPrettyDNWithoutCN(
                    diagnostic.getSignatureById(signatureId).getSigningCertificate().getCertificateDN());
            var issuer = getPrettyDN(diagnostic
                    .getCertificateIssuerDN(diagnostic.getSignatureById(signatureId).getSigningCertificate().getId()));
            var signatureType = isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId) : null;
            var timestamps = simple.getSignatureTimestamps(signatureId);

            var timestampQualifications = new ArrayList<TimestampQualification>();
            for (var timestampId : reports.getSimpleReport().getSignatureTimestamps(signatureId))
                timestampQualifications.add(reports.getDetailedReport().getTimestampQualification(timestampId.getId()));

            signaturesBox.getChildren().add(createSignatureBox(isValidated, isValid, name, signingTime, subject, issuer,
                    signatureType, timestampQualifications, createTimestampsBox(isValidated, timestamps, simple, diagnostic, e -> {
                        getNodeForLoosingFocus().requestFocus();
                    })));
        }
    }
}
