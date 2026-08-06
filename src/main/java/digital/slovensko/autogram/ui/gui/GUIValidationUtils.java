package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.ValidationReports;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.security.auth.x500.X500Principal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static digital.slovensko.autogram.ui.gui.HasI18n.translate;
import static eu.europa.esig.dss.enumerations.SignatureForm.CAdES;
import static eu.europa.esig.dss.enumerations.SignatureForm.PAdES;
import static eu.europa.esig.dss.enumerations.SignatureForm.XAdES;

public class GUIValidationUtils {
    public static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private record DisplayedSignature(ValidationReports.DocumentReport documentReport, String signatureId) {}

    public static Node createWarningText(String message) {
        var text = styled(new Text(message), "autogram-heading-s");
        return styled(new TextFlow(text), "autogram-warning-textflow");
    }

    public static GridPane createSignatureTableRows(ResourceBundle resources, ValidationReports validationReports, boolean isValidated, Consumer<String> callback, int maxRows) {
        var titleKey = validationReports.shouldShowDocumentContext() ? "signature.table.title.documents" : "signature.table.title";
        var headerText = styled(new TextFlow(new Text(translate(resources, titleKey))), "autogram-heading-s", "autogram-signatures-table-cell--left");
        var table = new GridPane();
        table.addRow(0, headerText, new TextFlow(createSignatureTableLink(callback, resources)));
        table.getColumnConstraints().addAll(columnPercent(45), columnPercent(55));

        var signatures = getDisplayedSignatures(validationReports);
        var totalSignatures = validationReports.getTotalSignatureCount();
        if (totalSignatures > maxRows)
            signatures = signatures.subList(0, maxRows - 1);

        int rowIndex = 1;
        for (var signature : signatures) {
            var doc = signature.documentReport();
            var subject = styled(new HBox(new VBox(new TextFlow(new Text(doc.reports().getSimpleReport().getSignedBy(signature.signatureId()))))), "autogram-signatures-table-cell--left");
            var type = styled(new HBox(createSignatureQualificationBadge(resources, doc, isValidated, signature.signatureId(), 0)), "autogram-signature-badges");
            if (doc.hasMultipleContainerDocuments()) {
                type.getChildren().add(doc.signatureCoversAllDocuments(signature.signatureId())
                        ? SignatureBadgeFactory.createValidQualifiedBadge(translate(resources, "signature.scope.allDocuments.label"))
                        : SignatureBadgeFactory.createWarningBadge(translate(resources, "signature.scope.notAllDocuments.label")));
            }
            table.addRow(rowIndex++, subject, type);
        }

        if (totalSignatures > maxRows) {
            var moreTextKey = validationReports.shouldShowDocumentContext() ? "signature.table.more.documents.txt" : "signature.table.more.txt";
            var button = styled(new Button(translate(resources, "signature.table.more.btn")), "autogram-link");
            button.setWrapText(true);
            button.setOnMouseClicked(event -> callback.accept(null));
            var flow = styled(new TextFlow(new Text(translate(resources, moreTextKey, totalSignatures - maxRows + 1)), button), "autogram-body", "autogram-font-weight-bold");
            table.add(flow, 0, rowIndex, 2, 1);
        }

        return table;
    }

    public static Button createSignatureTableLink(Consumer<String> callback, ResourceBundle resources) {
        var button = styled(new Button(translate(resources, "signature.table.show.btn")), "autogram-link");
        button.wrapTextProperty().setValue(true);
        button.setOnMouseClicked(event -> callback.accept(null));
        return button;
    }

    public static VBox createSignatureBox(ResourceBundle resources, ValidationReports.DocumentReport documentReport,
            boolean showDocumentName, boolean isValidated, String signatureId, Consumer<String> callback, boolean areTLsLoaded) {
        var reports = documentReport.reports();
        var simple = reports.getSimpleReport();
        var diagnostic = reports.getDiagnosticData();

        var isValid = simple.isValid(signatureId);
        var isFailed = reports.getDetailedReport().getBasicValidationIndication(signatureId).equals(Indication.FAILED);
        var signingTime = format.format(simple.getSigningTime(signatureId));
        var subject = getPrettyDNWithoutCN(diagnostic.getSignatureById(signatureId).getSigningCertificate().getCertificateDN());
        var issuer = getPrettyDN(diagnostic.getCertificateIssuerDN(diagnostic.getSignatureById(signatureId).getSigningCertificate().getId()));
        var signatureQualification = isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId) : null;
        var signatureForm = simple.getSignatureFormat(signatureId).getSignatureForm();
        var timestamps = simple.getSignatureTimestamps(signatureId);

        var isRevocationValidated = simple.getAdESValidationErrors(signatureId).stream().noneMatch(e -> e.getValue().contains("No revocation data found for the certificate"));
        var isTimestampInvalid = timestamps.stream().map(XmlTimestamp::getIndication).anyMatch(i -> i.equals(Indication.FAILED) || i.equals(Indication.TOTAL_FAILED));
        var isTimestampIndeterminate = timestamps.stream().map(XmlTimestamp::getIndication).anyMatch(Indication.INDETERMINATE::equals);

        var nameBox = new HBox(
                new TextFlow(new Text(simple.getSignedBy(signatureId))),
                new VBox(createSignatureQualificationBadge(resources, documentReport, isValidated, signatureId, 300)));

        var signatureDetailsBox = new VBox(createTableRow(
                translate(resources, "signature.details.validation.label"),
                isValidated
                        ? validityToString(isValid, isFailed, areTLsLoaded, isRevocationValidated,
                                signatureQualification, signatureForm, isTimestampInvalid, isTimestampIndeterminate, resources)
                        : translate(resources, "signature.details.validation.inProgress.label")));

        if (showDocumentName)
            signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "general.document"), getDisplayDocumentName(resources, documentReport)));

        if (documentReport.hasMultipleContainerDocuments())
            signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "general.documents"), getSignatureScopeLabel(resources, documentReport, signatureId)));

        signatureDetailsBox.getChildren().addAll(
                createTableRow(translate(resources, "signature.details.certificate.label"), subject),
                createTableRow(translate(resources, "signature.details.issuer.label"), issuer),
                createTableRow(translate(resources, "signature.details.signingTime.label"), signingTime));

        var timestampsBox = createTimestampsBox(isValidated, timestamps, simple, diagnostic, resources, e -> callback.accept(null));
        var hasTimestamps = !timestampsBox.getChildren().isEmpty();
        signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "signature.details.type.label"),
                SignatureBadgeFactory.createBadgeFromQualification(signatureQualification, signatureForm, resources), !hasTimestamps));
        if (hasTimestamps)
            signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "signature.details.timestamps.label"), timestampsBox, true));

        return styled(new VBox(nameBox, signatureDetailsBox), "autogram-signature-box");
    }

    public static String getDisplayDocumentName(ResourceBundle resources, ValidationReports.DocumentReport documentReport) {
        var name = documentReport.document().getName();
        if (name != null && !name.isBlank())
            return name;

        return translate(resources, "signing.multiDocument.unnamedDocument", documentReport.documentIndex() + 1);
    }

    private static List<DisplayedSignature> getDisplayedSignatures(ValidationReports validationReports) {
        var signatures = new java.util.ArrayList<DisplayedSignature>();
        for (var documentReport : validationReports.getDocumentReports())
            for (var signatureId : documentReport.reports().getSimpleReport().getSignatureIdList())
                signatures.add(new DisplayedSignature(documentReport, signatureId));
        
        return signatures;
    }

    private static Node createSignatureQualificationBadge(ResourceBundle resources, ValidationReports.DocumentReport documentReport,
            boolean isValidated, String signatureId, double prefWrapLength) {
        var reports = documentReport.reports();
        if (!isValidated)
            return SignatureBadgeFactory.createInProgressBadge(resources);

        if (reports.getDetailedReport().getBasicValidationIndication(signatureId).equals(Indication.FAILED))
            return SignatureBadgeFactory.createInvalidBadge(translate(resources, "signature.invalid.label"));

        return SignatureBadgeFactory.createCombinedBadgeFromQualification(resources,
                reports.getDetailedReport().getSignatureQualification(signatureId), reports, signatureId, prefWrapLength);
    }

    static String getSignatureScopeLabel(ResourceBundle resources, ValidationReports.DocumentReport documentReport, String signatureId) {
        if (documentReport.signatureCoversAllDocuments(signatureId))
            return translate(resources, "signature.scope.allDocuments.label");
        
        var scopeDocuments = documentReport.getSignatureScopeDocumentNames(signatureId);
        return scopeDocuments.isEmpty() ? translate(resources, "signature.scope.unknownDocuments.label") : String.join(", ", scopeDocuments);
    }

    static String validityToString(boolean isValid, boolean isFailed, boolean areTLsLoaded,
            boolean isRevocationValidated, SignatureQualification signatureQualification, SignatureForm signatureForm,
            boolean isTimestampInvalid, boolean isTimestampIndeterminate, ResourceBundle resources) {

        if (isFailed || isTimestampInvalid)
            return translate(resources, "signature.details.validation.failed.label");

        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm))
            return translate(resources, "signature.details.validation.unknownForm.label", signatureForm.name());

        if (!areTLsLoaded)
            return translate(resources, "signature.details.validation.trustedListInvalid.label");

        if (!isRevocationValidated)
            return translate(resources, "signature.details.validation.revocationInvalid.label");

        if (signatureQualification.getReadable().contains("Indeterminate") || isTimestampIndeterminate)
            return translate(resources, "signature.details.validation.indeterminate.label");

        if (isValid)
            return translate(resources, "signature.details.validation.valid.label");

        return translate(resources, "signature.unknown.label");
    }

    public static HBox createTableRow(String label, String value) {
        return createTableRow(label, value, false);
    }

    public static HBox createTableRow(String label, Node valueNode, boolean isLast) {
        if (valueNode instanceof Region region) {
            region.setMinWidth(0);
            region.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(region, Priority.ALWAYS);
        }

        var valueCell = styled(new HBox(valueNode),
            isLast ? "autogram-table-cell--last" : "autogram-table-cell",
            "autogram-table-cell--right");
        valueCell.setMinWidth(0);
        valueCell.setMaxWidth(Double.MAX_VALUE);

        var row = new HBox(createTableCell(label, "autogram-heading-s", isLast, true), valueCell);
        HBox.setHgrow(valueCell, Priority.ALWAYS);
        return row;
    }

    public static HBox createTableRow(String label, String value, boolean isLast) {
        var valueCell = createTableCell(value, "autogram-body", isLast, false);
        valueCell.setMinWidth(0);
        valueCell.setMaxWidth(Double.MAX_VALUE);

        var row = new HBox(createTableCell(label, "autogram-heading-s", isLast, true), valueCell);
        HBox.setHgrow(valueCell, Priority.ALWAYS);
        return row;
    }

    public static TextFlow createTableCell(String value, String textStyle, boolean isLast, boolean isLeft) {
        return styled(new TextFlow(styled(new Text(value), textStyle)),
                isLast ? "autogram-table-cell--last" : "autogram-table-cell",
                isLeft ? "autogram-table-cell--left" : "autogram-table-cell--right");
    }

    public static VBox createTimestampsBox(boolean isValidated, List<XmlTimestamp> timestamps, SimpleReport simple,
            DiagnosticData diagnostic, ResourceBundle resources, Consumer<String> callback) {
        var vBox = styled(new VBox(), "autogram-timestamps-box");

        for (var timestamp : timestamps) {
            var isFailed = timestamp.getIndication().equals(Indication.FAILED);
            var subject = new TextFlow(new Text(getPrettyDN(diagnostic.getCertificateDN(diagnostic.getTimestampSigningCertificateId(timestamp.getId())))));
            var timestampQualification = isValidated ? simple.getTimestampQualification(timestamp.getId()) : null;
            var qualificationBadge = new TextFlow(SignatureBadgeFactory.createBadgeFromTSQualification(isFailed, timestampQualification, resources));
            var timestampDetailsBox = new VBox(subject, qualificationBadge);

            var button = new Button(format.format(timestamp.getProductionTime()), new TextFlow(new Polygon(0.0, 0.0, 9.0, 6.0, 0.0, 12.0)));
            styled(button, "autogram-link");
            var detailsWrapper = new VBox(timestampDetailsBox);
            vBox.getChildren().add(new VBox(new TextFlow(button), detailsWrapper));

            button.setOnAction(e -> {
                if (timestampDetailsBox.isVisible()) {
                    timestampDetailsBox.setVisible(false);
                    detailsWrapper.getChildren().remove(timestampDetailsBox);
                } else {
                    detailsWrapper.getChildren().add(timestampDetailsBox);
                    timestampDetailsBox.setVisible(true);
                }
                callback.accept(null);
            });
        }

        return vBox;
    }

    public static String getPrettyDNWithoutCN(String s) {
        return getPrettyDN(s).replaceFirst("(\nCN=.*$|CN=.*\n)", "");
    }

    public static String getPrettyDN(String s) {
        return String.join("\n", new X500Principal(s).getName(X500Principal.RFC1779).split(", (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1));
    }

    private static <T extends Node> T styled(T node, String... classes) {
        node.getStyleClass().addAll(classes);
        return node;
    }

    private static ColumnConstraints columnPercent(double percent) {
        var cc = new ColumnConstraints();
        cc.setPercentWidth(percent);
        return cc;
    }
}
