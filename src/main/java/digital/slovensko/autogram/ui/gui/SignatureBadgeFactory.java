package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public abstract class SignatureBadgeFactory {
    public static HBox createBadge(String label, String styleClass) {
        var box = new HBox(new Text(label));
        box.getStyleClass().addAll("autogram-tag", styleClass);

        return box;
    }

    public static HBox createInProgressBadge() {
        return createBadge("Prebieha overovanie...", "autogram-tag-processing");
    }

    public static HBox createInvalidBadge(String label) {
        return createBadge(label, "autogram-tag-invalid");
    }

    public static HBox createValidQualifiedBadge(String label) {
        return createBadge(label, "autogram-tag-valid");
    }

    public static HBox createCustomValidQualifiedBadge(String label) {
        return createBadge(label, "autogram-tag-custom-valid");
    }

    public static HBox createUnknownBadge(String label) {
        return createBadge(label, "autogram-tag-unknown");
    }

    public static HBox createWarningBadge(String label) {
        return createBadge(label, "autogram-tag-warning");
    }

    public static HBox createBadgeFromQualification(SignatureQualification qualification) {
        if (qualification == null)
            return createInProgressBadge();

        switch (qualification) {
            case QESIG:
                return createValidQualifiedBadge("Kvalifikovaný elektronický podpis");
            case QESEAL:
                return createValidQualifiedBadge("Kvalifikovaná elektronická pečať");
            case ADESIG_QC:
                return createValidQualifiedBadge("Zdokonalený elektronický podpis");
            case ADESIG, ADESEAL, ADESEAL_QC:
                return createCustomValidQualifiedBadge("Iný elektronický podpis");
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD:
                return createUnknownBadge("Neznámy kvalifikovaný podpis");
            case NOT_ADES, UNKNOWN, NA:
                return createUnknownBadge("Neznámy podpis");
            default:
                if (qualification.name().contains("INDETERMINATE"))
                    return createWarningBadge("Predbežne platný podpis");
                else
                    return createInvalidBadge("Neznámy podpis");
        }
    }

    public static HBox createBadgeFromTSQualification(boolean isFailed, TimestampQualification timestampQualification) {
        if (timestampQualification == null)
            return createInProgressBadge();

        if (isFailed)
            return createInvalidBadge("Neplatná časová pečiatka");

        switch (timestampQualification) {
            case QTSA:
                return createValidQualifiedBadge("Kvalifikovaná časová pečiatka");
            case TSA:
                return createCustomValidQualifiedBadge("Časová pečiatka");
            default:
                return createUnknownBadge("Neznáma časová pečiatka");
        }
    }

    public static HBox createCombinedBadgeFromQualification(SignatureQualification signatureQualification,
            Reports reports, String signatureId) {
        if (signatureQualification == null)
            return createInProgressBadge();

        if (areTimestampsFailed(reports, signatureId))
            return createMultipleBadges(signatureQualification, reports, signatureId);

        switch (signatureQualification) {
            case QESIG: {
                if (reports.getSimpleReport().getSignatureTimestamps(signatureId).size() == 0)
                    return createValidQualifiedBadge("Vlastnoručný podpis");

                if (areTimestampsQualified(reports, signatureId))
                    return createValidQualifiedBadge("Osvedčený pdopis");

                return createMultipleBadges(signatureQualification, reports, signatureId);
            }
            case QESEAL: {
                if (areTimestampsQualified(reports, signatureId))
                    return createValidQualifiedBadge("Elektronická pečať");
                return createMultipleBadges(signatureQualification, reports, signatureId);
            }
            case ADESIG_QC: {
                if (areTimestampsQualified(reports, signatureId))
                    return createValidQualifiedBadge("Uznaný spôsob autorizácie");
                return createMultipleBadges(signatureQualification, reports, signatureId);
            }
            case ADESEAL, ADESEAL_QC, ADESIG:
                return createMultipleBadges(signatureQualification, reports, signatureId);
            default:
                return createBadgeFromQualification(signatureQualification);
        }
    }

    private static HBox createMultipleBadges(SignatureQualification signatureQualification, Reports reports,
            String signatureId) {
        var hBox = new HBox(createReadableBadgeFromQualification(signatureQualification));
        hBox.getStyleClass().add("autogram-tag-multiple-box");

        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId)) {
            var isQualified = timestamp.getQualificationDetails() != null;
            var isFailed = timestamp.getIndication() == Indication.TOTAL_FAILED
                    || timestamp.getIndication() == Indication.FAILED;

            if (isFailed)
                hBox.getChildren().add(createInvalidBadge("Neplatná ČP"));
            else if (isQualified)
                hBox.getChildren().add(
                        createValidQualifiedBadge(simple.getTimestampQualification(timestamp.getId()).getReadable()));
            else
                hBox.getChildren()
                        .add(createUnknownBadge(simple.getTimestampQualification(timestamp.getId()).getReadable()));
        }

        return hBox;
    }

    private static HBox createReadableBadgeFromQualification(SignatureQualification qualification) {
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
                    return createWarningBadge(qualification.getReadable());
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
