package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.ValidationReports;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import eu.europa.esig.dss.validation.reports.Reports;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static digital.slovensko.autogram.ui.gui.HasI18n.translate;
import static eu.europa.esig.dss.enumerations.SignatureForm.CAdES;
import static eu.europa.esig.dss.enumerations.SignatureForm.PAdES;
import static eu.europa.esig.dss.enumerations.SignatureForm.XAdES;

final class SignatureHtmlRenderer {
    private static final String BODY_PLACEHOLDER = "__AUTOGRAM_SIGNATURE_BODY__";
    private static final String HTML_TEMPLATE = """
            <html>
            <head>
                <meta charset="UTF-8">
                <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; img-src data:;">
                <style>
                    :root {
                        color-scheme: light;
                    }

                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        padding: 0;
                        background: #ffffff;
                        color: #383f43;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                        font-size: 14px;
                        line-height: 1.45;
                    }

                    .summary-list {
                        display: flex;
                        flex-direction: column;
                        font-size: 16px;
                    }

                    .summary-row {
                        display: grid;
                        grid-template-columns: minmax(0, 45%) minmax(0, 55%);
                        gap: 0;
                        align-items: center;
                        padding: 0;
                        border-bottom: 1px solid #d9d9d9;
                    }

                    .summary-name {
                        min-width: 0;
                        padding: 10px 12px 10px 0;
                        font-weight: 600;
                        overflow-wrap: anywhere;
                    }

                    .summary-badges,
                    .badge-row {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 8px;
                        align-items: center;
                    }

                    .summary-badges {
                        justify-content: flex-start;
                        padding: 10px 0;
                    }

                    .summary-more {
                        padding: 10px 0 0;
                    }

                    .warning-box {
                        margin: 0 0 12px;
                        padding: 12px 14px;
                        border-left: 4px solid #b38f00;
                        background: #fff7bf;
                        color: #594d00;
                    }

                    .signature-card {
                        margin-bottom: 12px;
                        border: 2px solid #d9d9d9;
                    }

                    .signature-card:last-child {
                        margin-bottom: 0;
                    }

                    .signature-card-header {
                        display: flex;
                        justify-content: space-between;
                        gap: 16px;
                        align-items: flex-start;
                        padding: 14px 16px;
                        background: #f3f3f3;
                    }

                    .signature-card-title {
                        min-width: 0;
                        font-size: 16px;
                        font-weight: 700;
                        overflow-wrap: anywhere;
                    }

                    .signature-card-body {
                        display: flex;
                        flex-direction: column;
                    }

                    .detail-row {
                        display: grid;
                        grid-template-columns: minmax(170px, 220px) minmax(0, 1fr);
                        border-top: 1px solid #d9d9d9;
                    }

                    .detail-row:first-child {
                        border-top: 0;
                    }

                    .detail-label {
                        padding: 10px 16px;
                        font-weight: 700;
                    }

                    .detail-value {
                        min-width: 0;
                        padding: 10px 16px;
                        overflow-wrap: anywhere;
                    }

                    .preformatted {
                        white-space: pre-line;
                    }

                    details.timestamp {
                        border: 1px solid #d9d9d9;
                        border-radius: 2px;
                        background: #ffffff;
                    }

                    details.timestamp + details.timestamp {
                        margin-top: 8px;
                    }

                    details.timestamp > summary {
                        padding: 8px 12px;
                        color: #0065bd;
                        font-weight: 700;
                        cursor: pointer;
                        list-style: none;
                    }

                    details.timestamp > summary::-webkit-details-marker {
                        display: none;
                    }

                    details.timestamp[open] > summary {
                        border-bottom: 1px solid #d9d9d9;
                    }

                    .timestamp-body {
                        padding: 10px 12px 12px;
                    }

                    .timestamp-subject {
                        margin-bottom: 8px;
                    }

                    .autogram-tag {
                        display: inline-flex;
                        align-items: center;
                        padding: 4px 8px;
                        font-weight: 700;
                        white-space: nowrap;
                    }

                    .autogram-tag-processing {
                        background: #d2e2f1;
                        color: #144e81;
                    }

                    .autogram-tag-invalid {
                        background: #f6d7d2;
                        color: #721c24;
                    }

                    .autogram-tag-valid {
                        background: #cce2d8;
                        color: #005a30;
                    }

                    .autogram-tag-custom-valid {
                        background: #d2e2f1;
                        color: #144e81;
                    }

                    .autogram-tag-unknown,
                    .autogram-tag-warning {
                        background: #fff7bf;
                        color: #594d00;
                    }

                    .autogram-tag-info {
                        background: #eeefef;
                        color: #383f43;
                    }
                </style>
            </head>
            <body>__AUTOGRAM_SIGNATURE_BODY__</body>
            </html>
            """;

    record SummaryDocument(String html, boolean truncated) {}

    private record DisplayedSignature(ValidationReports.DocumentReport documentReport, String signatureId) {}

    private record BadgeDescriptor(String label, String styleClass) {}

    private SignatureHtmlRenderer() {
    }

    static SummaryDocument buildSummaryDocument(ResourceBundle resources, ValidationReports validationReports,
            boolean isValidated, boolean areTLsLoaded, int maxRows) {
        var signatures = getDisplayedSignatures(validationReports);
        var totalSignatures = validationReports.getTotalSignatureCount();
        var truncated = totalSignatures > maxRows;
        if (truncated)
            signatures = signatures.subList(0, Math.max(0, maxRows - 1));

        var content = new StringBuilder();
        if (!areTLsLoaded) {
            content.append("<div class=\"warning-box\">")
                    .append(escapeHtml(translate(resources, "signing.tlsLoading.error")))
                    .append("</div>");
        }

        content.append("<div class=\"summary-list\">");
        for (var signature : signatures) {
            var documentReport = signature.documentReport();
            var badges = new ArrayList<>(describeHeaderBadges(resources, documentReport, isValidated,
                    signature.signatureId()));
            if (documentReport.hasMultipleContainerDocuments()) {
                badges.add(documentReport.signatureCoversAllDocuments(signature.signatureId())
                        ? createBadge(translate(resources, "signature.scope.allDocuments.label"),
                                "autogram-tag-valid")
                        : createBadge(translate(resources, "signature.scope.notAllDocuments.label"),
                                "autogram-tag-warning"));
            }

            content.append("<div class=\"summary-row\"><div class=\"summary-name\">")
                    .append(escapeHtml(documentReport.reports().getSimpleReport().getSignedBy(signature.signatureId())))
                    .append("</div><div class=\"summary-badges\">");
            appendBadges(content, badges);
            content.append("</div></div>");
        }

        if (truncated) {
            var moreTextKey = validationReports.shouldShowDocumentContext()
                    ? "signature.table.more.documents.txt"
                    : "signature.table.more.txt";
            content.append("<div class=\"summary-more\">")
                    .append(escapeHtml(translate(resources, moreTextKey, totalSignatures - signatures.size())))
                    .append("</div>");
        }

        content.append("</div>");
        return new SummaryDocument(wrapHtml(content.toString()), truncated);
    }

    static String buildPresentDocument(ResourceBundle resources, ValidationReports reports, boolean isValidated,
            boolean areTLsLoaded) {
        var content = new StringBuilder();
        for (var documentReport : reports.getDocumentReports()) {
            for (var signatureId : documentReport.reports().getDiagnosticData().getSignatureIdList())
                content.append(renderSignatureCard(resources, documentReport, reports.isMultiDocumentJob(), isValidated,
                        signatureId, areTLsLoaded));
        }

        return wrapHtml(content.toString());
    }

    static String emptyDocument() {
        return wrapHtml("");
    }

    private static String renderSignatureCard(ResourceBundle resources, ValidationReports.DocumentReport documentReport,
            boolean showDocumentName, boolean isValidated, String signatureId, boolean areTLsLoaded) {
        var reports = documentReport.reports();
        var simple = reports.getSimpleReport();
        var diagnostic = reports.getDiagnosticData();

        var isValid = simple.isValid(signatureId);
        var isFailed = reports.getDetailedReport().getBasicValidationIndication(signatureId).equals(Indication.FAILED);
        var signingTime = GUIValidationUtils.format.format(simple.getSigningTime(signatureId));
        var subject = GUIValidationUtils.getPrettyDNWithoutCN(
                diagnostic.getSignatureById(signatureId).getSigningCertificate().getCertificateDN());
        var issuer = GUIValidationUtils.getPrettyDN(
                diagnostic.getCertificateIssuerDN(diagnostic.getSignatureById(signatureId).getSigningCertificate().getId()));
        var signatureQualification = isValidated ? reports.getDetailedReport().getSignatureQualification(signatureId)
                : null;
        var signatureForm = simple.getSignatureFormat(signatureId).getSignatureForm();
        var timestamps = simple.getSignatureTimestamps(signatureId);
        var isRevocationValidated = simple.getAdESValidationErrors(signatureId).stream()
                .noneMatch(e -> e.getValue().contains("No revocation data found for the certificate"));
        var isTimestampInvalid = timestamps.stream().map(XmlTimestamp::getIndication)
                .anyMatch(i -> i.equals(Indication.FAILED) || i.equals(Indication.TOTAL_FAILED));
        var isTimestampIndeterminate = timestamps.stream().map(XmlTimestamp::getIndication)
                .anyMatch(Indication.INDETERMINATE::equals);

        var content = new StringBuilder();
        content.append("<section class=\"signature-card\"><div class=\"signature-card-header\"><div class=\"signature-card-title\">")
                .append(escapeHtml(simple.getSignedBy(signatureId)))
                .append("</div><div class=\"badge-row\">");
        appendBadges(content, describeHeaderBadges(resources, documentReport, isValidated, signatureId));
        content.append("</div></div><div class=\"signature-card-body\">");

        appendDetailRow(content, translate(resources, "signature.details.validation.label"),
                escapeHtml(GUIValidationUtils.validityToString(isValid, isFailed, areTLsLoaded, isRevocationValidated,
                        signatureQualification, signatureForm, isTimestampInvalid, isTimestampIndeterminate,
                        resources)), false);

        if (showDocumentName) {
            appendDetailRow(content, translate(resources, "general.document"),
                    escapeHtml(GUIValidationUtils.getDisplayDocumentName(resources, documentReport)), false);
        }

        if (documentReport.hasMultipleContainerDocuments()) {
            appendDetailRow(content, translate(resources, "general.documents"),
                    escapeHtml(GUIValidationUtils.getSignatureScopeLabel(resources, documentReport, signatureId)), false);
        }

        appendDetailRow(content, translate(resources, "signature.details.certificate.label"), escapePreformatted(subject),
                true);
        appendDetailRow(content, translate(resources, "signature.details.issuer.label"), escapePreformatted(issuer), true);
        appendDetailRow(content, translate(resources, "signature.details.signingTime.label"), escapeHtml(signingTime),
                false);

        appendBadgeRow(content, translate(resources, "signature.details.type.label"),
                List.of(describeTypeBadge(signatureQualification, signatureForm, resources)));

        if (!timestamps.isEmpty()) {
            content.append("<div class=\"detail-row\"><div class=\"detail-label\">")
                    .append(escapeHtml(translate(resources, "signature.details.timestamps.label")))
                    .append("</div><div class=\"detail-value\">");
            appendTimestampDetails(content, timestamps, simple, diagnostic, resources, isValidated);
            content.append("</div></div>");
        }

        content.append("</div></section>");
        return content.toString();
    }

    private static void appendTimestampDetails(StringBuilder content, List<XmlTimestamp> timestamps, SimpleReport simple,
            eu.europa.esig.dss.diagnostic.DiagnosticData diagnostic, ResourceBundle resources, boolean isValidated) {
        for (var timestamp : timestamps) {
            var timestampQualification = isValidated ? simple.getTimestampQualification(timestamp.getId()) : null;
            var isFailed = timestamp.getIndication().equals(Indication.FAILED)
                    || timestamp.getIndication().equals(Indication.TOTAL_FAILED);

            content.append("<details class=\"timestamp\"><summary>")
                    .append(escapeHtml(GUIValidationUtils.format.format(timestamp.getProductionTime())))
                    .append("</summary><div class=\"timestamp-body\"><div class=\"timestamp-subject preformatted\">")
                    .append(escapePreformatted(
                            GUIValidationUtils.getPrettyDN(diagnostic.getCertificateDN(
                                    diagnostic.getTimestampSigningCertificateId(timestamp.getId())))))
                    .append("</div><div class=\"badge-row\">");
            appendBadges(content, List.of(describeTimestampBadge(isFailed, timestampQualification, resources)));
            content.append("</div></div></details>");
        }
    }

    private static void appendDetailRow(StringBuilder content, String label, String valueHtml, boolean preformatted) {
        content.append("<div class=\"detail-row\"><div class=\"detail-label\">")
                .append(escapeHtml(label))
                .append("</div><div class=\"detail-value");
        if (preformatted)
            content.append(" preformatted");
        content.append("\">")
                .append(valueHtml)
                .append("</div></div>");
    }

    private static void appendBadgeRow(StringBuilder content, String label, List<BadgeDescriptor> badges) {
        content.append("<div class=\"detail-row\"><div class=\"detail-label\">")
                .append(escapeHtml(label))
                .append("</div><div class=\"detail-value\"><div class=\"badge-row\">");
        appendBadges(content, badges);
        content.append("</div></div></div>");
    }

    private static void appendBadges(StringBuilder content, List<BadgeDescriptor> badges) {
        for (var badge : badges) {
            content.append("<span class=\"autogram-tag ")
                    .append(badge.styleClass())
                    .append("\">")
                    .append(escapeHtml(badge.label()))
                    .append("</span>");
        }
    }

    private static List<DisplayedSignature> getDisplayedSignatures(ValidationReports validationReports) {
        var signatures = new ArrayList<DisplayedSignature>();
        for (var documentReport : validationReports.getDocumentReports()) {
            for (var signatureId : documentReport.reports().getSimpleReport().getSignatureIdList())
                signatures.add(new DisplayedSignature(documentReport, signatureId));
        }

        return signatures;
    }

    private static List<BadgeDescriptor> describeHeaderBadges(ResourceBundle resources,
            ValidationReports.DocumentReport documentReport, boolean isValidated, String signatureId) {
        var reports = documentReport.reports();
        if (!isValidated)
            return List.of(createBadge(translate(resources, "signature.qualification.inProgress.label"),
                    "autogram-tag-processing"));

        if (reports.getDetailedReport().getBasicValidationIndication(signatureId).equals(Indication.FAILED)) {
            return List.of(createBadge(translate(resources, "signature.invalid.label"), "autogram-tag-invalid"));
        }

        return describeCombinedBadges(resources, reports.getDetailedReport().getSignatureQualification(signatureId), reports,
                signatureId);
    }

    private static BadgeDescriptor describeTypeBadge(SignatureQualification qualification, SignatureForm signatureForm,
            ResourceBundle resources) {
        if (qualification == null)
            return createBadge(translate(resources, "signature.qualification.inProgress.label"), "autogram-tag-processing");

        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm)) {
            return createBadge(translate(resources, "signature.unknownForm.label", signatureForm.name()),
                    "autogram-tag-warning");
        }

        var key = "signature.qualification." + qualification.name() + ".label";
        return switch (qualification) {
            case QESIG, QESEAL, ADESIG_QC -> createBadge(translate(resources, key), "autogram-tag-valid");
            case ADESIG, ADESEAL, ADESEAL_QC -> createBadge(translate(resources, key), "autogram-tag-custom-valid");
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD -> createBadge(
                    translate(resources, "signature.qualification.unknown.label"), "autogram-tag-unknown");
            case NOT_ADES, UNKNOWN, NA -> createBadge(translate(resources, "signature.unknown.label"),
                    "autogram-tag-unknown");
            case INDETERMINATE_QESIG, INDETERMINATE_QESEAL, INDETERMINATE_ADESIG_QC,
                    INDETERMINATE_ADESIG, INDETERMINATE_ADESEAL, INDETERMINATE_ADESEAL_QC,
                    INDETERMINATE_UNKNOWN_QC, INDETERMINATE_UNKNOWN_QC_QSCD -> createBadge(translate(resources, key),
                            "autogram-tag-processing");
            default -> qualification.name().contains("INDETERMINATE")
                    ? createBadge(translate(resources, "signature.qualification.indeterminate.unknown.label"),
                            "autogram-tag-processing")
                    : createBadge(translate(resources, "signature.unknown.label"), "autogram-tag-invalid");
        };
    }

    private static BadgeDescriptor describeTimestampBadge(boolean isFailed,
            TimestampQualification timestampQualification, ResourceBundle resources) {
        if (timestampQualification == null)
            return createBadge(translate(resources, "signature.qualification.inProgress.label"),
                    "autogram-tag-processing");

        if (isFailed)
            return createBadge(translate(resources, "signature.timestamp.invalid.label"), "autogram-tag-invalid");

        return switch (timestampQualification) {
            case QTSA -> createBadge(translate(resources, "signature.timestamp.QTSA.label"), "autogram-tag-valid");
            case TSA -> createBadge(translate(resources, "signature.timestamp.TSA.label"),
                    "autogram-tag-custom-valid");
            default -> createBadge(translate(resources, "signature.timestamp.unknown.label"), "autogram-tag-unknown");
        };
    }

    private static List<BadgeDescriptor> describeCombinedBadges(ResourceBundle resources,
            SignatureQualification signatureQualification, Reports reports, String signatureId) {
        if (signatureQualification == null) {
            return List.of(createBadge(translate(resources, "signature.qualification.inProgress.label"),
                    "autogram-tag-processing"));
        }

        var signatureForm = reports.getSimpleReport().getSignatureFormat(signatureId).getSignatureForm();
        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm)) {
            return List.of(createBadge(translate(resources, "signature.unknownForm.label", signatureForm.name()),
                    "autogram-tag-warning"));
        }

        if (areTimestampsFailed(reports, signatureId))
            return createMultipleBadges(signatureQualification, reports, signatureId, resources);

        return switch (signatureQualification) {
            case QESIG -> {
                if (reports.getSimpleReport().getSignatureTimestamps(signatureId).isEmpty()) {
                    yield List.of(createBadge(translate(resources, "signature.qualification.QESIG.shortLabel"),
                            "autogram-tag-valid"));
                }

                if (areTimestampsQualified(reports, signatureId)) {
                    yield List.of(createBadge(translate(resources, "signature.qualification.QESIG.QTSA.shortLabel"),
                            "autogram-tag-valid"));
                }

                yield createMultipleBadges(signatureQualification, reports, signatureId, resources);
            }
            case QESEAL -> {
                if (areTimestampsQualified(reports, signatureId)) {
                    yield List.of(createBadge(translate(resources, "signature.qualification.QESEAL.shortLabel"),
                            "autogram-tag-valid"));
                }

                yield createMultipleBadges(signatureQualification, reports, signatureId, resources);
            }
            case ADESIG_QC -> {
                if (areTimestampsQualified(reports, signatureId)) {
                    yield List.of(createBadge(translate(resources, "signature.qualification.ADESIG_QC.shortLabel"),
                            "autogram-tag-valid"));
                }

                yield createMultipleBadges(signatureQualification, reports, signatureId, resources);
            }
            case ADESEAL, ADESEAL_QC, ADESIG -> createMultipleBadges(signatureQualification, reports, signatureId,
                    resources);
            default -> List.of(describeTypeBadge(signatureQualification, signatureForm, resources));
        };
    }

    private static List<BadgeDescriptor> createMultipleBadges(SignatureQualification signatureQualification,
            Reports reports, String signatureId, ResourceBundle resources) {
        var badges = new ArrayList<BadgeDescriptor>();
        badges.add(describeReadableBadge(signatureQualification, resources));

        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId)) {
            var qualification = simple.getTimestampQualification(timestamp.getId());
            var isQualified = qualification == TimestampQualification.QTSA;
            var isFailed = timestamp.getIndication() == Indication.TOTAL_FAILED
                    || timestamp.getIndication() == Indication.FAILED;
            var isIndeterminate = timestamp.getIndication() == Indication.INDETERMINATE;

            if (isFailed) {
                badges.add(createBadge(translate(resources, "signature.timestamp.invalid.shortLabel"),
                        "autogram-tag-invalid"));
            } else if (isIndeterminate) {
                badges.add(createBadge(translate(resources, "signature.timestamp.unknown.shortLabel"),
                        "autogram-tag-unknown"));
            } else if (isQualified) {
                badges.add(createBadge(qualification.getReadable(), "autogram-tag-valid"));
            } else {
                badges.add(createBadge(translate(resources, "signature.timestamp.unknown.shortLabel"),
                        "autogram-tag-unknown"));
            }
        }

        return badges;
    }

    private static BadgeDescriptor describeReadableBadge(SignatureQualification qualification, ResourceBundle resources) {
        return switch (qualification) {
            case QESIG, QESEAL, ADESIG_QC -> createBadge(qualification.getReadable(), "autogram-tag-valid");
            case ADESIG, ADESEAL, ADESEAL_QC -> createBadge(qualification.getReadable(), "autogram-tag-custom-valid");
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD -> createBadge(
                    qualification.getReadable(), "autogram-tag-unknown");
            case NOT_ADES, UNKNOWN, NA -> createBadge(translate(resources, "signature.unknown.label"),
                    "autogram-tag-unknown");
            default -> qualification.name().contains("INDETERMINATE")
                    ? createBadge(qualification.getReadable(), "autogram-tag-processing")
                    : createBadge(translate(resources, "signature.unknown.label"), "autogram-tag-invalid");
        };
    }

    private static boolean areTimestampsQualified(Reports reports, String signatureId) {
        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId)) {
            if (simple.getTimestampQualification(timestamp.getId()) != TimestampQualification.QTSA)
                return false;
        }

        return true;
    }

    private static boolean areTimestampsFailed(Reports reports, String signatureId) {
        for (var timestamp : reports.getSimpleReport().getSignatureTimestamps(signatureId)) {
            if (timestamp.getIndication().equals(Indication.TOTAL_FAILED)
                    || timestamp.getIndication().equals(Indication.FAILED))
                return true;
        }

        return false;
    }

    private static BadgeDescriptor createBadge(String label, String styleClass) {
        return new BadgeDescriptor(label, styleClass);
    }

    private static String wrapHtml(String body) {
        return HTML_TEMPLATE.replace(BODY_PLACEHOLDER, body);
    }

    private static String escapePreformatted(String text) {
        return escapeHtml(text).replace("\n", "<br/>");
    }

    private static String escapeHtml(String text) {
        if (text == null)
            return "";

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}