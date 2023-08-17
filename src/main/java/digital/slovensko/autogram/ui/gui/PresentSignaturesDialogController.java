package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;

import javax.security.auth.x500.X500Principal;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PresentSignaturesDialogController implements SuppressedFocusController {
    private final SigningJob signingJob;
    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

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

    public PresentSignaturesDialogController(SigningJob signingJob) {
        this.signingJob = signingJob;
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public void onSignatureValidationCompleted() {
        signatureValidationMessage.setText("");
        signatureValidationMessage.setVisible(false);
        signatureDetailsButton.setVisible(true);

        showSignatures();
    }

    public void onSignatureDetailsButtonAction() {
        var controller = new SignatureDetailsController(signingJob);
        var root = GUIUtils.loadFXML(controller, "signature-details.fxml");

        var stage = new Stage();
        stage.setTitle("Detaily podpisov");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        controller.showHTMLReport();
        stage.show();
    }

    public void showSignatures() {
        if (signingJob.getSignatureValidationReport() != null)
            renderSignatures(signingJob.getSignatureValidationReport(), true);

        else
            renderSignatures(signingJob.getSignatureCheckReport(), false);
    }

    public void renderSignatures(Reports reports, boolean isValidated) {
        signaturesBox.getChildren().clear();

        var simple = reports.getSimpleReport();
        var diagnostic = reports.getDiagnosticData();

        for (var signatureId : diagnostic.getSignatureIdList()) {
            var signatureType = isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId).getLabel() : "Prebieha overovanie...";
            var subject = new X500Principal(diagnostic.getSignatureById(signatureId).getSigningCertificate().getCertificateDN()).getName(X500Principal.RFC1779);
            var name = simple.getSignedBy(signatureId);
            var subjectStr = subject.replace(", ", "\n");
            var signingTime = format.format(simple.getSigningTime(signatureId));
            var timestampCount = Integer.toString(simple.getSignatureTimestamps(signatureId).size()) + (isQTSA(diagnostic, simple, signatureId) ? " (QTSA)" : "");
            var isValid = isValidated && simple.isValid(signatureId);

            signaturesBox.getChildren().add(createSignatureBox(isValid, isValidated, subjectStr, signatureType, name, signingTime, timestampCount));

            System.out.println("Issuer DN: " + new X500Principal(diagnostic.getSignatureById(signatureId).getSigningCertificate().getCertificateIssuerDN()).getName(X500Principal.RFC1779));
        }
    }

    private VBox createSignatureBox(boolean isValidated, boolean isValid, String subjectStr, String signatureType, String name, String signingTime, String timestampCount) {
        var nameText = new Text(name);
        nameText.getStyleClass().add("autogram-heading-m");
        var nameFlow = new TextFlow(nameText);
        nameFlow.getStyleClass().add("autogram-summary-header__title");
        nameFlow.setPrefWidth(248);

        var validText = new Text("Prebieha overovanie...");
        var validFlow = new TextFlow();
        validFlow.getStyleClass().add("autogram-summary-header__basge");

        if (!isValidated) {
            validFlow.getChildren().add(validText);
            validText.getStyleClass().add("autogram-body");

        } else {
            validText.getStyleClass().add("autogram-heading-s");
            var validBox = new VBox();
            validBox.getChildren().add(validText);
            validBox.getStyleClass().add("autogram-tag");

            if (isValid) {
                validText.setText("Platný");
                validText.getStyleClass().add("autogram-tag-valid--text");
                validBox.getStyleClass().add("autogram-tag-valid");

            } else {
                validText.setText("Neplatný");
                validText.getStyleClass().add("autogram-tag-invalid--text");
                validBox.getStyleClass().add("autogram-tag-invalid");
            }

            validFlow.getChildren().add(validBox);
        }

        var nameBox = new HBox(nameFlow, validFlow);
        nameBox.getStyleClass().add("autogram-summary-header");

        var signatureDetailsBox = new VBox(
            createTableRow("Certifikát", subjectStr),
            createTableRow("Typ podpisu", signatureType),
            createTableRow("Čas", signingTime),
            createTableRow("Časové pečiatky", timestampCount, true)
            );
            signatureDetailsBox.getStyleClass().add("autogram-signature-details-box");

        var signatureBox = new VBox(nameBox, signatureDetailsBox);
        signatureBox.setStyle("-fx-border-color: -autogram-border-colour; -fx-border-width: 1px;");

        return signatureBox;
    }

    private boolean isQTSA(DiagnosticData diagnostic, SimpleReport simple, String signatureId) {
        var signature = diagnostic.getSignatureById(signatureId);
        for (var timestamp : signature.getSignatureTimestamps()) {
            System.out.println("Timestamp DN: " + new X500Principal(timestamp.getSigningCertificate().getCertificateDN()).getName(X500Principal.RFC1779));

            if (simple.getTimestampQualification(timestamp.getId()).getReadable().equals("QTSA"))
                return true;
        }

        return false;
    }

    private HBox createTableRow(String label, String value) {
        return createTableRow(label, value, false);
    }

    private HBox createTableRow(String label, String value, boolean isLast) {
        var labelNode = createTableCell(label, "autogram-heading-s", isLast);
        var valueNode = createTableCell(value, "autogram-body", isLast);

        labelNode.setPrefWidth(248);
        valueNode.setPrefWidth(400);

        return new HBox(labelNode, valueNode);
    }

    private TextFlow createTableCell(String value, String textStyle, boolean isLast) {
        var text = new Text(value);
        text.getStyleClass().add(textStyle);

        var cell = new TextFlow(text);
        cell.getStyleClass().addAll(isLast ? "autogram-table-cell--last" : "autogram-table-cell");

        return cell;
    }
}
