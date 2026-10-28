package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.SignatureValidator.FailedTLs;
import digital.slovensko.autogram.core.TestSignedDocuments;
import digital.slovensko.autogram.core.ValidationReports;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The yellow warning above validated signatures, see {@link GUIValidationUtils#addTrustedListsWarning}. */
class TrustedListsWarningTest {
    private static final ResourceBundle SK = bundle("sk");
    private static final ResourceBundle EN = bundle("en");

    @Test
    void noneLoaded() {
        assertEquals(SK.getString("signing.tlsLoading.error"),
                GUIValidationUtils.trustedListsWarning(SK, false, new FailedTLs(0, 0, Set.of()), false));
        assertEquals(SK.getString("signing.tlsLoading.error"),
                GUIValidationUtils.trustedListsWarning(SK, false, new FailedTLs(8, 8, Set.of("BE", "CZ", "SK")), true));
    }

    @Test
    void someNotLoadedAreCountedNotListed() {
        assertEquals("Nepodarilo sa stiahnuť 1 z 8 dôveryhodných zoznamov a niektoré podpisy nie je možné úplne overiť.",
                GUIValidationUtils.trustedListsWarning(SK, true, new FailedTLs(1, 8, Set.of("BE")), true));
        assertEquals("1 of 8 trusted lists could not be downloaded and some signatures cannot be fully validated.",
                GUIValidationUtils.trustedListsWarning(EN, true, new FailedTLs(1, 8, Set.of("BE")), true));
    }

    @Test
    void someNotLoadedButAllSignaturesVerified() {
        assertNull(GUIValidationUtils.trustedListsWarning(SK, true, new FailedTLs(1, 8, Set.of("BE")), false));
    }

    @Test
    void onlySignaturesThatCouldNotBeVerifiedCount() {
        var pki = TestPki.create();
        SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
        try {
            var valid = TestSignedDocuments.sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_B, null);
            var unknownCa = TestSignedDocuments.sign(TestPki.create().signer(TestPki.JANA), SignatureProfile.BASELINE_B, null);

            assertFalse(GUIValidationUtils.hasNotVerifiedSignatures(signatures(valid)));
            assertTrue(GUIValidationUtils.hasNotVerifiedSignatures(signatures(unknownCa)));
            // invalid for sure, the trusted lists would not change that
            assertFalse(GUIValidationUtils.hasNotVerifiedSignatures(signatures(TestSignedDocuments.tamper(valid))));
        } finally {
            SignatureValidator.getInstance().useCustomVerifier(null);
        }
    }

    @Test
    void allLoaded() {
        assertNull(GUIValidationUtils.trustedListsWarning(SK, true, new FailedTLs(0, 8, Set.of()), true));
    }

    private static List<ValidationReports.SignatureEntry> signatures(DSSDocument signed) {
        var report = TestSignedDocuments.validate(signed);
        return report.reports().getSimpleReport().getSignatureIdList().stream()
                .map(id -> new ValidationReports.SignatureEntry(report, id))
                .toList();
    }

    private static ResourceBundle bundle(String language) {
        return ResourceBundle.getBundle("digital.slovensko.autogram.ui.gui.language.l10n", Locale.forLanguageTag(language));
    }
}
