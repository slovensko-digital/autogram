package digital.slovensko.autogram.ui.gui;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

import javax.security.auth.x500.X500Principal;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class GUIValidationUtils {
    public static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public static Node createWarningText(String message) {
        var warningText = new Text(message);
        warningText.getStyleClass().add("autogram-heading-s");
        var warningTextFlow = new TextFlow(warningText);
        warningTextFlow.getStyleClass().add("autogram-warning-textflow");

        return warningTextFlow;
    }

    public static GridPane createSignatureTableRows(Reports reports, boolean isValidated, Consumer<String> callback, int maxRows) {
        var table = new GridPane();
        table.getStyleClass().add("autogram-signatures-table");

        var headerText = new TextFlow(new Text("Podpisy na dokumente"));
        headerText.getStyleClass().addAll("autogram-heading-s", "autogram-signatures-table-cell--left");
        var headerLink = new TextFlow(createSignatureTableLink(callback));
        table.addRow(0, headerText, headerLink);

        var leftConstraints = new ColumnConstraints();
        leftConstraints.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        var rightConstraints = new ColumnConstraints();
        rightConstraints.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        table.getColumnConstraints().addAll(leftConstraints, rightConstraints);

        var signatures = reports.getSimpleReport().getSignatureIdList();
        var totalSignatures = signatures.size();
        if (totalSignatures > maxRows)
            signatures = signatures.subList(0, maxRows - 1);

        for (var signatureId : signatures) {
            var subject = new HBox(new TextFlow(new Text(reports.getSimpleReport().getSignedBy(signatureId))));
            subject.getStyleClass().add("autogram-signatures-table-cell--left");
            var type = new HBox(
                    SignatureBadgeFactory.createCombinedBadgeFromQualification(
                            isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId) : null,
                            reports, signatureId, 0));
            table.addRow(table.getChildren().size(), subject, type);
        }

        if (totalSignatures > maxRows) {
            var label = new Text("Na dokumente " + createRemainingSignaturesCountString(totalSignatures - maxRows + 1) + ". ");

            var button = new Button("Zobraziť všetky podpisy");
            button.getStyleClass().addAll("autogram-link");
            button.wrapTextProperty().setValue(true);
            button.setOnMouseClicked(event -> callback.accept(null));

            var flow = new TextFlow(label, button);
            flow.getStyleClass().addAll("autogram-body", "autogram-font-weight-bold");
            table.add(flow, 0, maxRows * 2 - 1, 2, 1);
        }

        return table;
    }

    private static String createRemainingSignaturesCountString(int i) {
        return switch (i) {
            case 1 -> "1 podpis";
            case 2, 3, 4 -> "sú ďalšie " + i + " podpisy";
            default -> "je ďalších " + i + " podpisov";
        };
    }

    public static Button createSignatureTableLink(Consumer<String> callback) {
        var whoSignedButton = new Button("Zobraziť detail podpisov");
        whoSignedButton.getStyleClass().addAll("autogram-link");
        whoSignedButton.wrapTextProperty().setValue(true);
        whoSignedButton.setOnMouseClicked(event -> {
            callback.accept(null);
        });

        return whoSignedButton;
    }

    public static VBox createSignatureBox(Reports reports, boolean isValidated, String signatureId,
                                          Consumer<String> callback, boolean areTLsLoaded) {
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
        var signatureQualification = isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId)
                : null;
        var timestamps = simple.getSignatureTimestamps(signatureId);

        var nameFlow = new TextFlow(new Text(name));
        nameFlow.getStyleClass().add("autogram-summary-header__title");
        var errors = reports.getSimpleReport().getAdESValidationErrors(signatureId);
        var isRevocationValidated = true;
        for (var error : errors)
            if (error.getValue().contains("No revocation data found for the certificate"))
                isRevocationValidated = false;

        var isTimestampInvalid = false;
        for (var timestamp : timestamps)
            if (timestamp.getIndication().equals(Indication.FAILED))
                isTimestampInvalid = true;

        Node badge = null;
        if (!isValidated)
            badge = SignatureBadgeFactory.createInProgressBadge();
        else if (isFailed)
            badge = SignatureBadgeFactory.createInvalidBadge("Neplatný podpis");
        else
            badge = SignatureBadgeFactory.createCombinedBadgeFromQualification(
                    isValidated ? signatureQualification : null, reports, signatureId, 300);

        var validFlow = new HBox(badge);
        validFlow.getStyleClass().add("autogram-summary-header__badge");
        var nameBox = new HBox(nameFlow, validFlow);

        var signatureDetailsBox = new VBox(
                createTableRow("Výsledok overenia",
                        validityToString(isValid, isFailed, areTLsLoaded, isRevocationValidated, signatureQualification,
                                isTimestampInvalid)),
                createTableRow("Certifikát", subject),
                createTableRow("Vydavateľ", issuer),
                createTableRow("Negarantovaný čas podpisu", signingTime));

        var timestampsBox = createTimestampsBox(isValidated, timestamps, simple, diagnostic, e -> {
            callback.accept(null);
        });
        if (!timestampsBox.getChildren().isEmpty()) {
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu",
                    SignatureBadgeFactory.createBadgeFromQualification(signatureQualification), false));
            signatureDetailsBox.getChildren().add(createTableRow("Časové pečiatky", timestampsBox, true));
        } else
            signatureDetailsBox.getChildren().add(createTableRow("Typ podpisu",
                    SignatureBadgeFactory.createBadgeFromQualification(signatureQualification), true));

        var signatureBox = new VBox(nameBox, signatureDetailsBox);
        signatureBox.getStyleClass().add("autogram-signature-box");
        return signatureBox;
    }

    private static String validityToString(boolean isValid, boolean isFailed, boolean areTLsLoaded,
                                           boolean isRevocationValidated, SignatureQualification signatureQualification, boolean isTimestampInvalid) {
        if (isFailed || isTimestampInvalid)
            return "Neplatný";

        if (!areTLsLoaded)
            return "Nepodarilo sa overiť";

        if (!isRevocationValidated)
            return "Nepodarilo sa overiť platnosť certifikátu";

        if (isValid)
            return "Platný";

        if (signatureQualification.getReadable().contains("INDETERMINATE"))
            return "Predbežne platný";

        return "Neznámy podpis";
    }

    public static HBox createTableRow(String label, String value) {
        return createTableRow(label, value, false);
    }

    public static HBox createTableRow(String label, Node valueNode, boolean isLast) {
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
