package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.CertificateStatusChecker.Result;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParametersResolver;
import digital.slovensko.autogram.core.TimestampSourceChecker;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Certificate selection with the results of the status and qualification checks, given by the test instead of OCSP,
 * CRL and the trusted lists. Desktop-only like {@link SettingsDialogAccessibilityTest}, run with
 * -Dautogram.guiTests=true.
 */
@EnabledIfSystemProperty(named = "autogram.guiTests", matches = "true")
class PickKeyDialogControllerTest {
    private static final Result QUALIFIED = new Result(CertificateStatus.GOOD, CertificateQualification.QCERT_FOR_ESIG_QSCD);
    private static final Result RECOGNIZED = new Result(CertificateStatus.GOOD, CertificateQualification.QCERT_FOR_ESIG);
    private static final Result NOT_QUALIFIED = new Result(CertificateStatus.GOOD, CertificateQualification.CERT_FOR_UNKNOWN);

    private static DSSPrivateKeyEntry jana;
    private static DSSPrivateKeyEntry peter;
    private static DSSPrivateKeyEntry seal;
    private static DSSPrivateKeyEntry recognized;

    @BeforeAll
    static void startJavaFx() throws Exception {
        var pki = TestPki.create();
        jana = pki.signer(TestPki.JANA).token().getKeys().getFirst();
        peter = pki.signer(TestPki.PETER).token().getKeys().getFirst();
        seal = pki.signer(TestPki.SEAL).token().getKeys().getFirst();
        recognized = pki.issue("Uznaný Podpisovateľ", TestPki.Kind.ADESIG_QC).token().getKeys().getFirst();

        var ready = new FutureTask<Void>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        try {
            Platform.startup(ready);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    void testSignButtonWaitsUntilAllCertificatesAreChecked() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana, peter), settings(false, "SK"));

            assertTrue(dialog.controller().mainButton.isDisabled());
            assertEquals(dialog.i18n("pickKey.certificateStatus.checking.btn"), dialog.controller().mainButton.getText());

            dialog.answer(jana, QUALIFIED);
            assertTrue(dialog.controller().mainButton.isDisabled());

            dialog.answer(peter, QUALIFIED);
            assertFalse(dialog.controller().mainButton.isDisabled());
            assertEquals(dialog.i18n("general.sign.btn.single"), dialog.controller().mainButton.getText());
            assertEquals(List.of("Vlastnoručný podpis"), texts(dialog.badges(jana)));
            assertFalse(dialog.controller().hint.isVisible());
        });
    }

    @Test
    void testExpectedSignatureLevelDistinguishesRecognizedAuthorizationAndQualifiedSignatures() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana, recognized, seal), settings(false, "SK"));
            dialog.answer(jana, QUALIFIED);
            dialog.answer(recognized, RECOGNIZED);
            dialog.answer(seal, new Result(CertificateStatus.GOOD, CertificateQualification.QCERT_FOR_ESEAL_QSCD));

            assertEquals(List.of("Vlastnoručný podpis"), texts(dialog.badges(jana)));
            assertEquals(List.of("Uznaný spôsob autorizácie"), texts(dialog.badges(recognized)));
            assertEquals(List.of("Elektronická pečať"), texts(dialog.badges(seal)));
            assertFalse(dialog.radio(recognized).isDisabled());
            assertFalse(dialog.controller().mainButton.isDisabled());
            assertFalse(dialog.controller().hint.isVisible());
        });
    }

    @Test
    void testRecognizedAuthorizationIsAllowedWithoutEnablingNonQualifiedCertificates() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(recognized), settings(false, "SK"));
            dialog.answer(recognized, RECOGNIZED);

            assertFalse(dialog.radio(recognized).isDisabled());
            assertTrue(dialog.radio(recognized).isSelected());
            assertFalse(dialog.controller().mainButton.isDisabled());
            assertFalse(dialog.controller().hint.isVisible());
        });
    }

    @Test
    void testRevokedRecognizedAuthorizationCannotBePickedEvenWhenNonQualifiedCertificatesAreAllowed() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(recognized), settings(true, "SK"));
            dialog.answer(recognized, new Result(CertificateStatus.REVOKED, CertificateQualification.QCERT_FOR_ESIG));

            assertTrue(dialog.radio(recognized).isDisabled());
            assertFalse(dialog.radio(recognized).isSelected());
            assertTrue(dialog.controller().mainButton.isDisabled());
            assertTrue(texts(dialog.badges(recognized)).contains("Zrušený certifikát"));
        });
    }

    @Test
    void testUnknownRevocationStatusDoesNotHideTheExpectedSignatureLevel() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(recognized), settings(false, "SK"));
            dialog.answer(recognized, new Result(CertificateStatus.UNKNOWN, CertificateQualification.QCERT_FOR_ESIG));

            assertEquals(List.of("Platnosť sa nepodarilo overiť", "Uznaný spôsob autorizácie"), texts(dialog.badges(recognized)));
            assertFalse(dialog.controller().mainButton.isDisabled());
        });
    }

    @Test
    void testSignButtonSaysWhenTrustedListsAreStillLoading() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana), settings(false, "SK"));

            dialog.controller().setTrustedListsLoading(true);
            assertEquals(dialog.i18n("pickKey.trustedListsLoading.btn"), dialog.controller().mainButton.getText());

            dialog.controller().setTrustedListsLoading(false);
            assertEquals(dialog.i18n("pickKey.certificateStatus.checking.btn"), dialog.controller().mainButton.getText());
            assertTrue(dialog.controller().mainButton.isDisabled());
        });
    }

    @Test
    void testRevokedAndNotQualifiedCertificatesCannotBePicked() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana, peter, seal), settings(false, "SK"));
            dialog.answer(jana, QUALIFIED);
            dialog.answer(peter, new Result(CertificateStatus.REVOKED, CertificateQualification.QCERT_FOR_ESIG_QSCD));
            dialog.answer(seal, NOT_QUALIFIED);

            assertFalse(dialog.radio(jana).isDisabled());
            assertTrue(dialog.radio(peter).isDisabled());
            assertTrue(dialog.radio(seal).isDisabled());
            assertFalse(dialog.controller().hint.isVisible());
        });
    }

    @Test
    void testNotQualifiedCertificateCanBePickedWhenAllowed() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana, seal), settings(true, "SK"));
            dialog.answer(jana, QUALIFIED);
            dialog.answer(seal, NOT_QUALIFIED);

            assertFalse(dialog.radio(seal).isDisabled());
            assertEquals(1, dialog.badges(seal).getChildren().size());
        });
    }

    @Test
    void testUnknownStatusAndQualificationDoNotBlockSigning() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana), settings(false, "SK"));
            dialog.answer(jana, new Result(CertificateStatus.UNKNOWN, null));

            assertFalse(dialog.radio(jana).isDisabled());
            assertEquals(2, dialog.badges(jana).getChildren().size());
            assertFalse(dialog.controller().hint.isVisible());
        });
    }

    @Test
    void testUntrustedCertificateIsNotConfusedWithUnavailableQualification() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana), settings(false, "SK"));
            dialog.answer(jana, new Result(CertificateStatus.GOOD, CertificateQualification.NA));

            assertEquals(List.of("Nekvalifikovaný certifikát"), texts(dialog.badges(jana)));
            assertTrue(dialog.radio(jana).isDisabled());
            assertTrue(dialog.controller().mainButton.isDisabled());
            assertTrue(dialog.controller().hint.isVisible());
        });
    }

    @Test
    void testHintNamesIssuerCountryWhoseTrustedListIsTurnedOff() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana, seal), settings(false, "CZ"));
            dialog.answer(jana, NOT_QUALIFIED);
            dialog.answer(seal, NOT_QUALIFIED);

            var slovakia = Country.withTrustedList("SK").orElseThrow().getName(dialog.controller().getResources().getLocale());
            assertTrue(dialog.controller().mainButton.isDisabled());
            assertTrue(dialog.controller().hint.isVisible());
            assertEquals(dialog.i18n("pickKey.hint.countryTurnedOff", slovakia) + " " + dialog.i18n("pickKey.hint.allowNotQualified"),
                    dialog.controller().hintText.getText());
        });
    }

    @Test
    void testGeneralHintWhenIssuerCountryIsTurnedOn() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana), settings(true, "SK"));
            dialog.answer(jana, NOT_QUALIFIED);

            assertTrue(dialog.controller().hint.isVisible());
            assertEquals(dialog.i18n("pickKey.hint.notQualified"), dialog.controller().hintText.getText());
        });
    }

    @Test
    void testQualifiedTimestampSwitchesHandwrittenToNotarizedWithoutChangingTheJobYet() throws Exception {
        onFxThread(() -> {
            var job = job(false, true);
            var source = mock(TSPSource.class);
            var checks = new ArrayList<Consumer<TimestampSourceChecker.Result>>();
            var dialog = open(List.of(jana), settings(false, "SK"), new PickKeyDialogController.TimestampOptions(job, source, checks::add));
            dialog.answer(jana, QUALIFIED);

            assertTrue(checks.isEmpty());
            assertFalse(dialog.controller().addTimestamp.isSelected());
            assertEquals(List.of("Vlastnoručný podpis"), texts(dialog.badges(jana)));

            dialog.controller().addTimestamp.setSelected(true);
            assertEquals(1, checks.size());
            assertTrue(dialog.controller().mainButton.isDisabled());
            checks.getFirst().accept(new TimestampSourceChecker.Result(TimestampQualification.QTSA, source));

            assertEquals(List.of("Osvedčený podpis"), texts(dialog.badges(jana)));
            assertEquals(dialog.i18n("pickKey.timestamp.addQualified"), dialog.controller().addTimestamp.getText());
            assertFalse(dialog.controller().mainButton.isDisabled());
            assertEquals(SignatureProfile.BASELINE_B, job.getParameters().getSignatureProfile());

            dialog.controller().addTimestamp.setSelected(false);
            assertEquals(List.of("Vlastnoručný podpis"), texts(dialog.badges(jana)));
            dialog.controller().addTimestamp.setSelected(true);
            assertEquals(1, checks.size(), "toggling the choice does not request another probe");
            assertEquals(List.of("Osvedčený podpis"), texts(dialog.badges(jana)));
        });
    }

    @Test
    void testTurningTimestampOffDoesNotWaitForTheProbeOrUpgradeOnALateReply() throws Exception {
        onFxThread(() -> {
            var checks = new ArrayList<Consumer<TimestampSourceChecker.Result>>();
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job(false, true), source, checks::add));
            dialog.answer(jana, QUALIFIED);
            dialog.controller().addTimestamp.setSelected(true);
            dialog.controller().addTimestamp.setSelected(false);

            assertFalse(dialog.controller().mainButton.isDisabled());
            checks.getFirst().accept(new TimestampSourceChecker.Result(TimestampQualification.QTSA, source));
            assertEquals(List.of("Vlastnoručný podpis"), texts(dialog.badges(jana)));
            assertFalse(dialog.controller().mainButton.isDisabled());
        });
    }

    @Test
    void testNonQualifiedAndUnknownSourcesDoNotPredictANotarizedSignature() throws Exception {
        onFxThread(() -> {
            for (var qualification : new TimestampQualification[]{TimestampQualification.TSA, null}) {
                var source = mock(TSPSource.class);
                var dialog = open(List.of(jana), settings(false, "SK"),
                        new PickKeyDialogController.TimestampOptions(job(true, true), source,
                                answer -> answer.accept(new TimestampSourceChecker.Result(qualification, source))));
                dialog.answer(jana, QUALIFIED);

                assertEquals(List.of("QESig", "Neznáma ČP"), texts(dialog.badges(jana)));
                assertEquals(dialog.i18n("pickKey.timestamp.add"), dialog.controller().addTimestamp.getText());
                assertFalse(dialog.controller().mainButton.isDisabled());
                assertEquals(dialog.i18n(qualification == null ? "pickKey.timestamp.unknown" : "pickKey.timestamp.notQualified"),
                        dialog.controller().timestampStatus.getText());
            }
        });
    }

    @Test
    void testOnlyConfirmedChoiceChangesThisJobAndNeverTheDefaultSettings() throws Exception {
        onFxThread(() -> {
            var job = job(false, true);
            var settings = settings(false, "SK");
            settings.setTsaEnabled(false);
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings,
                    new PickKeyDialogController.TimestampOptions(job, source,
                            answer -> answer.accept(new TimestampSourceChecker.Result(TimestampQualification.QTSA, source))));
            dialog.answer(jana, QUALIFIED);
            dialog.controller().addTimestamp.setSelected(true);
            var stage = new Stage();
            stage.setScene(new Scene(dialog.root()));
            dialog.controller().onPickCertificateButtonAction(null);

            assertEquals(SignatureProfile.BASELINE_T, job.getParameters().getSignatureProfile());
            assertFalse(settings.getTsaEnabled());
        });
    }

    @Test
    void testClosingPickerDoesNotApplyTheTimestampChoice() throws Exception {
        onFxThread(() -> {
            var job = job(false, true);
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job, source,
                            answer -> answer.accept(new TimestampSourceChecker.Result(TimestampQualification.QTSA, source))));
            dialog.answer(jana, QUALIFIED);
            dialog.controller().addTimestamp.setSelected(true);
            var stage = new Stage();
            stage.setScene(new Scene(dialog.root()));
            stage.close();

            assertEquals(SignatureProfile.BASELINE_B, job.getParameters().getSignatureProfile());
        });
    }

    @Test
    void testMissingSourceCanBeResolvedByTurningTimestampOff() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job(true, true), null,
                            answer -> fail("a missing source must not be probed")));
            dialog.answer(jana, QUALIFIED);

            assertTrue(dialog.controller().mainButton.isDisabled());
            assertEquals(dialog.i18n("pickKey.timestamp.missingSource"), dialog.controller().timestampStatus.getText());
            dialog.controller().addTimestamp.setSelected(false);
            assertFalse(dialog.controller().mainButton.isDisabled());
        });
    }

    @Test
    void testTimestampRequiredByAWebsiteIsShownButCannotBeTurnedOff() throws Exception {
        onFxThread(() -> {
            var job = job(true, false);
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job, source,
                            answer -> answer.accept(new TimestampSourceChecker.Result(TimestampQualification.QTSA, source))));
            dialog.answer(jana, QUALIFIED);

            assertTrue(dialog.controller().addTimestamp.isSelected());
            assertTrue(dialog.controller().addTimestamp.isDisabled());
            assertEquals(dialog.i18n("pickKey.timestamp.fixed"), dialog.controller().timestampHint.getText());
            assertEquals(List.of("Osvedčený podpis"), texts(dialog.badges(jana)));
            assertEquals(SignatureProfile.BASELINE_T, job.getParameters().getSignatureProfile());
        });
    }

    private static SigningJob job(boolean timestamp, boolean local) {
        var document = AutogramDocument.build(new InMemoryDocument("test".getBytes(), "test.txt", MimeTypeEnum.TEXT), null);
        var parameters = SigningParametersResolver.buildRequested(timestamp ? SignatureProfile.BASELINE_T : SignatureProfile.BASELINE_B,
                SignatureForm.XAdES, DigestAlgorithm.SHA256, ASiCContainerType.ASiC_E, SignaturePackaging.ENVELOPING,
                true, null, null, null, false, 640, true);
        var input = SigningInput.fromDocument(document, parameters);
        return local ? SigningJob.fromLocalInput(input, null) : SigningJob.fromInput(input, null);
    }

    private static UserSettings settings(boolean nonQualifiedCertsEnabled, String trustedCountry) {
        var settings = UserSettings.load();
        settings.setNonQualifiedCertsEnabled(nonQualifiedCertsEnabled);
        settings.getTrustedList().clear();
        settings.addToTrustedList(trustedCountry);
        return settings;
    }

    private static List<String> texts(Node node) {
        if (node instanceof Text text)
            return List.of(text.getText());
        if (node instanceof Parent parent)
            return parent.getChildrenUnmodifiable().stream().flatMap(child -> texts(child).stream()).toList();
        return List.of();
    }

    private static Dialog open(List<DSSPrivateKeyEntry> keys, UserSettings settings) {
        return open(keys, settings, null);
    }

    private static Dialog open(List<DSSPrivateKeyEntry> keys, UserSettings settings, PickKeyDialogController.TimestampOptions timestampOptions) {
        var answers = new HashMap<DSSPrivateKeyEntry, Consumer<Result>>();
        var controller = new PickKeyDialogController(keys, key -> { }, settings, answers::put, () -> { }, timestampOptions);
        var root = (Parent) GUIUtils.loadFXML(controller, "pick-key-dialog.fxml");
        return new Dialog(controller, root, answers);
    }

    private static void onFxThread(FxAction action) throws Exception {
        var task = new FutureTask<Void>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private interface FxAction {
        void run() throws Exception;
    }

    private record Dialog(PickKeyDialogController controller, Parent root, Map<DSSPrivateKeyEntry, Consumer<Result>> answers) {
        void answer(DSSPrivateKeyEntry key, Result result) {
            answers.get(key).accept(result);
        }

        String i18n(String key, Object... args) {
            return controller.i18n(key, args);
        }

        RadioButton radio(DSSPrivateKeyEntry key) {
            return row(key).getChildren().stream().map(RadioButton.class::cast).findFirst().orElseThrow();
        }

        HBox badges(DSSPrivateKeyEntry key) {
            return (HBox) row(key).getChildren().get(1);
        }

        private HBox row(DSSPrivateKeyEntry key) {
            return controller.radios.getChildren().stream()
                    .map(HBox.class::cast)
                    .filter(row -> row.getChildren().getFirst().getUserData() == key)
                    .findFirst().orElseThrow();
        }
    }
}
