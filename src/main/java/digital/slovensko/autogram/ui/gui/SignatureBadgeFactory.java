package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.validation.process.qualification.signature.SigQualificationMatrix;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.ResourceBundle;

import static digital.slovensko.autogram.ui.gui.HasI18n.translate;
import static eu.europa.esig.dss.enumerations.SignatureForm.CAdES;
import static eu.europa.esig.dss.enumerations.SignatureForm.PAdES;
import static eu.europa.esig.dss.enumerations.SignatureForm.XAdES;

public abstract class SignatureBadgeFactory {
    public static Node createBadge(String label, String styleClass) {
        var textFlow = new TextFlow(new Text(label));
        textFlow.setMaxWidth(Region.USE_PREF_SIZE);

        var box = new HBox(textFlow);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.getStyleClass().addAll("autogram-tag", styleClass);

        return box;
    }

    public static Node createInProgressBadge(ResourceBundle resources) {
        return createInProgressBadge(translate(resources, "signature.qualification.inProgress.label"));
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

    /** Expected signature level before signing, not a validation result for an already created signature. */
    public static Node createBadgeFromCertificateQualification(CertificateQualification qualification, ResourceBundle resources) {
        return createBadgeFromCertificateQualification(qualification, false, null, resources);
    }

    public static Node createBadgeFromCertificateQualification(CertificateQualification qualification, boolean timestampRequested,
            TimestampQualification timestampQualification, ResourceBundle resources) {
        if (qualification == null)
            return createWarningBadge(translate(resources, "pickKey.qualification.unknown"));

        return switch (qualification) {
            case QCERT_FOR_ESIG_QSCD, QCERT_FOR_ESIG, QCERT_FOR_ESEAL_QSCD, QCERT_FOR_ESEAL -> {
                var signatureQualification = SigQualificationMatrix.getSignatureQualification(Indication.PASSED, qualification);
                var shortBadge = createShortQualificationBadge(resources, signatureQualification, timestampRequested,
                        !timestampRequested || timestampQualification == TimestampQualification.QTSA);
                if (shortBadge != null)
                    yield shortBadge;

                var badges = new FlowPane(createReadableBadgeFromQualification(signatureQualification, resources));
                if (timestampRequested)
                    badges.getChildren().add(timestampQualification == TimestampQualification.QTSA
                            ? createValidQualifiedBadge(timestampQualification.getReadable())
                            : createUnknownBadge(translate(resources, "signature.timestamp.unknown.shortLabel")));
                yield wrapMultipleBadges(badges, 0);
            }
            case QCERT_FOR_UNKNOWN, QCERT_FOR_UNKNOWN_QSCD -> createWarningBadge(translate(resources, "pickKey.qualification.unknownType"));
            default -> createWarningBadge(translate(resources, "pickKey.qualification.notQualified"));
        };
    }

    public static Node createBadgeFromQualification(SignatureQualification qualification, SignatureForm signatureForm, ResourceBundle resources) {
        if (qualification == null)
            return createInProgressBadge(resources);

        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm))
            return createWarningBadge(translate(resources, "signature.unknownForm.label", signatureForm.name()));

        var key = "signature.qualification." + qualification.name() + ".label";
        switch (qualification) {
            case QESIG, QESEAL, ADESIG_QC:
                return createValidQualifiedBadge(translate(resources, key));
            case ADESIG, ADESEAL, ADESEAL_QC:
                return createCustomValidQualifiedBadge(translate(resources, key));
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD:
                return createUnknownBadge(translate(resources, "signature.qualification.unknown.label"));
            case NOT_ADES, UNKNOWN, NA:
                return createUnknownBadge(translate(resources, "signature.unknown.label"));
            case INDETERMINATE_QESIG, INDETERMINATE_QESEAL, INDETERMINATE_ADESIG_QC:
            case INDETERMINATE_ADESIG, INDETERMINATE_ADESEAL, INDETERMINATE_ADESEAL_QC:
            case INDETERMINATE_UNKNOWN_QC, INDETERMINATE_UNKNOWN_QC_QSCD:
                return createInProgressBadge(translate(resources, key));
            default:
                if (qualification.name().contains("INDETERMINATE"))
                    return createInProgressBadge(translate(resources, "signature.qualification.indeterminate.unknown.label"));
                else
                    return createInvalidBadge(translate(resources, "signature.unknown.label"));
        }
    }

    public static Node createBadgeFromTSQualification(boolean isFailed, TimestampQualification timestampQualification, ResourceBundle resources) {
        if (timestampQualification == null)
            return createInProgressBadge(resources);

        if (isFailed)
            return createInvalidBadge(translate(resources, "signature.timestamp.invalid.label"));

        return switch (timestampQualification) {
            case QTSA -> createValidQualifiedBadge(translate(resources, "signature.timestamp.QTSA.label"));
            case TSA -> createCustomValidQualifiedBadge(translate(resources, "signature.timestamp.TSA.label"));
            default -> createUnknownBadge(translate(resources, "signature.timestamp.unknown.label"));
        };
    }

    public static Node createCombinedBadgeFromQualification(ResourceBundle resources, SignatureQualification signatureQualification,
                                                            Reports reports, String signatureId, double prefWrapLength) {
        if (signatureQualification == null)
            return createInProgressBadge(resources);

        var signatureForm = reports.getSimpleReport().getSignatureFormat(signatureId).getSignatureForm();
        if (!List.of(XAdES, CAdES, PAdES).contains(signatureForm))
            return createWarningBadge(translate(resources, "signature.unknownForm.label", signatureForm.name()));

        if (areTimestampsFailed(reports, signatureId))
            return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength, resources);

        var shortBadge = createShortQualificationBadge(resources, signatureQualification,
                !reports.getSimpleReport().getSignatureTimestamps(signatureId).isEmpty(), areTimestampsQualified(reports, signatureId));
        if (shortBadge != null)
            return shortBadge;

        switch (signatureQualification) {
            case QESIG, QESEAL, ADESIG_QC, ADESEAL, ADESEAL_QC, ADESIG:
                return createMultipleBadges(signatureQualification, reports, signatureId, prefWrapLength, resources);
            default:
                return createBadgeFromQualification(signatureQualification, signatureForm, resources);
        }
    }

    /** Shared names for the picker's prediction and the validated signature. */
    private static Node createShortQualificationBadge(ResourceBundle resources, SignatureQualification qualification,
            boolean hasTimestamps, boolean timestampsQualified) {
        var key = switch (qualification) {
            case QESIG -> !hasTimestamps ? "signature.qualification.QESIG.shortLabel"
                    : timestampsQualified ? "signature.qualification.QESIG.QTSA.shortLabel" : null;
            case QESEAL -> timestampsQualified ? "signature.qualification.QESEAL.shortLabel" : null;
            case ADESIG_QC -> timestampsQualified ? "signature.qualification.ADESIG_QC.shortLabel" : null;
            default -> null;
        };
        return key == null ? null : createValidQualifiedBadge(translate(resources, key));
    }

    private static HBox createMultipleBadges(SignatureQualification signatureQualification, Reports reports,
                                             String signatureId, double prefWrapLength, ResourceBundle resources) {
        var flowPane = new FlowPane(createReadableBadgeFromQualification(signatureQualification, resources));
        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId)) {
            var isQualified = simple.getTimestampQualification(timestamp.getId()) == TimestampQualification.QTSA;
            var isFailed = timestamp.getIndication() == Indication.TOTAL_FAILED
                    || timestamp.getIndication() == Indication.FAILED;
            var isIndeterminate = timestamp.getIndication() == Indication.INDETERMINATE;

            if (isFailed)
                flowPane.getChildren().add(createInvalidBadge(translate(resources, "signature.timestamp.invalid.shortLabel")));

            else if (isIndeterminate)
                flowPane.getChildren().add(createUnknownBadge(translate(resources, "signature.timestamp.unknown.shortLabel")));

            else if (isQualified)
                flowPane.getChildren().add(createValidQualifiedBadge(simple.getTimestampQualification(timestamp.getId()).getReadable()));

            else
                flowPane.getChildren().add(createUnknownBadge(translate(resources, "signature.timestamp.unknown.shortLabel")));
        }

        return wrapMultipleBadges(flowPane, prefWrapLength);
    }

    private static HBox wrapMultipleBadges(FlowPane flowPane, double prefWrapLength) {
        flowPane.getStyleClass().add("autogram-tag-multiple-box");
        flowPane.setMaxHeight(Region.USE_PREF_SIZE);
        if (prefWrapLength > 1) {
            flowPane.setPrefWrapLength(prefWrapLength);
            flowPane.setMaxWidth(prefWrapLength);
        } else {
            flowPane.setMaxWidth(Region.USE_PREF_SIZE);
        }
        var wrapper = new HBox(flowPane);
        wrapper.setMaxWidth(Region.USE_PREF_SIZE);
        wrapper.setMaxHeight(Region.USE_PREF_SIZE);
        return wrapper;
    }

    private static Node createReadableBadgeFromQualification(SignatureQualification qualification, ResourceBundle resources) {
        switch (qualification) {
            case QESIG, QESEAL, ADESIG_QC:
                return createValidQualifiedBadge(qualification.getReadable());
            case ADESIG, ADESEAL, ADESEAL_QC:
                return createCustomValidQualifiedBadge(qualification.getReadable());
            case UNKNOWN_QC, UNKNOWN_QC_QSCD, NOT_ADES_QC, NOT_ADES_QC_QSCD:
                return createUnknownBadge(qualification.getReadable());
            case NOT_ADES, UNKNOWN, NA:
                return createUnknownBadge(translate(resources, "signature.unknown.label"));
            default:
                if (qualification.name().contains("INDETERMINATE"))
                    return createInProgressBadge(qualification.getReadable());
                else
                    return createInvalidBadge(translate(resources, "signature.unknown.label"));
        }
    }

    private static boolean areTimestampsQualified(Reports reports, String signatureId) {
        var simple = reports.getSimpleReport();
        for (var timestamp : simple.getSignatureTimestamps(signatureId))
            if (simple.getTimestampQualification(timestamp.getId()) != TimestampQualification.QTSA)
                return false;

        return true;
    }

    private static boolean areTimestampsFailed(Reports reports, String signatureId) {
        for (var timestamp : reports.getSimpleReport().getSignatureTimestamps(signatureId))
            if (timestamp.getIndication() == Indication.TOTAL_FAILED
                    || timestamp.getIndication() == Indication.FAILED)
                return true;

        return false;
    }
}
