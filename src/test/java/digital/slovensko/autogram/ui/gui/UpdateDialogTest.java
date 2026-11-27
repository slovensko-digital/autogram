package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.SupportedLanguage;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Desktop-only tests, intentionally opt-in for builds without a display.
 * Run with -Dautogram.guiTests=true; see DEVELOPER.md for the Xvfb command.
 */
@EnabledIfSystemProperty(named = "autogram.guiTests", matches = "true")
class UpdateDialogTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        var ready = new FutureTask<Void>(() -> {
            Platform.setImplicitExit(false);
            javafx.application.Application.setUserAgentStylesheet(UpdateController.class.getResource("idsk.css").toExternalForm());
            return null;
        });
        try {
            Platform.startup(ready);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @AfterEach
    void clearUpdateChannel() {
        System.clearProperty("autogram.updateChannel");
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    void aurInstallationIsToldToUpdateThroughThePackageManager(SupportedLanguage language) throws Exception {
        System.setProperty("autogram.updateChannel", "aur");

        var hostServices = mock(HostServices.class);

        onFxThread(() -> {
            var dialog = openDialog(language, hostServices);
            var controller = dialog.controller();

            assertEquals(HasI18n.translate(dialog.resources(), "update.newVersion.aur.text", "yay -Syu", "paru -Syu"),
                    textOf(controller.textFlow));
            // shown as code, the same as in the guides
            var commands = controller.textFlow.getChildren().stream()
                    .filter(node -> node.getStyleClass().contains("autogram-code"))
                    .map(node -> ((Labeled) node).getText())
                    .toList();
            assertEquals(List.of("yay -Syu", "paru -Syu"), commands);

            // the update happens in the terminal, so there's no primary button, only a grey Close and the guide next to it
            for (var hidden : List.of(controller.mainButton, controller.storeButton, controller.cancelButton)) {
                assertFalse(hidden.isVisible(), hidden.getId());
                assertFalse(hidden.isManaged(), hidden.getId());
            }
            assertFalse(controller.moreInfo.isVisible());
            assertTrue(controller.closeButton.isVisible());
            assertTrue(controller.closeButton.getStyleClass().contains("autogram-button--secondary"));
            assertEquals(dialog.resources().getString("general.close.btn"), controller.closeButton.getText());
            assertTrue(controller.guideButton.isVisible());
            assertEquals(dialog.resources().getString("update.moreInfo.link"), controller.guideButton.getText());
            var actions = controller.closeButton.getParent().getChildrenUnmodifiable();
            assertEquals(actions.indexOf(controller.closeButton) + 1, actions.indexOf(controller.guideButton));

            controller.guideButton.fire();
            verify(hostServices).showDocument(dialog.resources().getString("docs.url") + "installation/#arch-linux");

            controller.closeButton.fire();
            assertFalse(dialog.stage().isShowing());
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void otherInstallationsAreOfferedTheDownload(SupportedLanguage language) throws Exception {
        onFxThread(() -> {
            var dialog = openDialog(language, null);
            var controller = dialog.controller();

            assertEquals(dialog.resources().getString("update.newVersion.text"), controller.text.getText());
            assertEquals(dialog.resources().getString("update.download.btn"), controller.mainButton.getText());
            assertTrue(controller.cancelButton.isVisible());
            dialog.stage().close();
        });
    }

    private static Dialog openDialog(SupportedLanguage language, HostServices hostServices) throws Exception {
        var resources = language.loadResources();
        var controller = new UpdateController(hostServices);
        var loader = new FXMLLoader(UpdateController.class.getResource("update-dialog.fxml"), resources);
        loader.setController(controller);
        Parent root = loader.load();

        var stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
        return new Dialog(stage, controller, resources);
    }

    private static String textOf(TextFlow flow) {
        return flow.getChildren().stream()
                .map(node -> node instanceof Text text ? text.getText() : ((Labeled) node).getText())
                .collect(Collectors.joining());
    }

    private static void onFxThread(FxAction action) throws Exception {
        var task = new FutureTask<Void>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private record Dialog(Stage stage, UpdateController controller, ResourceBundle resources) {
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }
}
