package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.SignatureValidator;
import digital.slovensko.autogram.core.TestSignedDocuments;
import digital.slovensko.autogram.core.ValidationReports;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What the signing window shows for a signature: the short badge next to the signer and the rows of the signature
 * details, for signatures made with the offline {@link TestPki}. Badges are read as "style: label" (the style is the
 * autogram-tag-* class without the prefix). The details box is checked only without timestamps, because the
 * timestamp rows contain buttons, which need a running JavaFX toolkit.
 */
class SignatureBadgesTest {
    private static final ResourceBundle SK = bundle("sk");
    private static TestPki pki;

    @BeforeAll
    static void useTestPki() {
        pki = TestPki.create();
        SignatureValidator.getInstance().useCustomVerifier(pki.verifier());
    }

    @AfterAll
    static void useTrustedLists() {
        SignatureValidator.getInstance().useCustomVerifier(null);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            QCERT_FOR_ESIG_QSCD    | valid: Kvalifikovaný elektronický podpis
            QCERT_FOR_ESIG         | valid: Uznaný spôsob autorizácie
            QCERT_FOR_ESEAL_QSCD   | valid: Kvalifikovaná elektronická pečať
            QCERT_FOR_ESEAL        | custom-valid: Zdokonalená elektronická pečať
            QCERT_FOR_UNKNOWN_QSCD | warning: Kvalifikovaný certifikát neznámeho typu
            QCERT_FOR_UNKNOWN      | warning: Kvalifikovaný certifikát neznámeho typu
            QCERT_FOR_WSA          | warning: Nekvalifikovaný certifikát
            CERT_FOR_ESIG          | warning: Nekvalifikovaný certifikát
            CERT_FOR_ESEAL         | warning: Nekvalifikovaný certifikát
            CERT_FOR_WSA           | warning: Nekvalifikovaný certifikát
            CERT_FOR_UNKNOWN       | warning: Nekvalifikovaný certifikát
            NA                     | warning: Nekvalifikovaný certifikát
            """)
    void certificateBadgeShowsExpectedSignatureLevel(CertificateQualification qualification, String expected) {
        var badge = SignatureBadgeFactory.createBadgeFromCertificateQualification(qualification, SK);

        assertEquals(List.of("[" + expected + "]"), flatten(badge));
    }

    @Test
    void unavailableCertificateQualificationIsNotShownAsNotQualified() {
        var badge = SignatureBadgeFactory.createBadgeFromCertificateQualification(null, SK);

        assertEquals(List.of("[warning: Kvalifikáciu sa nepodarilo overiť]"), flatten(badge));
    }

    @Test
    void recognizedAuthorizationCertificateBadgeIsTranslated() {
        var badge = SignatureBadgeFactory.createBadgeFromCertificateQualification(CertificateQualification.QCERT_FOR_ESIG, bundle("en"));

        assertEquals(List.of("[valid: Advanced Electronic Signature]"), flatten(badge));
    }

    @Test
    void qualifiedCertificateWithoutQscdGivesRecognizedAuthorizationAfterSigning() {
        var signer = pki.issue("Uznaný Podpisovateľ", TestPki.Kind.ADESIG_QC);
        var report = validate(sign(signer, null));

        assertEquals(List.of("valid: Uznaný spôsob autorizácie"), badge(report, SK));
    }

    @Test
    void qualifiedSignatureIsHandwritten() {
        var report = validate(sign(pki.signer(TestPki.JANA), null));

        assertEquals(List.of("valid: Vlastnoručný podpis"), badge(report, SK));
        assertEquals("Platný", row(details(report, true, true), "Výsledok overenia"));
        assertEquals("[valid: Kvalifikovaný elektronický podpis]", row(details(report, true, true), "Typ podpisu"));
    }

    @Test
    void qualifiedSignatureWithQualifiedTimestampIsNotarized() {
        var report = validate(sign(pki.signer(TestPki.JANA), pki.tspSource()));

        assertEquals(List.of("valid: Osvedčený podpis"), badge(report, SK));
        assertEquals(List.of("valid: QES + QTS"), badge(report, bundle("en")));
    }

    @Test
    void nonQualifiedTimestampGetsItsOwnBadge() {
        var report = validate(sign(pki.signer(TestPki.JANA), pki.nonQualifiedTspSource()));

        // the qualification badge shows DSS's readable name, it is not translated
        assertEquals(List.of("valid: QESig", "unknown: Neznáma ČP"), badge(report, SK));
    }

    @Test
    void qualifiedSealShowsOnlyTheSealQualification() {
        var withoutTimestamp = validate(sign(pki.signer(TestPki.SEAL), null));
        var withQualifiedTimestamp = validate(sign(pki.signer(TestPki.SEAL), pki.tspSource()));

        assertEquals(List.of("valid: Elektronická pečať"), badge(withoutTimestamp, SK));
        assertEquals(List.of("valid: Elektronická pečať"), badge(withQualifiedTimestamp, SK));
        assertEquals("[valid: Kvalifikovaná elektronická pečať]", row(details(withoutTimestamp, true, true), "Typ podpisu"));
    }

    @Test
    void sealWithNonQualifiedTimestampGetsBothBadges() {
        var report = validate(sign(pki.signer(TestPki.SEAL), pki.nonQualifiedTspSource()));

        assertEquals(List.of("valid: QESeal", "unknown: Neznáma ČP"), badge(report, SK));
    }

    @Test
    void certificateWithoutQcStatementsIsUnknownSignature() {
        var report = validate(sign(pki.issue("Nekvalifikovaný Podpisovateľ", TestPki.Kind.NOT_QUALIFIED), null));

        assertEquals(List.of("unknown: Neznámy podpis"), badge(report, SK));
        assertEquals("Platný", row(details(report, true, true), "Výsledok overenia"));
    }

    @Test
    void revokedCertificateIsOnlyIndeterminate() {
        var signer = pki.issue("Zneplatnený Podpisovateľ", TestPki.Kind.QESIG);
        var signed = sign(signer, null);
        pki.revoke(signer);
        var report = validate(signed);

        // no proof that the signature is older than the revocation (REVOKED_NO_POE), so DSS cannot reject it
        assertEquals(List.of("processing: Predbežne platný kvalifikovaný podpis"), badge(report, SK));
        assertEquals("Predbežne platný", row(details(report, true, true), "Výsledok overenia"));
    }

    @Test
    void expiredCertificateIsOnlyIndeterminate() {
        var now = System.currentTimeMillis();
        var signer = pki.issue("Expirovaný Podpisovateľ", TestPki.Kind.QESIG,
                new Date(now - 400 * 86_400_000L), new Date(now - 30 * 86_400_000L));
        var report = validate(sign(signer, null));

        assertEquals(List.of("processing: Predbežne platný iný zdokonalený podpis"), badge(report, SK));
        assertEquals("Predbežne platný", row(details(report, true, true), "Výsledok overenia"));
    }

    @Test
    void documentChangedAfterSigningIsInvalid() {
        var report = validate(TestSignedDocuments.tamper(sign(pki.signer(TestPki.JANA), null)));
        var details = details(report, true, true);

        assertEquals("[invalid: Neplatný podpis]", row(details, "Jana Vzorová"));
        assertEquals("Neplatný", row(details, "Výsledok overenia"));
    }

    @Test
    void signatureBeforeValidationIsInProgress() {
        var details = details(validate(sign(pki.signer(TestPki.JANA), null)), false, true);

        assertEquals("[processing: Prebieha overovanie…]", row(details, "Jana Vzorová"));
        assertEquals("Prebieha overovanie", row(details, "Výsledok overenia"));
    }

    @Test
    void signatureWithoutTrustedListsCannotBeValidated() {
        var details = details(validate(sign(pki.signer(TestPki.JANA), null)), true, false);

        assertEquals("[valid: Vlastnoručný podpis]", row(details, "Jana Vzorová"));
        assertEquals("Nepodarilo sa overiť", row(details, "Výsledok overenia"));
    }

    private static DSSDocument sign(TestPki.Signer signer, eu.europa.esig.dss.spi.x509.tsp.TSPSource tspSource) {
        return TestSignedDocuments.sign(signer, tspSource == null ? SignatureProfile.BASELINE_B : SignatureProfile.BASELINE_T, tspSource);
    }

    private static ValidationReports.DocumentReport validate(DSSDocument signed) {
        return TestSignedDocuments.validate(signed);
    }

    private static String signatureId(ValidationReports.DocumentReport report) {
        return report.reports().getSimpleReport().getSignatureIdList().get(0);
    }

    /** The short badge(s) shown next to the signer in the signatures table. */
    private static List<String> badge(ValidationReports.DocumentReport report, ResourceBundle resources) {
        var id = signatureId(report);
        var node = SignatureBadgeFactory.createCombinedBadgeFromQualification(resources,
                report.reports().getDetailedReport().getSignatureQualification(id), report.reports(), id, 0);
        var out = new ArrayList<String>();
        for (var item : flatten(node))
            if (item.startsWith("["))
                out.add(item.substring(1, item.length() - 1));
        return out;
    }

    /** The signature details box in Slovak, flattened to texts in reading order; badges as "[style: label]". */
    private static List<String> details(ValidationReports.DocumentReport report, boolean isValidated, boolean areTLsLoaded) {
        return flatten(GUIValidationUtils.createSignatureBox(SK, report, isValidated, signatureId(report), ignored -> {}, areTLsLoaded));
    }

    /** The item right after the label: the value of a details row, or the badge after the signer's name. */
    private static String row(List<String> items, String label) {
        var index = items.indexOf(label);
        if (index < 0 || index + 1 >= items.size())
            throw new AssertionError("no " + label + " in " + items);
        return items.get(index + 1);
    }

    private static List<String> flatten(Node node) {
        var out = new ArrayList<String>();
        flatten(node, out);
        return out;
    }

    private static void flatten(Node node, List<String> out) {
        if (node.getStyleClass().contains("autogram-tag")) {
            var style = node.getStyleClass().stream().filter(c -> c.startsWith("autogram-tag-")).findFirst().orElse("autogram-tag-");
            out.add("[" + style.substring("autogram-tag-".length()) + ": " + String.join(" ", texts(node)) + "]");
            return;
        }
        if (node instanceof Text text)
            out.add(text.getText());
        if (node instanceof Parent parent)
            for (var child : parent.getChildrenUnmodifiable())
                flatten(child, out);
    }

    private static List<String> texts(Node node) {
        var out = new ArrayList<String>();
        if (node instanceof Text text)
            out.add(text.getText());
        if (node instanceof Parent parent)
            for (var child : parent.getChildrenUnmodifiable())
                out.addAll(texts(child));
        return out;
    }

    private static ResourceBundle bundle(String language) {
        return ResourceBundle.getBundle("digital.slovensko.autogram.ui.gui.language.l10n", Locale.forLanguageTag(language));
    }
}
