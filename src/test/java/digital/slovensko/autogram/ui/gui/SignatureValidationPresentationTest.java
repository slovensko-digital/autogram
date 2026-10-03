package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import static eu.europa.esig.dss.enumerations.SignatureForm.PAdES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignatureValidationPresentationTest {
    @Test
    void indeterminateQualificationIsNotPresentedAsValidationInProgress() {
        var resources = SupportedLanguage.ENGLISH.loadResources();
        var badge = (HBox) SignatureBadgeFactory.createBadgeFromQualification(
                SignatureQualification.INDETERMINATE_QESIG, PAdES, resources);

        assertTrue(badge.getStyleClass().contains("autogram-tag-unknown"));
    }

    @Test
    void successfulCryptographicCheckIsNotEnoughWhenTrustedListIsMissing() {
        var resources = SupportedLanguage.ENGLISH.loadResources();

        assertEquals("Validation failed", GUIValidationUtils.validityToString(true, false, false, true,
                SignatureQualification.QESIG, PAdES, false, false, resources));
    }

    @Test
    void indeterminateTimestampDoesNotProduceValidResult() {
        var resources = SupportedLanguage.ENGLISH.loadResources();

        assertEquals("Indeterminate", GUIValidationUtils.validityToString(true, false, true, true,
                SignatureQualification.QESIG, PAdES, false, true, resources));
    }

    @Test
    void qualifiedBadgeIsNotShownWithoutAllSelectedTrustedLists() {
        var resources = SupportedLanguage.ENGLISH.loadResources();
        var reports = mock(Reports.class);
        var detailed = mock(eu.europa.esig.dss.detailedreport.DetailedReport.class);
        var simple = mock(eu.europa.esig.dss.simplereport.SimpleReport.class);
        when(reports.getDetailedReport()).thenReturn(detailed);
        when(reports.getSimpleReport()).thenReturn(simple);
        when(detailed.getBasicValidationIndication("sig")).thenReturn(Indication.TOTAL_PASSED);
        when(detailed.getSignatureQualification("sig")).thenReturn(SignatureQualification.QESIG);
        when(simple.isValid("sig")).thenReturn(true);
        var document = new ValidationReports.DocumentReport(0, mock(eu.europa.esig.dss.model.DSSDocument.class), reports);

        var badge = (HBox) GUIValidationUtils.createSignatureQualificationBadge(
                resources, document, true, "sig", 0, false);

        assertTrue(badge.getStyleClass().contains("autogram-tag-unknown"));
    }
}
