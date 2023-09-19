package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.security.auth.x500.X500Principal;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class GUIValidationUtils {
    public static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public static HBox createSignatureTableHeader(boolean isValidated) {
        var whoSigned = new TextFlow(new Text("Podpisy na dokumente"));
        var r = new HBox(whoSigned);
        r.getStyleClass().add("autogram-table-header");
        return r;
    }

    public static VBox createSignatureTableRows(Reports reports, boolean isValidated, Consumer<String> callback) {

        var r = new VBox();
        for (var signatureId : reports.getSimpleReport().getSignatureIdList()) {
            var name = reports.getSimpleReport().getSignedBy(signatureId);
            var signatureType = reports.getDetailedReport().getSignatureQualification(signatureId);
            var isFailed = reports.getDetailedReport().getBasicValidationIndication(signatureId)
                    .equals(Indication.FAILED);

            var timestampQualifications = new ArrayList<TimestampQualification>();
            for (var timestampId : reports.getSimpleReport().getSignatureTimestamps(signatureId))
                timestampQualifications.add(reports.getDetailedReport().getTimestampQualification(timestampId.getId()));

            r.getChildren().add(createSignatureTableRow(name, signatureType, timestampQualifications, isValidated,
                    isFailed, callback));
        }

        return r;
    }

    public static HBox createSignatureTableRow(String name, SignatureQualification signatureType,
            ArrayList<TimestampQualification> timestampQualifications, boolean isValidated,
            boolean isFailed, Consumer<String> callback) {
        var whoSignedButton = new Button(name);
        whoSignedButton.getStyleClass().addAll("autogram-link", "autogram-table__left-column");
        whoSignedButton.wrapTextProperty().setValue(true);
        whoSignedButton.setOnMouseClicked(event -> {
            callback.accept(null);
        });

        var signatureTypeBox = SignatureBadgeFactory
                .createCombinedBadgeFromQualification(isValidated ? signatureType : null, timestampQualifications);
        if (isValidated && isFailed)
            signatureTypeBox = SignatureBadgeFactory.createInvalidBadge("Neplatný podpis");

        var r = new HBox(new HBox(whoSignedButton), new VBox(signatureTypeBox));
        r.getStyleClass().add("autogram-table-cell");
        return r;
    }

    public static VBox createSignatureBox(Reports reports, boolean isValidated, String signatureId,
            Consumer<String> callback) {
        var simple = reports.getSimpleReport();
        var diagnostic = reports.getDiagnosticData();

        var isValid = simple.isValid(signatureId);
        var isFailed = reports.getDetailedReport().getBasicValidationIndication(signatureId).equals(Indication.FAILED);
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

        return createSignatureBox(isValidated, isValid, isFailed, name, signingTime, subject, issuer,
                signatureType, timestampQualifications,
                createTimestampsBox(isValidated, timestamps, simple, diagnostic, e -> {
                    callback.accept(null);
                }));
    }

    public static VBox createSignatureBox(boolean isValidated, boolean isValid, boolean isFailed, String name,
            String signingTime,
            String subjectStr, String issuerStr, SignatureQualification signatureQualification,
            ArrayList<TimestampQualification> timestampQualifications, VBox timestamps) {

        var nameFlow = new TextFlow(new Text(name));
        nameFlow.getStyleClass().add("autogram-summary-header__title");

        VBox badge = null;
        if (!isValidated)
            badge = SignatureBadgeFactory.createInProgressBadge();
        else if (isFailed)
            badge = SignatureBadgeFactory.createInvalidBadge("Neplatný podpis");
        else
            badge = SignatureBadgeFactory.createCombinedBadgeFromQualification(
                    isValidated ? signatureQualification : null, timestampQualifications);

        var validFlow = new TextFlow(badge);
        validFlow.getStyleClass().add("autogram-summary-header__badge");
        var nameBox = new HBox(nameFlow, validFlow);

        var signatureDetailsBox = new VBox(
                createTableRow("Negarantovaný čas podpisu", signingTime),
                createTableRow("Certifikát", subjectStr),
                createTableRow("Vydavateľ", issuerStr));

        if (timestamps.getChildren().size() > 0) {
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu",
                    SignatureBadgeFactory.createBadgeFromQualification(signatureQualification), false));
            signatureDetailsBox.getChildren().add(createTableRow("Časové pečiatky", timestamps, true));
        } else
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu",
                    SignatureBadgeFactory.createBadgeFromQualification(signatureQualification), true));

        var signatureBox = new VBox(nameBox, signatureDetailsBox);
        signatureBox.getStyleClass().add("autogram-signature-box");
        return signatureBox;
    }

    public static HBox createTableRow(String label, String value) {
        return createTableRow(label, value, false);
    }

    public static HBox createTableRow(String label, VBox valueNode, boolean isLast) {
        var labelNode = createTableCell(label, "autogram-heading-s", isLast, true);
        var cell = new TextFlow(valueNode);
        cell.getStyleClass().addAll(isLast ? "autogram-table-cell--last" : "autogram-table-cell");
        cell.getStyleClass().addAll("autogram-table-cell--right");

        return new HBox(labelNode, cell);
    }

    public static HBox createTableRow(String label, String value, boolean isLast) {
        var labelNode = createTableCell(label, "autogram-heading-s", isLast, true);
        var valueNode = createTableCell(value, "autogram-body", isLast, false);

        return new HBox(labelNode, valueNode);
    }

    public static TextFlow createTableCell(String value, String textStyle, boolean isLast, boolean isLeft) {
        var text = new Text(value);
        text.getStyleClass().add(textStyle);

        var cell = new TextFlow(text);
        cell.getStyleClass().addAll(isLast ? "autogram-table-cell--last" : "autogram-table-cell");
        cell.getStyleClass().addAll(isLeft ? "autogram-table-cell--left" : "autogram-table-cell--right");

        return cell;
    }

    public static VBox createTimestampsBox(boolean isValidated, List<XmlTimestamp> timestamps, SimpleReport simple,
            DiagnosticData diagnostic, Consumer<String> callback) {
        var vBox = new VBox();
        vBox.getStyleClass().add("autogram-timestamps-box");

        for (var timestamp : timestamps) {
            var isFailed = timestamp.getIndication().equals(Indication.FAILED);
            var subject = new TextFlow(new Text(getPrettyDN(
                    diagnostic.getCertificateDN(diagnostic.getTimestampSigningCertificateId(timestamp.getId())))));
            var timestampQualification = isValidated ? simple.getTimestampQualification(timestamp.getId()) : null;
            var qualificationBadge = new TextFlow(
                    SignatureBadgeFactory.createBadgeFromTSQualification(isFailed, timestampQualification));
            var timestampDetailsBox = new VBox(subject, qualificationBadge);

            var button = new Button(
                    format.format(timestamp.getProductionTime()),
                    new TextFlow(new Polygon(0.0, 0.0, 9.0, 6.0, 0.0, 12.0)));

            button.getStyleClass().addAll("autogram-link");
            var timestampDetailsVBoxWrapper = new VBox();
            timestampDetailsVBoxWrapper.setVisible(true);
            timestampDetailsVBoxWrapper.getChildren().add(timestampDetailsBox);
            vBox.getChildren().add(new VBox(new TextFlow(button), timestampDetailsVBoxWrapper));

            button.setOnAction(e -> {
                if (timestampDetailsBox.isVisible()) {
                    timestampDetailsBox.setVisible(false);
                    timestampDetailsVBoxWrapper.getChildren().remove(timestampDetailsBox);
                    callback.accept(null);
                } else {
                    timestampDetailsVBoxWrapper.getChildren().add(timestampDetailsBox);
                    timestampDetailsBox.setVisible(true);
                    callback.accept(null);
                }
            });
        }

        return vBox;
    }

    public static String getPrettyDNWithoutCN(String s) {
        return String
                .join("\n",
                        new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",
                                -1))
                .replaceFirst("(\nCN=.*$|CN=.*\n)", "");
    }

    public static String getPrettyDN(String s) {
        return String.join("\n",
                new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1));
    }
}
