package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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
        var rep = signingJob.getSignatureCheckReport().getSimpleReport();
        var docValidator = signingJob.getDocumentValidator();
        docValidator.setCertificateVerifier(new CommonCertificateVerifier());
        var format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        for (var signatureId : rep.getSignatureIdList()) {
            var subjectPrincipal = docValidator.getSignatureById(signatureId).getCertificates().get(0).getCertificate().getSubjectX500Principal();
            var name = rep.getSignedBy(signatureId);
            var subjectStr = subjectPrincipal.getName("RFC1779").replace(", ", "\n");
            var signingTime = format.format(rep.getSigningTime(signatureId));
            var timestampCount = Integer.toString(rep.getSignatureTimestamps(signatureId).size());

            var signatureBox = new VBox();
            signatureBox.setStyle("-fx-border-color: -autogram-border-colour; -fx-border-width: 1px;");

            var nameText = new Text(name);
            nameText.getStyleClass().add("autogram-heading-s");
            var nameFlow = new TextFlow(nameText);
            nameFlow.setStyle("-fx-padding: 1.25em 1.25em;");
            nameFlow.setPrefWidth(248);

            var validText = new Text("Prebieha overovanie platnosti...");
            validText.getStyleClass().add("autogram-body");
            var validFlow = new TextFlow(validText);
            validFlow.setStyle("-fx-padding: 1.25em 1.25em;");

            var nameBox = new HBox();
            nameBox.getChildren().addAll(nameFlow, validFlow);
            nameBox.setStyle("-fx-background-color: #f3f2f1;");
            signatureBox.getChildren().add(nameBox);

            var signatureDetailsBox = new VBox();
            signatureDetailsBox.setStyle("-fx-padding: 0.5em 1.25em 1.25em 1.25em;");
            signatureBox.getChildren().add(signatureDetailsBox);
            signatureDetailsBox.getChildren().add(createTableRow("Certifikát", subjectStr));
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu", "Prebieha overovanie..."));
            signatureDetailsBox.getChildren().add(createTableRow("Čas", signingTime));
            signatureDetailsBox.getChildren().add(createTableRow("Časové pečiatky", timestampCount));

            signaturesBox.getChildren().add(signatureBox);
        }
    }

    private HBox createTableRow(String label, String value) {
        var row = new HBox();
        var labelNode = createTableCell(label, "autogram-heading-s");
        var valueNode = createTableCell(value, "autogram-body");

        labelNode.setPrefWidth(248);
        valueNode.setPrefWidth(400);

        row.getChildren().addAll(labelNode, valueNode);

        return row;
    }

    private TextFlow createTableCell(String value, String textStyle) {
        var cell = new TextFlow();
        cell.getStyleClass().addAll("autogram-table-cell");
        var text = new Text(value);
        text.getStyleClass().add(textStyle);
        cell.getChildren().add(text);
        return cell;
    }
}
