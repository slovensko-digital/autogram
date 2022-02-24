package com.octosign.whitelabel.ui.status;

import com.octosign.whitelabel.ui.Main;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.octosign.whitelabel.ui.I18n.translate;

/**
 * Status indication if the app is running in the background
 *
 * This is:
 * - Preferably a system tray icon with information about the running app for supported OS
 * - Minimized window everywhere else
 */
public class StatusIndication {
    private Runnable onExit;

    public StatusIndication(Runnable onExit) {
        this.onExit = onExit;

        addMinimizedWindow();
    }

    /**
     * Add Window with info about the running app
     */
    private void addMinimizedWindow() {
        var windowStage = new Stage();

        var fxmlLoader = Main.loadWindow("status");
        VBox root = fxmlLoader.getRoot();

        var scene = new Scene(root, 320, 160);
        windowStage.setTitle(translate("app.name"));
        windowStage.setScene(scene);
        windowStage.setIconified(true);
        windowStage.setOnHidden((event) -> this.onExit.run());
        windowStage.show();
    }
}
