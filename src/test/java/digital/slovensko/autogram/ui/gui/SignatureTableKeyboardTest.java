package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.SupportedLanguage;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Arrays;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/** Run with -Dautogram.guiTests=true on a desktop or under xvfb-run. */
@EnabledIfSystemProperty(named = "autogram.guiTests", matches = "true")
class SignatureTableKeyboardTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
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
    void signatureDetailsAndShowMoreRespondToEnterAndSpace() throws Exception {
        onFxThread(() -> {
            var count = new AtomicInteger();
            var resources = SupportedLanguage.SLOVAK.loadResources();
            var details = GUIValidationUtils.createSignatureTableLink(ignored -> count.incrementAndGet(), resources);
            // maxRows=1 shows the "show more" button before rendering any signature rows.
            var table = GUIValidationUtils.createSignatureTableRows(resources, Arrays.asList(null, null),
                    false, false, ignored -> count.incrementAndGet(), 1);
            var stage = new Stage();
            stage.setScene(new Scene(new VBox(details, table)));
            stage.show();
            try {
                var more = table.lookupAll(".button").stream().map(Button.class::cast)
                        .filter(button -> button.getText().equals(resources.getString("signature.table.more.btn")))
                        .findFirst().orElseThrow();
                for (var button : new Button[]{details, more}) {
                    button.requestFocus();
                    assertSame(button, stage.getScene().getFocusOwner());
                    press(button, KeyCode.ENTER);
                    assertEquals(1, count.getAndSet(0), button.getText() + " with Enter");
                    press(button, KeyCode.SPACE);
                    assertEquals(1, count.getAndSet(0), button.getText() + " with Space");
                }
            } finally {
                stage.close();
            }
            return null;
        });
    }

    @Test
    void clickingSignatureDetailsRunsTheSameAction() throws Exception {
        var count = new AtomicInteger();
        var stage = onFxThread(() -> {
            var button = GUIValidationUtils.createSignatureTableLink(ignored -> count.incrementAndGet(),
                    SupportedLanguage.SLOVAK.loadResources());
            var window = new Stage();
            window.setScene(new Scene(new VBox(button), 320, 100));
            window.show();
            return window;
        });
        try {
            onFxThread(() -> {
                var button = (Button) stage.getScene().getRoot().getChildrenUnmodifiable().getFirst();
                var bounds = button.localToScreen(button.getBoundsInLocal());
                var robot = new Robot();
                robot.mouseMove(bounds.getCenterX(), bounds.getCenterY());
                robot.mouseClick(MouseButton.PRIMARY);
                return null;
            });
            var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (count.get() == 0 && System.nanoTime() < deadline)
                Thread.sleep(20);
            if (count.get() == 0)
                fail("Mouse click did not activate signature details");
            assertEquals(1, count.get());
        } finally {
            onFxThread(() -> { stage.close(); return null; });
        }
    }

    private static void press(Button button, KeyCode code) {
        Event.fireEvent(button, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
        Event.fireEvent(button, new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, false, false, false, false));
    }

    private static <T> T onFxThread(java.util.concurrent.Callable<T> action) throws Exception {
        var task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
