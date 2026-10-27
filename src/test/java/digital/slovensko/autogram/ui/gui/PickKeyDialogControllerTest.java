package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CertificateStatusChecker.Result;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.SigningParametersResolver;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.settings.Country;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
            assertTrue(dialog.badges(jana).getChildren().isEmpty());

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
    void testQualifiedCertificateHasTwoGoalsWithoutRequestingAnyTrialTimestamp() throws Exception {
        onFxThread(() -> {
            var job = job(false, true);
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings(false, "SK"), new PickKeyDialogController.TimestampOptions(job, source));
            assertTrue(dialog.badges(jana, true).getChildren().isEmpty());
            assertTrue(dialog.badges(jana, false).getChildren().isEmpty());
            assertTrue(dialog.controller().mainButton.isDisabled());
            dialog.answer(jana, QUALIFIED);

            assertEquals(2, dialog.controller().radios.getChildren().size());
            assertEquals(List.of("Osvedčený podpis"), texts(dialog.badges(jana, true)));
            assertEquals(List.of("Vlastnoručný podpis"), texts(dialog.badges(jana, false)));
            assertTrue(dialog.description(jana, true).isEmpty());
            assertTrue(dialog.description(jana, false).isEmpty());
            assertTrue(dialog.radio(jana, true).getAccessibleText().contains("Osvedčený podpis"));
            assertTrue(dialog.radio(jana, false).getAccessibleText().contains("Vlastnoručný podpis"));
            assertTrue(dialog.radio(jana, true).isSelected());
            dialog.radio(jana, false).setSelected(true);
            assertFalse(dialog.controller().mainButton.isDisabled());
            assertEquals(SignatureProfile.BASELINE_B, job.getParameters().getSignatureProfile());
            verifyNoInteractions(source);
        });
    }

    @Test
    void testRecognizedAuthorizationUsesTheSameBadgeForBothTimestampOptions() throws Exception {
        onFxThread(() -> {
            var source = mock(TSPSource.class);
            var dialog = open(List.of(recognized), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job(false, true), source));
            dialog.answer(recognized, RECOGNIZED);

            assertEquals(List.of("Uznaný spôsob autorizácie"), texts(dialog.badges(recognized, true)));
            assertEquals(List.of("Uznaný spôsob autorizácie"), texts(dialog.badges(recognized, false)));
            assertEquals("S časovou pečiatkou", dialog.description(recognized, true));
            assertEquals("Bez časovej pečiatky", dialog.description(recognized, false));
            assertTrue(dialog.radio(recognized, true).getAccessibleText().contains("S časovou pečiatkou"));
            assertFalse(dialog.controller().mainButton.isDisabled());
            verifyNoInteractions(source);
        });
    }

    @Test
    void testEachCertificateIsCheckedOnceAndRevocationDisablesBothChoices() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana, peter), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job(true, true), mock(TSPSource.class)));
            assertEquals(2, dialog.answers().size());
            assertEquals(4, dialog.controller().radios.getChildren().size());
            dialog.answer(jana, QUALIFIED);
            assertTrue(dialog.controller().mainButton.isDisabled());
            dialog.answer(peter, new Result(CertificateStatus.REVOKED, CertificateQualification.QCERT_FOR_ESIG_QSCD));
            assertTrue(dialog.radio(peter, true).isDisabled());
            assertTrue(dialog.radio(peter, false).isDisabled());
            assertFalse(dialog.radio(jana, true).isDisabled());
            assertFalse(dialog.controller().mainButton.isDisabled());
        });
    }

    @Test
    void testOnlyConfirmedChoiceChangesThisJobAndNeverTheDefaultSettings() throws Exception {
        onFxThread(() -> {
            var job = job(false, true);
            var settings = settings(false, "SK");
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings,
                    new PickKeyDialogController.TimestampOptions(job, source));
            dialog.answer(jana, QUALIFIED);
            dialog.radio(jana, true).setSelected(true);
            var stage = new Stage();
            stage.setScene(new Scene(dialog.root()));
            dialog.controller().onPickCertificateButtonAction(null);

            assertEquals(SignatureProfile.BASELINE_T, job.getParameters().getSignatureProfile());
            assertEquals(SignatureProfile.BASELINE_B, settings.getDefaultSigningParameters().getSignatureProfile());
        });
    }

    @Test
    void testClosingPickerDoesNotApplyTheTimestampChoice() throws Exception {
        onFxThread(() -> {
            var job = job(false, true);
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job, source));
            dialog.answer(jana, QUALIFIED);
            dialog.radio(jana, true).setSelected(true);
            var stage = new Stage();
            stage.setScene(new Scene(dialog.root()));
            stage.close();

            assertEquals(SignatureProfile.BASELINE_B, job.getParameters().getSignatureProfile());
        });
    }

    @Test
    void testLocalDefaultDoesNotDependOnThePreparedTimestampProfile() throws Exception {
        onFxThread(() -> {
            for (var timestamp : new boolean[]{false, true}) {
                var dialog = open(List.of(jana), settings(false, "SK"),
                        new PickKeyDialogController.TimestampOptions(job(timestamp, true), mock(TSPSource.class)));
                dialog.answer(jana, QUALIFIED);
                assertTrue(dialog.radio(jana, true).isSelected());
                assertFalse(dialog.radio(jana, false).isSelected());
            }
        });
    }

    @Test
    void testMissingSourceCanBeResolvedByChoosingHandwrittenSignature() throws Exception {
        onFxThread(() -> {
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job(true, true), null));
            dialog.answer(jana, QUALIFIED);

            assertTrue(dialog.controller().mainButton.isDisabled());
            assertEquals(dialog.i18n("pickKey.timestamp.missingSource"), dialog.controller().timestampStatus.getText());
            dialog.radio(jana, false).setSelected(true);
            assertFalse(dialog.controller().mainButton.isDisabled());
            assertFalse(dialog.controller().timestampStatus.getParent().isVisible());
        });
    }

    @Test
    void testWebsiteHasOneFixedChoiceAndDoesNotPromiseTimestampQualification() throws Exception {
        onFxThread(() -> {
            var job = job(true, false);
            var source = mock(TSPSource.class);
            var dialog = open(List.of(jana), settings(false, "SK"),
                    new PickKeyDialogController.TimestampOptions(job, source));
            dialog.answer(jana, QUALIFIED);

            assertEquals(1, dialog.controller().radios.getChildren().size());
            assertTrue(dialog.radio(jana, true).isSelected());
            assertEquals(List.of("QESig", "Neznáma ČP"), texts(dialog.badges(jana)));
            assertEquals(SignatureProfile.BASELINE_T, job.getParameters().getSignatureProfile());
        });
    }

    @Test
    void testRememberedCertificateStillRequiresChoosingTheGoalBeforeEveryLocalSignature() throws Exception {
        onFxThread(() -> {
            var gui = mock(GUI.class);
            var autogram = mock(Autogram.class);
            var key = mock(SigningKey.class);
            var job = job(false, true);
            when(gui.getActiveSigningKey()).thenReturn(key);
            var controller = new SigningDialogController(job, autogram, gui, "Test", settings(false, "SK"));
            controller.mainBox = new VBox();

            controller.sign();

            var callback = ArgumentCaptor.forClass(Runnable.class);
            verify(gui).pickSignatureAndThen(eq(key), eq(job), callback.capture());
            verifyNoInteractions(autogram);
            callback.getValue().run();
            verify(autogram).sign(job, key);
        });
    }

    @Test
    void testRememberedCertificateKeepsTheExistingWebsiteSigningFlow() throws Exception {
        onFxThread(() -> {
            var gui = mock(GUI.class);
            var autogram = mock(Autogram.class);
            var key = mock(SigningKey.class);
            var job = job(true, false);
            when(gui.getActiveSigningKey()).thenReturn(key);
            var controller = new SigningDialogController(job, autogram, gui, "Test", settings(false, "SK"));
            controller.mainBox = new VBox();

            controller.sign();

            verify(autogram).sign(job, key);
            verify(gui, never()).pickSignatureAndThen(any(), any(), any());
        });
    }

    @Test
    void testBothApprovedLayoutsStayCompactWithTheRealStylesheet() throws Exception {
        onFxThread(() -> {
            for (var result : List.of(QUALIFIED, RECOGNIZED)) {
                var dialog = open(List.of(peter), settings(false, "SK"),
                        new PickKeyDialogController.TimestampOptions(job(true, true), mock(TSPSource.class)));
                dialog.answer(peter, result);
                var stage = new Stage();
                var scene = new Scene(dialog.root());
                scene.getStylesheets().add(GUI.class.getResource("idsk.css").toExternalForm());
                stage.setScene(scene);
                try {
                    stage.show();
                    dialog.root().applyCss();
                    dialog.root().layout();
                    stage.sizeToScene();
                    assertTrue(dialog.root().getLayoutBounds().getHeight() < 500, "the two choices must not expand vertically");
                    assertTrue(dialog.radio(peter, true).getHeight() < 90);
                    assertTrue(dialog.radio(peter, false).getHeight() < 90);
                } finally {
                    stage.close();
                }
            }
        });
    }

    @Test
    void testPickerAssertionsUseSlovakEvenWhenTheUserSelectedEnglish() throws Exception {
        onFxThread(() -> {
            var settings = settings(false, "SK");
            settings.setLanguage(SupportedLanguage.ENGLISH);
            var dialog = open(List.of(jana), settings,
                    new PickKeyDialogController.TimestampOptions(job(false, true), mock(TSPSource.class)));
            dialog.answer(jana, QUALIFIED);

            assertEquals("sk", dialog.controller().getResources().getLocale().getLanguage());
            assertEquals(List.of("Osvedčený podpis"), texts(dialog.badges(jana, true)));
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

    private static Dialog open(List<DSSPrivateKeyEntry> keys, UserSettings settings) throws Exception {
        return open(keys, settings, null);
    }

    private static Dialog open(List<DSSPrivateKeyEntry> keys, UserSettings settings, PickKeyDialogController.TimestampOptions timestampOptions) throws Exception {
        var answers = new HashMap<DSSPrivateKeyEntry, Consumer<Result>>();
        var controller = new PickKeyDialogController(keys, key -> { }, settings, answers::put, () -> { }, timestampOptions);
        var loader = new FXMLLoader(PickKeyDialogController.class.getResource("pick-key-dialog.fxml"),
                SupportedLanguage.SLOVAK.loadResources());
        loader.setController(controller);
        Parent root = loader.load();
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
            return radios(key).getFirst();
        }

        RadioButton radio(DSSPrivateKeyEntry key, boolean timestamp) {
            return radios(key).get(timestamp ? 0 : 1);
        }

        HBox badges(DSSPrivateKeyEntry key) {
            return badges(radio(key));
        }

        HBox badges(DSSPrivateKeyEntry key, boolean timestamp) {
            return badges(radio(key, timestamp));
        }

        private HBox badges(RadioButton radio) {
            var content = (javafx.scene.layout.VBox) radio.getGraphic();
            return (HBox) ((HBox) content.getChildren().getFirst()).getChildren().get(1);
        }

        String description(DSSPrivateKeyEntry key, boolean timestamp) {
            var content = (javafx.scene.layout.VBox) radio(key, timestamp).getGraphic();
            var description = content.getChildren().get(1);
            return description.isVisible() ? String.join("", texts(description)) : "";
        }

        private List<RadioButton> radios(DSSPrivateKeyEntry key) {
            return controller.radios.getChildren().stream()
                    .map(RadioButton.class::cast)
                    .filter(radio -> texts(radio.getGraphic()).contains(digital.slovensko.autogram.util.DSSUtils
                            .parseCN(key.getCertificate().getSubject().getRFC2253())))
                    .toList();
        }
    }
}
