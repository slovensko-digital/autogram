package com.octosign.whitelabel.ui.status;

import java.net.URL;

import com.octosign.whitelabel.ui.Main;
import dorkbox.os.OSUtil;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.octosign.whitelabel.ui.utils.I18n.translate;

/**
 * Status indication if the app is running in the background
 *
 * This is:
 * - Preferably a system tray icon with information about the running app for supported OS
 * - Minimized window everywhere else
 */
public class StatusIndication {
    private static final URL iconUrl = Main.class.getResource("icon.png");

    private SystemTray systemTray;

    private Runnable onExit;

    public StatusIndication(Runnable onExit) {
        this.onExit = onExit;

        if (isTraySupported()) {
            addAppToTray();
        } else {
            addMinimizedWindow();
        }
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

    /**
     * Add a tray icon for this application
     */
    private void addAppToTray() {
        systemTray = SystemTray.get();
        if (systemTray != null) {
            systemTray.setImage(iconUrl);
            systemTray.setStatus(translate("app.name"));
            systemTray.getMenu().add(
                new MenuItem(translate("text.quit"),
                e-> Platform.runLater(onExit))
            );
        }
    }

    /**
     * Is the system tray supported in this environment
     *
     * Only tested OS should be allowed to be enabled.
     * Because, for example, the native code crashes JVM on Fedora.
     */
    private boolean isTraySupported() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) return false;
        if (os.startsWith("Linux") && OSUtil.Linux.isUbuntu()) return true;
        if (os.startsWith("Windows")) return true;

        return false;
    }

    /**
     * Free all native handles
     *
     * TODO: Can't we use closeable here?
     */
    public void dispose() {
        if (systemTray != null) {
            systemTray.shutdown();
        }
    }
}
