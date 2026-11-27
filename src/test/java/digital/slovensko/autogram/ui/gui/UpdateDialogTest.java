package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.ui.SupportedLanguage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        onFxThread(() -> {
            var dialog = openDialog(language);
            var controller = dialog.controller();

            assertEquals(dialog.resources().getString("update.newVersion.aur.text"), controller.text.getText());
            assertTrue(controller.mainButton.isVisible());
            assertEquals(dialog.resources().getString("general.close.btn"), controller.mainButton.getText());
            assertFalse(controller.cancelButton.isVisible());
            assertFalse(controller.cancelButton.isManaged());
            assertFalse(controller.storeButton.isVisible());
            assertFalse(controller.moreInfo.isVisible());

            controller.mainButton.fire();
            assertFalse(dialog.stage().isShowing(), "the only button closes the window");
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguage.class)
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void otherInstallationsAreOfferedTheDownload(SupportedLanguage language) throws Exception {
        onFxThread(() -> {
            var dialog = openDialog(language);
            var controller = dialog.controller();

            assertEquals(dialog.resources().getString("update.newVersion.text"), controller.text.getText());
            assertEquals(dialog.resources().getString("update.download.btn"), controller.mainButton.getText());
            assertTrue(controller.cancelButton.isVisible());
            dialog.stage().close();
        });
    }

    private static Dialog openDialog(SupportedLanguage language) throws Exception {
        var resources = language.loadResources();
        var controller = new UpdateController(null);
        var loader = new FXMLLoader(UpdateController.class.getResource("update-dialog.fxml"), resources);
        loader.setController(controller);
        Parent root = loader.load();

        var stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
        return new Dialog(stage, controller, resources);
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
