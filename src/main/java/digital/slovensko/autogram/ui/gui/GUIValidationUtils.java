package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.security.auth.x500.X500Principal;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
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
        var whoSigned = new TextFlow(new Text("Podpísal"));
        whoSigned.getStyleClass().add("autogram-table__left-column");
        var signatureType = new TextFlow(new Text("Typ podpisu"));

        var r = new HBox(whoSigned, signatureType);
        r.getStyleClass().add("autogram-table-header");
        return r;
    }

    public static VBox createSignatureTableRows(Reports reports, boolean isValidated, Consumer<String> callback) {
        var r = new VBox();
        for (var signatureId : reports.getSimpleReport().getSignatureIdList()) {
            var name = reports.getSimpleReport().getSignedBy(signatureId);
            var signatureType = reports.getDetailedReport().getSignatureQualification(signatureId);
            var valid = reports.getSimpleReport().isValid(signatureId);

            var timestampQualifications = new ArrayList<TimestampQualification>();
            for (var timestampId : reports.getSimpleReport().getSignatureTimestamps(signatureId))
                timestampQualifications.add(reports.getDetailedReport().getTimestampQualification(timestampId.getId()));

            r.getChildren().add(createSignatureTableRow(name, signatureType, timestampQualifications, isValidated, valid, callback));
        }

        return r;
    }

    public static HBox createSignatureTableRow(String name, SignatureQualification signatureType, ArrayList<TimestampQualification> timestampQualifications, boolean isValidated, boolean valid, Consumer<String> callback) {
        var whoSignedButton = new Button(name);
        whoSignedButton.getStyleClass().addAll("autogram-link", "autogram-table__left-column");
        whoSignedButton.setOnMouseClicked(event -> {
            callback.accept(null);
        });

        var signatureTypeBox = SignatureBadgeFactory.createCombinedBadgeFromQualification(isValidated ? signatureType : null, timestampQualifications);
        if (isValidated && !valid)
            signatureTypeBox = SignatureBadgeFactory.createInvalidBadge("Neplatný podpis");

        var r = new HBox(new HBox(whoSignedButton), new VBox(signatureTypeBox));
        r.getStyleClass().add("autogram-table-cell");
        return r;
    }

    public static VBox createSignatureBox(boolean isValidated, boolean isValid, String name, String signingTime,
            String subjectStr, String issuerStr, SignatureQualification signatureQualification, VBox timestamps) {

        var nameFlow = new TextFlow(new Text(name));
        nameFlow.getStyleClass().add("autogram-summary-header__title");

        VBox badge = null;
        if (!isValidated)
            badge = SignatureBadgeFactory.createInProgressBadge();
        else
            if (isValid)
                badge = SignatureBadgeFactory.createBadgeFromQualification(signatureQualification);
            else
                badge = SignatureBadgeFactory.createInvalidBadge("Neplatný podpis");

        var validFlow = new TextFlow(badge);
        validFlow.getStyleClass().add("autogram-summary-header__badge");
        var nameBox = new HBox(nameFlow, validFlow);

        var signatureDetailsBox = new VBox(
                createTableRow("Čas podpisu", signingTime),
                createTableRow("Certifikát", subjectStr),
                createTableRow("Vydavateľ", issuerStr)
        );

        if (timestamps.getChildren().size() > 0) {
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu", SignatureBadgeFactory.createBadgeFromQualification(signatureQualification), false));
            signatureDetailsBox.getChildren().add(createTableRow("Časové pečiatky", timestamps, true));
        } else
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu", SignatureBadgeFactory.createBadgeFromQualification(signatureQualification), true));

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

    public static VBox createTimestampsBox(boolean isValidated, List<XmlTimestamp> timestamps, SimpleReport simple, DiagnosticData diagnostic, Consumer<String> callback) {
        var vBox =  new VBox();
        vBox.getStyleClass().add("autogram-timestamps-box");

        for (var timestamp : timestamps) {
            var productionTime = new TextFlow(new Text(format.format(timestamp.getProductionTime())));
            var subject = new TextFlow(new Text(getPrettyDNWithoutCN(diagnostic.getCertificateDN(diagnostic.getTimestampSigningCertificateId(timestamp.getId())))));
            var timestampQualification = isValidated ? simple.getTimestampQualification(timestamp.getId()) : null;
            var timestampDetailsBox = new VBox(productionTime, subject, new TextFlow(SignatureBadgeFactory.createBadgeFromTSQualification(timestampQualification)));
            timestampDetailsBox.setVisible(false);

            var button = new Button(timestamp.getProducedBy(), new TextFlow(new Polygon(0.0, 0.0, 9.0, 6.0, 0.0, 12.0)));
            button.getStyleClass().addAll("autogram-link");
            var timestampDetailsVBoxWrapper = new VBox();
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
        return String.join("\n", new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
            .replaceFirst("(\nCN=.*$|CN=.*\n)", "");
    }

    public static String getPrettyDN(String s) {
        return String.join("\n", new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1));
    }
}
