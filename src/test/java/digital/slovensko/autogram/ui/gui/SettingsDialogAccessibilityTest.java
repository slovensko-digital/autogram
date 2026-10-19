package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.ui.SupportedLanguage;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.text.Collator;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Desktop-only tests, intentionally opt-in for builds without a display.
 * Run with -Dautogram.guiTests=true; see DEVELOPER.md for the Xvfb command.
 */
@EnabledIfSystemProperty(named = "autogram.guiTests", matches = "true")
class SettingsDialogAccessibilityTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        var ready = new FutureTask<Void>(() -> {
            Platform.setImplicitExit(false);
            javafx.application.Application.setUserAgentStylesheet(SettingsDialogController.class.getResource("idsk.css").toExternalForm());
            return null;
        });
        try {
            Platform.startup(ready);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    void announcesCountryAndCheckedStateAndSpaceUpdatesSettings(SupportedLanguage language) throws Exception {
        onFxThread(() -> {
            try (var dialog = openDialog(language)) {
                for (var box : dialog.boxes()) {
                    var row = box.getParent().getParent();
                    var textFlow = (TextFlow) ((VBox) ((Parent) row).getChildrenUnmodifiable().getFirst()).getChildren().getFirst();
                    var countryName = ((Text) textFlow.getChildren().getFirst()).getText();
                    assertEquals(countryName, box.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                    assertEquals(AccessibleRole.CHECK_BOX, box.getAccessibleRole());
                    assertEquals(box.isSelected(), box.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
                }

                var names = dialog.boxes().stream().map(CheckBox::getAccessibleText).toList();
                assertEquals(names.stream().sorted(Collator.getInstance(language.getLocale())).toList(), names);

                // The keyboard help is announced only once, on the country where Tab enters the list.
                assertFalse(dialog.boxes().getFirst().getAccessibleHelp().isBlank());
                assertTrue(dialog.boxes().stream().skip(1).allMatch(box -> box.getAccessibleHelp() == null));

                var belgium = dialog.box(language == SupportedLanguage.SLOVAK ? "Belgicko" : "Belgium");
                belgium.requestFocus();
                press(dialog.scene(), KeyCode.SPACE, false);
                assertFalse(belgium.isSelected());
                assertFalse(dialog.settings().getTrustedList().contains("BE"));
                assertEquals(belgium.getAccessibleText(), belgium.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                assertEquals(false, belgium.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
                press(dialog.scene(), KeyCode.SPACE, false);
                assertTrue(belgium.isSelected());
                assertEquals(1, dialog.settings().getTrustedList().stream().filter("BE"::equals).count());

                // Greece is displayed using ISO GR, but stored using trusted-list code EL.
                var greece = dialog.box(language == SupportedLanguage.SLOVAK ? "Grécko" : "Greece");
                greece.requestFocus();
                press(dialog.scene(), KeyCode.SPACE, false);
                assertTrue(dialog.settings().getTrustedList().contains("EL"));
                assertFalse(dialog.settings().getTrustedList().contains("GR"));
            }
        });
    }

    private static Dialog openDialog(SupportedLanguage language) throws Exception {
        var settings = UserSettings.load();
        settings.setLanguage(language);
        settings.getTrustedList().clear();
        settings.addToTrustedList("BE");
        var loader = new FXMLLoader(SettingsDialogController.class.getResource("settings-dialog.fxml"), language.loadResources());
        loader.setController(new SettingsDialogController(null, settings));
        Parent root = loader.load();
        var scene = new Scene(root);
        var stage = new Stage();
        stage.setScene(scene);
        ((TabPane) root.lookup(".tab-pane")).getSelectionModel().select(1);
        stage.show();
        root.applyCss();
        root.layout();
        var group = (VBox) root.lookup("#trustedCountriesList");
        var boxes = group.getChildren().stream().map(row -> (CheckBox) row.lookup(".check-box")).toList();
        return new Dialog(stage, scene, settings, boxes);
    }

    private static void press(Scene scene, KeyCode code, boolean shift) {
        var target = scene.getFocusOwner();
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, false, false, false));
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, shift, false, false, false));
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

    private record Dialog(Stage stage, Scene scene, UserSettings settings, List<CheckBox> boxes) implements AutoCloseable {
        CheckBox box(String countryName) {
            return boxes.stream().filter(box -> countryName.equals(box.getAccessibleText())).findFirst().orElseThrow();
        }

        @Override
        public void close() {
            stage.close();
        }
    }
}
