package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    @FXML
    TableView signaturesTable;

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

        var rows = new MyTableRow[rep.getSignatureIdList().size()];
        var format = new SimpleDateFormat("HH:mm:ss\ndd.MM.yyyy");

        for (var signatureId : rep.getSignatureIdList()) {
            var index = rep.getSignatureIdList().indexOf(signatureId);

            var subjectPrincipal = docValidator.getSignatureById(signatureId).getCertificates().get(0).getCertificate().getSubjectX500Principal();
            var name = subjectPrincipal.getName("RFC1779").replace(',', '\n');

            rows[index] = new MyTableRow(
                    name,
                    format.format(rep.getSigningTime(signatureId)),
                    Integer.toString(rep.getSignatureTimestamps(signatureId).size()),
                    docValidator.getSignatureById(signatureId).getCertificates().get(0).getIssuerX500Principal().getName("RFC1779").replace(',', '\n')
            );
        }

        var subjectColumn = new TableColumn("Subjekt");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        var signingTimeColumn = new TableColumn("Čas podpisu");
        signingTimeColumn.setCellValueFactory(new PropertyValueFactory<>("signingTime"));
        var timestampCountColumn = new TableColumn("Časové pečiatky");
        timestampCountColumn.setCellValueFactory(new PropertyValueFactory<>("timestampCount"));
        var issuerColumn = new TableColumn("Vydavateľ");
        issuerColumn.setCellValueFactory(new PropertyValueFactory<>("issuer"));

        subjectColumn.setStyle("-fx-alignment: CENTER; -fx-table-cell-border-color: #000000; -fx-table-cell-border-width: 1px; -fx-table-cell-border-style: solid; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-style: solid; -fx-padding: 5px;");
        signingTimeColumn.setStyle("-fx-alignment: CENTER; -fx-table-cell-border-color: #000000; -fx-table-cell-border-width: 1px; -fx-table-cell-border-style: solid; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-style: solid; -fx-padding: 5px;");
        timestampCountColumn.setStyle("-fx-alignment: CENTER; -fx-table-cell-border-color: #000000; -fx-table-cell-border-width: 1px; -fx-table-cell-border-style: solid; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-style: solid; -fx-padding: 5px;");
        issuerColumn.setStyle("-fx-alignment: CENTER; -fx-table-cell-border-color: #000000; -fx-table-cell-border-width: 1px; -fx-table-cell-border-style: solid; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-style: solid; -fx-padding: 5px;");

        signaturesTable.getColumns().addAll(subjectColumn, signingTimeColumn, timestampCountColumn, issuerColumn);
        signaturesTable.getItems().addAll(rows);
        signaturesTable.setStyle("-fx-border-color: #000000; -fx-border-width: 1px; -fx-border-style: solid; -fx-alignment: CENTER; -fx-table-cell-border-color: #000000; -fx-table-cell-border-width: 1px; -fx-table-cell-border-style: solid;");
    }

    public class MyTableRow {
        String subject;
        String signingTime;
        String timestampCount;
        String issuer;

        public MyTableRow(String subject, String signingTime, String timestampCount, String issuer) {
            this.subject = subject;
            this.signingTime = signingTime;
            this.timestampCount = timestampCount;
            this.issuer = issuer;
        }

        public String getSubject() {
            return subject;
        }

        public String getSigningTime() {
            return signingTime;
        }

        public String getTimestampCount() {
            return timestampCount;
        }

        public String getIssuer() {
            return issuer;
        }
    }
}
