package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;

import javax.security.auth.x500.X500Principal;

import digital.slovensko.autogram.core.SigningJob;
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
        stage.initModality(Modality.APPLICATION_MODAL);
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

        var s = reports.getSimpleReport();

        var format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        for (var signatureId : s.getSignatureIdList()) {
            var subject = new X500Principal(reports.getDiagnosticData().getSignatureById(signatureId).getSigningCertificate().getCertificateDN()).getName(X500Principal.RFC1779);
            var name = s.getSignedBy(signatureId);
            var subjectStr = subject.replace(", ", "\n");
            var signingTime = format.format(s.getSigningTime(signatureId));
            var timestampCount = Integer.toString(s.getSignatureTimestamps(signatureId).size());

            var signatureBox = new VBox();
            signatureBox.setStyle("-fx-border-color: -autogram-border-colour; -fx-border-width: 1px;");

            var nameText = new Text(name);
            nameText.getStyleClass().add("autogram-heading-m");
            var nameFlow = new TextFlow(nameText);
            nameFlow.setStyle("-fx-padding: 1.25em 1.25em;");
            nameFlow.setPrefWidth(248);

            var signatureType = isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId).getLabel() : "Prebieha overovanie...";

            var validText = new Text("Prebieha overovanie...");
            var validFlow = new TextFlow();
            validFlow.setStyle("-fx-padding: 1.1875em 1.25em;");

            if (!isValidated) {
                validFlow.getChildren().add(validText);
                validText.getStyleClass().add("autogram-body");

            } else {
                validText.getStyleClass().add("autogram-heading-s");
                var validBox = new VBox();
                validBox.getChildren().add(validText);
                validBox.getStyleClass().add("autogram-tag");

                if (s.isValid(signatureId)) {
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

            var nameBox = new HBox();
            nameBox.getChildren().addAll(nameFlow, validFlow);
            nameBox.setStyle("-fx-background-color: #f3f2f1;");
            signatureBox.getChildren().add(nameBox);

            var signatureDetailsBox = new VBox();
            signatureDetailsBox.setStyle("-fx-padding: 0.5em 1.25em 1.25em 1.25em;");
            signatureBox.getChildren().add(signatureDetailsBox);
            signatureDetailsBox.getChildren().add(createTableRow("Certifikát", subjectStr));
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu", signatureType));
            signatureDetailsBox.getChildren().add(createTableRow("Čas", signingTime));
            signatureDetailsBox.getChildren().add(createTableRow("Časové pečiatky", timestampCount, true));

            signaturesBox.getChildren().add(signatureBox);
        }
    }

    private HBox createTableRow(String label, String value) {
        return createTableRow(label, value, false);
    }

    private HBox createTableRow(String label, String value, boolean isLast) {
        var row = new HBox();
        var labelNode = createTableCell(label, "autogram-heading-s", isLast);
        var valueNode = createTableCell(value, "autogram-body", isLast);

        labelNode.setPrefWidth(248);
        valueNode.setPrefWidth(400);

        row.getChildren().addAll(labelNode, valueNode);

        return row;
    }

    private TextFlow createTableCell(String value, String textStyle, boolean isLast) {
        var cell = new TextFlow();
        cell.getStyleClass().addAll(isLast ? "autogram-table-cell--last" : "autogram-table-cell");
        var text = new Text(value);
        text.getStyleClass().add(textStyle);
        cell.getChildren().add(text);
        return cell;
    }
}
