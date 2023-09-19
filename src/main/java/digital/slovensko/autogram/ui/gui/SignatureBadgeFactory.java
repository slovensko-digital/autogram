package digital.slovensko.autogram.ui.gui;

import java.util.ArrayList;

import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public abstract class SignatureBadgeFactory {
    public static VBox createBadge(String label, String styleClass) {
        var box = new VBox(new Text(label));
        box.getStyleClass().addAll("autogram-tag", styleClass);

        return box;
    }

    public static VBox createInProgressBadge() {
        return createBadge("Prebieha overovanie...", "autogram-tag-processing");
    }

    public static VBox createInvalidBadge(String label) {
        return createBadge(label, "autogram-tag-invalid");
    }

    public static VBox createValidQualifiedBadge(String label) {
        return createBadge(label, "autogram-tag-valid");
    }

    public static VBox createCustomValidQualifiedBadge(String label) {
        return createBadge(label, "autogram-tag-custom-valid");
    }

    public static VBox createUnknownBadge(String label) {
        return createBadge(label, "autogram-tag-unknown");
    }

    public static VBox createWarningBadge(String label) {
        return createBadge(label, "autogram-tag-warning");
    }

    public static VBox createBadgeFromQualification(SignatureQualification qualification) {
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

    public static VBox createBadgeFromTSQualification(boolean isFailed, TimestampQualification timestampQualification) {
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

    private static boolean allTSQualified(ArrayList<TimestampQualification> timestampQualifications) {
        for (var timestampQualification : timestampQualifications)
            if (timestampQualification != TimestampQualification.QTSA)
                return false;

        return timestampQualifications.size() > 0;
    }

    public static VBox createCombinedBadgeFromQualification(SignatureQualification signatureQualification,
            ArrayList<TimestampQualification> timestampQualifications) {
        if (signatureQualification == null)
            return createInProgressBadge();

        switch (signatureQualification) {
            case QESIG: {
                if (timestampQualifications.size() == 0)
                    return createValidQualifiedBadge("Vlastnoručný podpis");

                if (allTSQualified(timestampQualifications))
                    return createValidQualifiedBadge("Osvedčený pdopis");

                return createMultipleBadges(signatureQualification, timestampQualifications);
            }
            case QESEAL: {
                if (allTSQualified(timestampQualifications))
                    return createValidQualifiedBadge("Elektronická pečať");
                return createMultipleBadges(signatureQualification, timestampQualifications);
            }
            case ADESIG_QC: {
                if (timestampQualifications.size() == 0 || allTSQualified(timestampQualifications))
                    return createValidQualifiedBadge("Uznaný spôsob autorizácie");
                return createMultipleBadges(signatureQualification, timestampQualifications);
            }
            case ADESEAL, ADESEAL_QC, ADESIG:
                return createMultipleBadges(signatureQualification, timestampQualifications);
            default:
                return createBadgeFromQualification(signatureQualification);
        }
    }

    private static VBox createMultipleBadges(SignatureQualification signatureQualification,
            ArrayList<TimestampQualification> timestampQualifications) {
        var hBox = new HBox(10);
        hBox.getChildren().add(createCustomValidQualifiedBadge(signatureQualification.getReadable()));
        for (var timestampQualification : timestampQualifications)
            hBox.getChildren().add(createCustomValidQualifiedBadge(timestampQualification.getReadable()));

        return new VBox(hBox);
    }
}
