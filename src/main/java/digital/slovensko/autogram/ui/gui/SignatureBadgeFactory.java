package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

import static eu.europa.esig.dss.enumerations.SignatureForm.*;

public abstract class SignatureBadgeFactory {
    public static Node createBadge(String label, String styleClass) {
        var box = new HBox(new TextFlow(new Text(label)));
        box.getStyleClass().addAll("autogram-tag", styleClass);

        return box;
    }

    public static Node createInProgressBadge() {
        return createInProgressBadge("Prebieha overovanie…");
    }

    public static Node createInProgressBadge(String label) {
        return createBadge(label, "autogram-tag-processing");
    }

    public static Node createInvalidBadge(String label) {
        return createBadge(label, "autogram-tag-invalid");
    }

    public static Node createValidQualifiedBadge(String label) {
        return createBadge(label, "autogram-tag-valid");
    }

    public static Node createCustomValidQualifiedBadge(String label) {
        return createBadge(label, "autogram-tag-custom-valid");
    }

    public static Node createUnknownBadge(String label) {
        return createBadge(label, "autogram-tag-unknown");
    }

    public static Node createWarningBadge(String label) {
        return createBadge(label, "autogram-tag-warning");
    }

    public static Node createInfoBadge(String label) {
        return createBadge(label, "autogram-tag-info");
    }

    public static Node createBadgeFromQualification(SignatureQualification qualification, SignatureForm signatureForm) {
        if (qualification == null)
            return createInProgressBadge();

        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm))
            return createWarningBadge("Neznámy formát: " + signatureForm.name());

        switch (qualification) {
            case QESIG:
                return createValidQualifiedBadge("Kvalifikovaný elektronický podpis");
            case QESEAL:
                return createValidQualifiedBadge("Kvalifikovaná elektronická pečať");
            case ADESIG_QC:
                return createValidQualifiedBadge("Zdokonalený elektronický podpis");
            case ADESIG, ADESEAL, ADESEAL_QC:
                return createCustomValidQualifiedBadge("Iný zdokonalený elektronický podpis");
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD:
                return createUnknownBadge("Neznámy kvalifikovaný podpis");
            case NOT_ADES, UNKNOWN, NA:
                return createUnknownBadge("Neznámy podpis");
            case INDETERMINATE_QESIG:
                return createInProgressBadge("Predbežne platný kvalifikovaný podpis");
            case INDETERMINATE_QESEAL:
                return createInProgressBadge("Predbežne platná elektronická pečať");
            case INDETERMINATE_ADESIG_QC:
                return createInProgressBadge("Predbežne platný zdokonalený podpis");
            case INDETERMINATE_ADESIG, INDETERMINATE_ADESEAL, INDETERMINATE_ADESEAL_QC:
                return createInProgressBadge("Predbežne platný iný zdokonalený podpis");
            case INDETERMINATE_UNKNOWN_QC, INDETERMINATE_UNKNOWN_QC_QSCD:
                return createInProgressBadge("Predbežne platný neznámy kvalifikovaný podpis");
            default:
                if (qualification.name().contains("INDETERMINATE"))
                    return createInProgressBadge("Predbežne platný neznámy podpis");
                else
                    return createInvalidBadge("Neznámy podpis");
        }
    }

    public static Node createBadgeFromTSQualification(boolean isFailed, TimestampQualification timestampQualification) {
        if (timestampQualification == null)
            return createInProgressBadge();

        if (isFailed)
            return createInvalidBadge("Neplatná časová pečiatka");

        return switch (timestampQualification) {
            case QTSA -> createValidQualifiedBadge("Kvalifikovaná časová pečiatka");
            case TSA -> createCustomValidQualifiedBadge("Časová pečiatka");
            default -> createUnknownBadge("Neznáma časová pečiatka");
        };
    }

    public static Node createCombinedBadgeFromQualification(SignatureQualification signatureQualification,
                                                            Reports reports, String signatureId, double prefWrapLength) {
        if (signatureQualification == null)
            return createInProgressBadge();

        var signatureForm = reports.getSimpleReport().getSignatureFormat(signatureId).getSignatureForm();
        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm))
            return createWarningBadge("Neznámy formát: " + signatureForm.name());

        if (areTimestampsFailed(reports, signatureId))
            return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength);

        switch (signatureQualification) {
            case QESIG: {
                if (reports.getSimpleReport().getSignatureTimestamps(signatureId).isEmpty())
                    return createValidQualifiedBadge("Vlastnoručný podpis");

                if (areTimestampsQualified(reports, signatureId))
                    return createValidQualifiedBadge("Osvedčený podpis");

                return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength);
            }
            case QESEAL: {
                if (areTimestampsQualified(reports, signatureId))
                    return createValidQualifiedBadge("Elektronická pečať");
                return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength);
            }
            case ADESIG_QC: {
                if (areTimestampsQualified(reports, signatureId))
                    return createValidQualifiedBadge("Uznaný spôsob autorizácie");
                return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength);
            }
            case ADESEAL, ADESEAL_QC, ADESIG:
                return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength);
            default:
                return createBadgeFromQualification(signatureQualification, signatureForm);
        }
    }

    private static HBox createMultipleBadges(SignatureQualification signatureQualification, Reports reports,
            String signatureId, double prefWrapLength) {
        var flowPane = new FlowPane(createReadableBadgeFromQualification(signatureQualification));
        flowPane.getStyleClass().add("autogram-tag-multiple-box");
        if (prefWrapLength > 1)
            flowPane.setPrefWrapLength(prefWrapLength);

        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId)) {
            var isQualified = simple.getTimestampQualification(timestamp.getId()) == TimestampQualification.QTSA;
            var isFailed = timestamp.getIndication() == Indication.TOTAL_FAILED
                    || timestamp.getIndication() == Indication.FAILED;
            var isIndeterminate = timestamp.getIndication() == Indication.INDETERMINATE;

            if (isFailed)
                flowPane.getChildren().add(createInvalidBadge("Neplatná ČP"));

            else if (isIndeterminate)
                flowPane.getChildren().add(createUnknownBadge("Neznáma ČP"));

            else if (isQualified)
                flowPane.getChildren().add(createValidQualifiedBadge(simple.getTimestampQualification(timestamp.getId()).getReadable()));

            else
                flowPane.getChildren().add(createUnknownBadge("Neznáma ČP"));
        }

        return new HBox(flowPane);
    }

    private static Node createReadableBadgeFromQualification(SignatureQualification qualification) {
        switch (qualification) {
            case QESIG, QESEAL, ADESIG_QC:
                return createValidQualifiedBadge(qualification.getReadable());
            case ADESIG, ADESEAL, ADESEAL_QC:
                return createCustomValidQualifiedBadge(qualification.getReadable());
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD:
                return createUnknownBadge(qualification.getReadable());
            case NOT_ADES, UNKNOWN, NA:
                return createUnknownBadge("Neznámy podpis");
            default:
                if (qualification.name().contains("INDETERMINATE"))
                    return createInProgressBadge(qualification.getReadable());
                else
                    return createInvalidBadge("Neznámy podpis");
        }
    }

    private static boolean areTimestampsQualified(Reports reports, String signatureId) {
        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId))
            if (!simple.getTimestampQualification(timestamp.getId()).equals(TimestampQualification.QTSA))
                return false;

        return true;
    }

    private static boolean areTimestampsFailed(Reports reports, String signatureId) {
        for (var timestamp : reports.getSimpleReport().getSignatureTimestamps(signatureId))
            if (timestamp.getIndication().equals(Indication.TOTAL_FAILED)
                    || timestamp.getIndication().equals(Indication.FAILED))
                return true;

        return false;
    }
}
