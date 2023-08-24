package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import digital.slovensko.autogram.core.SignatureValidator;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class SignaturesController implements SuppressedFocusController {
    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
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
            var subject = getPrettyDNWithoutCN(diagnostic.getSignatureById(signatureId).getSigningCertificate().getCertificateDN());
            var issuer = getPrettyDN(diagnostic.getCertificateIssuerDN(diagnostic.getSignatureById(signatureId).getSigningCertificate().getId()));
            var signatureType = isValidated
                    ? reports.getDetailedReport().getSignatureQualification(signatureId).getLabel()
                    : null;
            var timestamps = simple.getSignatureTimestamps(signatureId);

            signaturesBox.getChildren().add(createSignatureBox(isValidated, isValid, name, signingTime, subject, issuer,
                    signatureType, createTimestampsBox(timestamps, simple, diagnostic)));
        }
    }

    private VBox createSignatureBox(boolean isValidated, boolean isValid, String name, String signingTime,
            String subjectStr, String issuerStr, String signatureType, VBox timestamps) {
        var nameText = new Text(name);
        nameText.getStyleClass().add("autogram-heading-s");
        var nameFlow = new TextFlow(nameText);
        nameFlow.getStyleClass().add("autogram-summary-header__title");
        nameFlow.setPrefWidth(272);

        var validText = new Text();
        var validBox = new VBox(validText);
        validBox.getStyleClass().add("autogram-tag");
        validText.getStyleClass().add("autogram-heading-s");
        var validFlow = new TextFlow(validBox);
        validFlow.getStyleClass().add("autogram-summary-header__badge");

        if (!isValidated) {
            validText.setText("Prebieha overovanie...");
            validText.getStyleClass().add("autogram-tag-processing--text");
            validBox.getStyleClass().add("autogram-tag-processing");

        } else {
            if (isValid) {
                validText.setText("Platný");
                validText.getStyleClass().add("autogram-tag-valid--text");
                validBox.getStyleClass().add("autogram-tag-valid");

            } else {
                validText.setText("Neplatný");
                validText.getStyleClass().add("autogram-tag-invalid--text");
                validBox.getStyleClass().add("autogram-tag-invalid");
            }
        }

        var nameBox = new HBox(nameFlow, validFlow);
        nameBox.getStyleClass().add("autogram-summary-header");

        var signatureDetailsBox = new VBox(
                createTableRow("Čas podpisu", signingTime),
                createTableRow("Certifikát", subjectStr),
                createTableRow("Vydavateľ", issuerStr),
                createTableRow("Typ podpisu", signatureType),
                createTableRow("Časové pečiatky", timestamps, true));
        signatureDetailsBox.getStyleClass().add("autogram-signature-details-box");

        var signatureBox = new VBox(nameBox, signatureDetailsBox);
        signatureBox.getStyleClass().add("autogram-signature-box");
        return signatureBox;
    }

    private boolean isQTSA(DiagnosticData diagnostic, SimpleReport simple, String signatureId) {
        var signature = diagnostic.getSignatureById(signatureId);
        for (var timestamp : signature.getSignatureTimestamps()) {
            if (simple.getTimestampQualification(timestamp.getId()).getReadable().equals("QTSA"))
                return true;
        }

        return false;
    }

    private HBox createTableRow(String label, String value) {
        return createTableRow(label, value, false);
    }

    private HBox createTableRow(String label, VBox valueNode, boolean isLast) {
        var labelNode = createTableCell(label, "autogram-heading-s", isLast);
        labelNode.setPrefWidth(280);
        valueNode.setPrefWidth(384);

        return new HBox(labelNode, valueNode);
    }

    private HBox createTableRow(String label, String value, boolean isLast) {
        var labelNode = createTableCell(label, "autogram-heading-s", isLast);
        var valueNode = createTableCell(value, "autogram-body", isLast);

        labelNode.setPrefWidth(280);
        valueNode.setPrefWidth(384);

        return new HBox(labelNode, valueNode);
    }

    private TextFlow createTableCell(String value) {
        return createTableCell(value, "autogram-body", false);
    }

    private TextFlow createTableCell(String value, String textStyle, boolean isLast) {
        var text = new Text(value);
        text.getStyleClass().add(textStyle);

        var cell = new TextFlow(text);
        cell.getStyleClass().addAll(isLast ? "autogram-table-cell--last" : "autogram-table-cell");

        return cell;
    }

    private VBox createTimestampsBox(List<XmlTimestamp> timestamps, SimpleReport simple, DiagnosticData diagnostic) {
        var vBox =  new VBox();

        for (var timestamp : timestamps) {
            var productionTime = new TextFlow(new Text(format.format(timestamp.getProductionTime())));
            var subject = new TextFlow(new Text(getPrettyDNWithoutCN(diagnostic.getCertificateDN(diagnostic.getTimestampSigningCertificateId(timestamp.getId())))));
            var qualification = new TextFlow(new Text(simple.getTimestampQualification(timestamp.getId()).getLabel()));

            productionTime.getStyleClass().add("autogram-body-details");
            subject.getStyleClass().add("autogram-body-details");
            qualification.getStyleClass().add("autogram-body-details");

            var timestampDetailsBox = new VBox(productionTime, subject, qualification);
            timestampDetailsBox.getStyleClass().add("autogram-timestamp-details-box");
            timestampDetailsBox.setVisible(false);

            var polygon = new Polygon(0.0, 0.0, 9.0, 6.0, 0.0, 12.0);
            polygon.setFill(Paint.valueOf("#1d70b8"));
            var graphic = new TextFlow(polygon);
            var button = new Button(timestamp.getProducedBy(), graphic);
            button.getStyleClass().addAll("autogram-link", "autogram-details__more");
            var textFlow = new TextFlow(button);
            var timestampDetailsVBoxWrapper = new VBox();
            vBox.getChildren().add(new VBox(textFlow, timestampDetailsVBoxWrapper));
            timestampDetailsBox.setVisible(false);

            button.setOnAction(e -> {
                if (timestampDetailsBox.isVisible()) {
                    timestampDetailsBox.setVisible(false);
                    timestampDetailsVBoxWrapper.getChildren().remove(timestampDetailsBox);
                    getNodeForLoosingFocus().requestFocus();
                } else {
                    timestampDetailsVBoxWrapper.getChildren().add(timestampDetailsBox);
                    timestampDetailsBox.setVisible(true);
                    getNodeForLoosingFocus().requestFocus();
                }
            });
        }

        return vBox;
    }

    private static String getPrettyDNWithoutCN(String s) {
        return String.join("\n", new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
            .replaceFirst("(\nCN=.*$|CN=.*\n)", "");
    }

    private static String getPrettyDN(String s) {
        return String.join("\n", new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1));
    }
}
