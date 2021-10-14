package com.octosign.whitelabel.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Consumer;

import static com.octosign.whitelabel.ui.I18n.getProperty;

public class FX {

    public static void displayError(String base) { displayError(base, null); }

    public static void displayError(String base, Throwable e) {
        var alert = buildAlert(
                Alert.AlertType.ERROR,
                getProperty("error.title"),
                getProperty("error." + base + ".header"),
                getProperty("error." + base + ".description")
        );

        if(e != null)
            alert.getDialogPane().setExpandableContent(getErrorDetails(e));

        alert.showAndWait();
    }

    public static void displayInfo(String base) {
        var alert = buildAlert(
                Alert.AlertType.INFORMATION,
                getProperty("info.title"),
                getProperty("info." + base + ".header"),
                getProperty("info." + base + ".description")
        );

        if(e != null)
            alert.getDialogPane().setExpandableContent(getErrorDetails(e));

        alert.showAndWait();
    }

    /**
     * Display alert
     */
    protected static void displayAlert(Alert.AlertType type, String title, String header, String description) {
        var alert = buildAlert(type, title, header, description);

        alert.showAndWait();
    }

    /**
     * Create unified Alert
     */
    public static Alert buildAlert(Alert.AlertType type, String title, String header, String description) {
        var alert = new Alert(type);
        Optional.ofNullable(title).ifPresent(alert::setTitle);
        Optional.ofNullable(header).ifPresent(alert::setHeaderText);
        Optional.ofNullable(description).ifPresent(alert::setContentText);

        addStylesheets(alert);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        return alert;
    }

    private static <T extends Throwable> Pane getErrorDetails(T e) {
        e.printStackTrace();
        var stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        var stackTrace = stringWriter.toString();

        var details = new GridPane();
        details.setMaxWidth(Double.MAX_VALUE);

        var label = new Label(getProperty("error.details"));
        details.add(label, 0, 0);

        var textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        details.add(textArea, 0, 1);

        return details;
    }

    public static <U, T extends Dialog<U>> void addStylesheets(T dialog) {
        var stylesheets = dialog.getDialogPane().getStylesheets();

        stylesheets.add(Main.class.getResource("shared.css").toExternalForm());
        stylesheets.add(Main.class.getResource("dialog.css").toExternalForm());
        stylesheets.add(Main.class.getResource("overrides.css").toExternalForm());
    }


    /**
     * Display error alert with exception details
     */
    public static void displayError(String title, String header, String description, Throwable e) {
        var alert = buildAlert(Alert.AlertType.ERROR, title, header, description);
        if (e != null)
            alert.getDialogPane().setExpandableContent(getErrorDetails(e));

        alert.showAndWait();
    }

//    /**
//     * Display error alert with exception details
//     */
//    public static void displayError(String header, String description, Throwable e) {
//        var alert = buildAlert(Alert.AlertType.ERROR, getProperty(DEFAULT_ERROR_TITLE_KEY), header, description);
//        if (e != null) alert.getDialogPane().setExpandableContent(getErrorDetails(e));
//
//        alert.showAndWait();
//    }
//
}
