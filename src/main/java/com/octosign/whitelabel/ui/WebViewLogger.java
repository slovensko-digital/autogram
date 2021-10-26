package com.octosign.whitelabel.ui;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 * JavaFX WebView console monkey-patch
 */
public class WebViewLogger {
    public void log(String message)
    {
        System.out.println("WebView INFO: " + message);
    }

    public void warn(String message)
    {
        System.out.println("WebView WARNING: " + message);
    }

    public void error(String message)
    {
        System.out.println("WebView ERROR: " + message);
    }

    public static void register(WebEngine engine) {
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("logger", new WebViewLogger());
        engine.executeScript("console.log = (...message) => logger.log(message.join(' '));");
        engine.executeScript("console.warn = (...message) => logger.warn(message.join(' '));");
        engine.executeScript("console.error = (...message) => logger.error(message.join(' '));");
    }
}
