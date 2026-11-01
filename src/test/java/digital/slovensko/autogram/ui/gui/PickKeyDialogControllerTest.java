package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.TestPki;
import digital.slovensko.autogram.core.CertificateStatusChecker.Qualification;
import digital.slovensko.autogram.core.CertificateStatusChecker.Result;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Certificate selection with the results of the status and qualification checks, given by the test instead of OCSP,
 * CRL and the trusted lists. Desktop-only like {@link SettingsDialogAccessibilityTest}, run with
 * -Dautogram.guiTests=true.
 */
@EnabledIfSystemProperty(named = "autogram.guiTests", matches = "true")
class PickKeyDialogControllerTest {
    private static final Result QUALIFIED = new Result(CertificateStatus.GOOD, Qualification.QUALIFIED);
    private static final Result NOT_QUALIFIED = new Result(CertificateStatus.GOOD, Qualification.NOT_QUALIFIED);

    private static DSSPrivateKeyEntry jana;
    private static DSSPrivateKeyEntry peter;
    private static DSSPrivateKeyEntry seal;

    @BeforeAll
    static void startJavaFx() throws Exception {
        var pki = TestPki.create();
        jana = pki.signer(TestPki.JANA).token().getKeys().getFirst();
        peter = pki.signer(TestPki.PETER).token().getKeys().getFirst();
        seal = pki.signer(TestPki.SEAL).token().getKeys().getFirst();

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
            assertTrue(dialog.badges(jana).getChildren().isEmpty());
            assertFalse(dialog.controller().hint.isVisible());
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
            dialog.answer(peter, new Result(CertificateStatus.REVOKED, Qualification.QUALIFIED));
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
            dialog.answer(jana, new Result(CertificateStatus.UNKNOWN, Qualification.UNKNOWN));

            assertFalse(dialog.radio(jana).isDisabled());
            assertEquals(2, dialog.badges(jana).getChildren().size());
            assertFalse(dialog.controller().hint.isVisible());
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

    private static UserSettings settings(boolean nonQualifiedCertsEnabled, String trustedCountry) {
        var settings = UserSettings.load();
        settings.setNonQualifiedCertsEnabled(nonQualifiedCertsEnabled);
        settings.getTrustedList().clear();
        settings.addToTrustedList(trustedCountry);
        return settings;
    }

    private static Dialog open(List<DSSPrivateKeyEntry> keys, UserSettings settings) {
        var answers = new HashMap<DSSPrivateKeyEntry, Consumer<Result>>();
        var controller = new PickKeyDialogController(keys, key -> { }, settings, answers::put, () -> { });
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
