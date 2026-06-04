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

    private record DisplayedSignature(ValidationReports.DocumentReport documentReport, String signatureId) {
    }

    public static Node createWarningText(String message) {
        var warningText = new Text(message);
        warningText.getStyleClass().add("autogram-heading-s");
        var warningTextFlow = new TextFlow(warningText);
        warningTextFlow.getStyleClass().add("autogram-warning-textflow");

        return warningTextFlow;
    }

    public static GridPane createSignatureTableRows(ResourceBundle resources, ValidationReports validationReports, boolean isValidated, Consumer<String> callback, int maxRows) {
        var table = new GridPane();
        table.getStyleClass().add("autogram-signatures-table");

        var titleKey = validationReports.shouldShowDocumentContext()
            ? "signature.table.title.documents"
            : "signature.table.title";
        var headerText = new TextFlow(new Text(translate(resources, titleKey)));
        headerText.getStyleClass().addAll("autogram-heading-s", "autogram-signatures-table-cell--left");
        var headerLink = new TextFlow(createSignatureTableLink(callback, resources));
        table.addRow(0, headerText, headerLink);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(45);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(55);
        table.getColumnConstraints().addAll(left, right);

        var signatures = getDisplayedSignatures(validationReports);
        var totalSignatures = validationReports.getTotalSignatureCount();
        if (totalSignatures > maxRows)
            signatures = signatures.subList(0, maxRows - 1);

        int rowIndex = 1;
        for (var signature : signatures) {
            var subject = new HBox(new VBox(new TextFlow(new Text(signature.documentReport().reports().getSimpleReport().getSignedBy(signature.signatureId())))));
            subject.getStyleClass().add("autogram-signatures-table-cell--left");
            var type = new HBox(createSignatureQualificationBadge(resources, signature.documentReport(), isValidated, signature.signatureId(), 0));
            if (signature.documentReport().hasMultipleContainerDocuments()) {
                if (signature.documentReport().signatureCoversAllDocuments(signature.signatureId()))
                    type.getChildren().add(SignatureBadgeFactory.createCustomValidQualifiedBadge(translate(resources, "signature.scope.allDocuments.label")));
                
                else
                    type.getChildren().add(SignatureBadgeFactory.createWarningBadge(translate(resources, "signature.scope.notAllDocuments.label")));
            }

            type.setSpacing(8);
            table.addRow(rowIndex++, subject, type);
        }

        if (totalSignatures > maxRows) {
            var moreTextKey = validationReports.shouldShowDocumentContext()
                    ? "signature.table.more.documents.txt"
                    : "signature.table.more.txt";
            var label = new Text(translate(resources, moreTextKey, (totalSignatures - maxRows + 1)));

            var button = new Button(translate(resources, "signature.table.more.btn"));
            button.getStyleClass().addAll("autogram-link");
            button.setWrapText(true);
            button.setOnMouseClicked(event -> callback.accept(null));

            var flow = new TextFlow(label, button);
            flow.getStyleClass().addAll("autogram-body", "autogram-font-weight-bold");
            table.add(flow, 0, rowIndex - 1, 2, 1);
        }

        return table;
    }

    public static Button createSignatureTableLink(Consumer<String> callback, ResourceBundle resources) {
        var whoSignedButton = new Button(translate(resources, "signature.table.show.btn"));
        whoSignedButton.getStyleClass().addAll("autogram-link");
        whoSignedButton.wrapTextProperty().setValue(true);
        whoSignedButton.setOnMouseClicked(event -> {
            callback.accept(null);
        });

        return whoSignedButton;
    }

    public static VBox createSignatureBox(ResourceBundle resources, ValidationReports.DocumentReport documentReport,
            boolean showDocumentName, boolean isValidated, String signatureId,
            Consumer<String> callback, boolean areTLsLoaded) {
        var reports = documentReport.reports();
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
        var signatureForm = simple.getSignatureFormat(signatureId).getSignatureForm();
        var timestamps = simple.getSignatureTimestamps(signatureId);

        var nameFlow = new TextFlow(new Text(name));
        nameFlow.getStyleClass().add("autogram-summary-header__title");
        var errors = reports.getSimpleReport().getAdESValidationErrors(signatureId);
        var isRevocationValidated = true;
        for (var error : errors)
            if (error.getValue().contains("No revocation data found for the certificate"))
                isRevocationValidated = false;

        var isTimestampInvalid = false;
        var isTimestampIndeterminate = false;
        for (var timestamp : timestamps) {
            var indication = timestamp.getIndication();
            if (indication.equals(Indication.FAILED) || indication.equals(Indication.TOTAL_FAILED))
                isTimestampInvalid = true;

            if (indication.equals(Indication.INDETERMINATE))
                isTimestampIndeterminate = true;
        }

        var validFlow = new VBox(createSignatureQualificationBadge(resources, documentReport, isValidated, signatureId, 300));
        if (documentReport.hasMultipleContainerDocuments()){
            if (documentReport.signatureCoversAllDocuments(signatureId))
                validFlow.getChildren().add(SignatureBadgeFactory.createCustomValidQualifiedBadge(translate(resources, "signature.scope.allDocuments.label")));
            else
                validFlow.getChildren().add(SignatureBadgeFactory.createWarningBadge(translate(resources, "signature.scope.notAllDocuments.label")));
        }

        validFlow.setSpacing(8);
        validFlow.getStyleClass().add("autogram-summary-header__badge");
        var nameBox = new HBox(nameFlow, validFlow);

        var signatureDetailsBox = new VBox(
            createTableRow(translate(resources, "signature.details.validation.label"),
                isValidated
                    ? validityToString(isValid, isFailed, areTLsLoaded, isRevocationValidated,
                        signatureQualification, signatureForm, isTimestampInvalid, isTimestampIndeterminate,
                        resources)
                    : translate(resources, "signature.details.validation.inProgress.label")));

        if (showDocumentName)
            signatureDetailsBox.getChildren().add(
                createTableRow(translate(resources, "general.document"), getDisplayDocumentName(resources, documentReport)));

        if (documentReport.hasMultipleContainerDocuments()) {
            signatureDetailsBox.getChildren().add(createTableRow(
                    translate(resources, "general.documents"),
                    getSignatureScopeLabel(resources, documentReport, signatureId)));
        }

        signatureDetailsBox.getChildren().addAll(
            createTableRow(translate(resources, "signature.details.certificate.label"), subject),
            createTableRow(translate(resources, "signature.details.issuer.label"), issuer),
            createTableRow(translate(resources, "signature.details.signingTime.label"), signingTime));

        var timestampsBox = createTimestampsBox(isValidated, timestamps, simple, diagnostic, resources, e -> {
            callback.accept(null);
        });
        if (!timestampsBox.getChildren().isEmpty()) {
            signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "signature.details.type.label"),
                    SignatureBadgeFactory.createBadgeFromQualification(signatureQualification, signatureForm, resources), false));
            signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "signature.details.timestamps.label"), timestampsBox, true));
        } else
            signatureDetailsBox.getChildren().add(createTableRow(translate(resources, "signature.details.type.label"),
                    SignatureBadgeFactory.createBadgeFromQualification(signatureQualification, signatureForm, resources), true));

        var signatureBox = new VBox(nameBox, signatureDetailsBox);
        signatureBox.getStyleClass().add("autogram-signature-box");
        return signatureBox;
    }

    public static String getDisplayDocumentName(ResourceBundle resources, ValidationReports.DocumentReport documentReport) {
        var name = documentReport.document().getName();
        if (name != null && !name.isBlank())
            return name;

        return translate(resources, "signing.multiDocument.unnamedDocument", documentReport.documentIndex() + 1);
    }

    private static List<DisplayedSignature> getDisplayedSignatures(ValidationReports validationReports) {
        var signatures = new java.util.ArrayList<DisplayedSignature>();

        for (var documentReport : validationReports.getDocumentReports()) {
            for (var signatureId : documentReport.reports().getSimpleReport().getSignatureIdList()) {
                signatures.add(new DisplayedSignature(documentReport, signatureId));
            }
        }

        return signatures;
    }

    private static Node createSignatureQualificationBadge(ResourceBundle resources,
            ValidationReports.DocumentReport documentReport, boolean isValidated, String signatureId,
            double prefWrapLength) {
        var reports = documentReport.reports();

        if (!isValidated)
            return SignatureBadgeFactory.createInProgressBadge(resources);

        if (reports.getDetailedReport().getBasicValidationIndication(signatureId).equals(Indication.FAILED))
            return SignatureBadgeFactory.createInvalidBadge(translate(resources, "signature.invalid.label"));

        return SignatureBadgeFactory.createCombinedBadgeFromQualification(resources,
                reports.getDetailedReport().getSignatureQualification(signatureId),
                reports, signatureId, prefWrapLength);
    }

    private static String getSignatureScopeLabel(ResourceBundle resources,
            ValidationReports.DocumentReport documentReport, String signatureId) {
        if (documentReport.signatureCoversAllDocuments(signatureId))
            return translate(resources, "signature.scope.allDocuments.label");

        var scopeDocuments = documentReport.getSignatureScopeDocumentNames(signatureId);
        if (!scopeDocuments.isEmpty())
            return String.join(", ", scopeDocuments);

        return translate(resources, "signature.scope.unknownDocuments.label");
    }

    private static String validityToString(boolean isValid, boolean isFailed, boolean areTLsLoaded,
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
                                           DiagnosticData diagnostic, ResourceBundle resources, Consumer<String> callback) {
        var vBox = new VBox();
        vBox.getStyleClass().add("autogram-timestamps-box");

        for (var timestamp : timestamps) {
            var isFailed = timestamp.getIndication().equals(Indication.FAILED);
            var subject = new TextFlow(new Text(getPrettyDN(
                    diagnostic.getCertificateDN(diagnostic.getTimestampSigningCertificateId(timestamp.getId())))));
            var timestampQualification = isValidated ? simple.getTimestampQualification(timestamp.getId()) : null;
            var qualificationBadge = new TextFlow(
                    SignatureBadgeFactory.createBadgeFromTSQualification(isFailed, timestampQualification, resources));
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
