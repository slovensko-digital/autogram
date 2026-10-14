package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.ui.SupportedLanguage;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
                    assertFalse(box.getAccessibleHelp().isBlank());
                }

                var first = dialog.boxes().getFirst();
                assertEquals(language == SupportedLanguage.SLOVAK ? "Belgicko" : "Belgium", first.getAccessibleText());
                first.requestFocus();
                press(dialog.scene(), KeyCode.SPACE, false);
                assertFalse(first.isSelected());
                assertFalse(dialog.settings().getTrustedList().contains("BE"));
                assertEquals(first.getAccessibleText(), first.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                assertEquals(false, first.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
                press(dialog.scene(), KeyCode.SPACE, false);
                assertTrue(first.isSelected());
                assertEquals(1, dialog.settings().getTrustedList().stream().filter("BE"::equals).count());

                // Greece is displayed using ISO GR, but stored using trusted-list code EL.
                var greece = dialog.boxes().get(9);
                assertEquals(language == SupportedLanguage.SLOVAK ? "Grécko" : "Greece", greece.getAccessibleText());
                greece.requestFocus();
                press(dialog.scene(), KeyCode.SPACE, false);
                assertTrue(dialog.settings().getTrustedList().contains("EL"));
                assertFalse(dialog.settings().getTrustedList().contains("GR"));
            }
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    void arrowsScrollWithoutTogglingAndTabLeavesAndReentersGroup(SupportedLanguage language) throws Exception {
        onFxThread(() -> {
            try (var dialog = openDialog(language)) {
                assertEquals(27, dialog.boxes().size());
                assertTrue(dialog.boxes().stream().allMatch(CheckBox::isFocusTraversable));
                assertFalse(dialog.scrollPane().isFocusTraversable());
                var original = List.copyOf(dialog.settings().getTrustedList());
                var first = dialog.boxes().getFirst();
                first.requestFocus();
                press(dialog.scene(), KeyCode.UP, false);
                assertSame(first, dialog.scene().getFocusOwner());
                press(dialog.scene(), KeyCode.DOWN, false);
                assertSame(dialog.boxes().get(1), dialog.scene().getFocusOwner());
                assertTrue(dialog.boxes().stream().allMatch(CheckBox::isFocusTraversable));
                press(dialog.scene(), KeyCode.END, false);
                assertSame(dialog.boxes().getLast(), dialog.scene().getFocusOwner());
                dialog.scene().getRoot().layout();
                assertTrue(dialog.scrollPane().getVvalue() > 0);
                assertRowVisible(dialog, dialog.boxes().getLast());
                press(dialog.scene(), KeyCode.DOWN, false);
                assertSame(dialog.boxes().getLast(), dialog.scene().getFocusOwner());
                press(dialog.scene(), KeyCode.HOME, false);
                dialog.scene().getRoot().layout();
                assertSame(first, dialog.scene().getFocusOwner());
                assertRowVisible(dialog, first);
                press(dialog.scene(), KeyCode.DOWN, false);
                var active = dialog.boxes().get(1);
                press(dialog.scene(), KeyCode.TAB, false);
                assertSame(dialog.scene().lookup("#saveButton"), dialog.scene().getFocusOwner());
                press(dialog.scene(), KeyCode.TAB, true);
                assertSame(active, dialog.scene().getFocusOwner());
                press(dialog.scene(), KeyCode.TAB, true);
                assertSame(dialog.scene().lookup(".tab-pane"), dialog.scene().getFocusOwner());
                press(dialog.scene(), KeyCode.TAB, false);
                assertSame(active, dialog.scene().getFocusOwner());
                assertEquals(original, dialog.settings().getTrustedList());
            }
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    void accessibilityFocusCanReachEveryCountry(SupportedLanguage language) throws Exception {
        onFxThread(() -> {
            try (var dialog = openDialog(language)) {
                for (var box : dialog.boxes()) {
                    box.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                    assertSame(box, dialog.scene().getFocusOwner());
                    press(dialog.scene(), KeyCode.TAB, false);
                    assertSame(dialog.scene().lookup("#saveButton"), dialog.scene().getFocusOwner());
                    press(dialog.scene(), KeyCode.TAB, true);
                    assertSame(box, dialog.scene().getFocusOwner());
                }
            }
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    void mouseClickMovesFocusSoSpaceTogglesTheClickedCountry(SupportedLanguage language) throws Exception {
        var setup = new FutureTask<Dialog>(() -> openDialog(language));
        Platform.runLater(setup);
        var dialog = setup.get(10, TimeUnit.SECONDS);
        try {
            onFxThread(() -> {
                dialog.boxes().getFirst().requestFocus();
                var bounds = dialog.boxes().get(1).localToScreen(dialog.boxes().get(1).getBoundsInLocal());
                var robot = new Robot();
                robot.mouseMove(bounds.getCenterX(), bounds.getCenterY());
                robot.mouseClick(MouseButton.PRIMARY);
            });
            var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (true) {
                var focused = new FutureTask<Boolean>(() -> dialog.scene().getFocusOwner() == dialog.boxes().get(1));
                Platform.runLater(focused);
                if (focused.get(10, TimeUnit.SECONDS))
                    break;
                if (System.nanoTime() >= deadline)
                    fail("Mouse click did not focus Bulgaria");
                Thread.sleep(20);
            }
            onFxThread(() -> {
                var belgium = dialog.boxes().getFirst();
                var bulgaria = dialog.boxes().get(1);
                assertTrue(belgium.isSelected());
                assertTrue(bulgaria.isSelected());
                press(dialog.scene(), KeyCode.SPACE, false);
                assertFalse(bulgaria.isSelected());
                assertTrue(belgium.isSelected());
                assertEquals(List.of("BE"), dialog.settings().getTrustedList());
                press(dialog.scene(), KeyCode.TAB, false);
                assertSame(dialog.scene().lookup("#saveButton"), dialog.scene().getFocusOwner());
                press(dialog.scene(), KeyCode.TAB, true);
                assertSame(bulgaria, dialog.scene().getFocusOwner());
            });
        } finally {
            onFxThread(dialog::close);
        }
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
        var scrollPane = (ScrollPane) root.lookup("#trustedCountriesScrollPane");
        return new Dialog(stage, scene, settings, boxes, scrollPane);
    }

    private static void assertRowVisible(Dialog dialog, CheckBox box) {
        var viewport = dialog.scrollPane().lookup(".viewport");
        var viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        var row = box.getParent().getParent();
        var rowBounds = row.localToScene(row.getBoundsInLocal());
        assertTrue(rowBounds.getMinY() >= viewportBounds.getMinY() - 1);
        assertTrue(rowBounds.getMaxY() <= viewportBounds.getMaxY() + 1);
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

    private record Dialog(Stage stage, Scene scene, UserSettings settings, List<CheckBox> boxes,
                          ScrollPane scrollPane) implements AutoCloseable {
        @Override
        public void close() {
            stage.close();
        }
    }
}
