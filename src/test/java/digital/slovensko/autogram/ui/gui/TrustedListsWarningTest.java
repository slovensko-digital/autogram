package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.SignatureValidator.FailedTLs;
import digital.slovensko.autogram.core.TestSignedDocuments;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.Indication;
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
                GUIValidationUtils.trustedListsWarning(SK, false, new FailedTLs(0, 0, Set.of()), () -> false));
        assertEquals(SK.getString("signing.tlsLoading.error"),
                GUIValidationUtils.trustedListsWarning(SK, false, new FailedTLs(8, 8, Set.of("BE", "CZ", "SK")), () -> true));
    }

    @Test
    void someNotLoadedAreCountedNotListed() {
        assertEquals("Nepodarilo sa stiahnuť 1 z 8 dôveryhodných zoznamov a niektoré podpisy nie je možné úplne overiť.",
                GUIValidationUtils.trustedListsWarning(SK, true, new FailedTLs(1, 8, Set.of("BE")), () -> true));
        assertEquals("1 of 8 trusted lists could not be downloaded and some signatures cannot be fully validated.",
                GUIValidationUtils.trustedListsWarning(EN, true, new FailedTLs(1, 8, Set.of("BE")), () -> true));
    }

    @Test
    void someNotLoadedButAllSignaturesVerified() {
        assertNull(GUIValidationUtils.trustedListsWarning(SK, true, new FailedTLs(1, 8, Set.of("BE")), () -> false));
    }

    @Test
    void onlySignaturesNotVerifiedBecauseOfMissingListsCount() {
        var pki = TestPki.create();
        SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
        try {
            var valid = TestSignedDocuments.sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_B, null);
            // the test CAs are Slovak
            var unknownCa = TestSignedDocuments.sign(TestPki.create().signer(TestPki.JANA), SignatureProfile.BASELINE_B, null);
            var unknownTsa = TestSignedDocuments.sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_T,
                    TestPki.create().tspSource());

            assertFalse(GUIValidationUtils.hasNotVerifiedSignatures(signatures(valid), Set.of("SK")));
            assertTrue(GUIValidationUtils.hasNotVerifiedSignatures(signatures(unknownCa), Set.of("SK")));
            assertFalse(GUIValidationUtils.hasNotVerifiedSignatures(signatures(unknownCa), Set.of("BE")));
            var signedWithUnknownTsa = signatures(unknownTsa).getFirst();
            assertEquals(Indication.TOTAL_PASSED, signedWithUnknownTsa.documentReport().reports().getSimpleReport()
                    .getIndication(signedWithUnknownTsa.signatureId()), "only the timestamp is not verified");
            assertTrue(GUIValidationUtils.hasNotVerifiedSignatures(signatures(unknownTsa), Set.of("SK")));
            assertFalse(GUIValidationUtils.hasNotVerifiedSignatures(signatures(unknownTsa), Set.of("BE")));
            // invalid for sure, the trusted lists would not change that
            assertFalse(GUIValidationUtils.hasNotVerifiedSignatures(signatures(TestSignedDocuments.tamper(valid)), Set.of("SK")));
        } finally {
            SignatureValidator.getInstance().useCustomVerifier(null);
        }
    }

    @Test
    void countriesOfNotVerifiedSignaturesWhoseListsAreTurnedOff() {
        var pki = TestPki.create();
        SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
        try {
            var valid = TestSignedDocuments.sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_B, null);
            var german = TestSignedDocuments.sign(TestPki.create("DE").issue("Erika Mustermann", TestPki.Kind.QESIG),
                    SignatureProfile.BASELINE_B, null);
            var germanTsa = TestSignedDocuments.sign(pki.signer(TestPki.JANA), SignatureProfile.BASELINE_T,
                    TestPki.create("DE").tspSource());

            assertEquals(List.of("DE"), shortnames(GUIValidationUtils.countriesTurnedOff(signatures(german), List.of("SK", "CZ"))));
            assertEquals(List.of(), shortnames(GUIValidationUtils.countriesTurnedOff(signatures(german), List.of("SK", "DE"))));
            assertEquals(List.of("DE"), shortnames(GUIValidationUtils.countriesTurnedOff(signatures(germanTsa), List.of("SK"))));
            assertEquals(List.of(), shortnames(GUIValidationUtils.countriesTurnedOff(signatures(valid), List.of("CZ"))));
        } finally {
            SignatureValidator.getInstance().useCustomVerifier(null);
        }
    }

    @Test
    void countriesTurnedOffAreNamed() {
        var belgium = Country.withTrustedList("BE").orElseThrow();
        var czechia = Country.withTrustedList("CZ").orElseThrow();
        assertEquals("Autogram nevie úplne overiť podpisy od poskytovateľov z krajiny Belgicko, lebo jej dôveryhodný zoznam nemáte zapnutý.",
                GUIValidationUtils.countriesTurnedOffWarning(SK, List.of(belgium)));
        assertEquals("Autogram nevie úplne overiť podpisy od poskytovateľov z krajín Belgicko, Česko, lebo ich dôveryhodné zoznamy nemáte zapnuté.",
                GUIValidationUtils.countriesTurnedOffWarning(SK, List.of(belgium, czechia)));
        assertEquals("Autogram cannot fully validate signatures from providers in Belgium, because its trusted list is turned off.",
                GUIValidationUtils.countriesTurnedOffWarning(EN, List.of(belgium)));
        assertEquals("Autogram cannot fully validate signatures from providers in Belgium, Czechia, because their trusted lists are turned off.",
                GUIValidationUtils.countriesTurnedOffWarning(EN, List.of(belgium, czechia)));
    }

    @Test
    void allLoaded() {
        assertNull(GUIValidationUtils.trustedListsWarning(SK, true, new FailedTLs(0, 8, Set.of()), () -> true));
    }

    private static List<ValidationReports.SignatureEntry> signatures(DSSDocument signed) {
        var report = TestSignedDocuments.validate(signed);
        return report.reports().getSimpleReport().getSignatureIdList().stream()
                .map(id -> new ValidationReports.SignatureEntry(report, id))
                .toList();
    }

    private static List<String> shortnames(List<Country> countries) {
        return countries.stream().map(Country::getShortname).toList();
    }

    private static ResourceBundle bundle(String language) {
        return ResourceBundle.getBundle("digital.slovensko.autogram.ui.gui.language.l10n", Locale.forLanguageTag(language));
    }
}
