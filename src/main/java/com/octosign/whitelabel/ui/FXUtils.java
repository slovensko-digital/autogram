package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;
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

import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;
import static java.util.Objects.requireNonNull;

public class FXUtils {

    public static void displayIntegrationError(String description, Throwable cause) {
        displayError("error.integration.header", description, cause);
    }

    public static void displaySimpleError(String... inputs) {
        switch (inputs.length) {
            case 0 -> displayError("error.general.header", "error.general.description");
            case 1 -> displayError("error.general.header", inputs[0]);
            case 2 -> displayError(inputs[0], inputs[1]);
            default -> displaySimpleError();
        }
    }

    public static void displayError(UserException e) {
        if (isNullOrBlank(e.getHeader()) && isNullOrBlank(e.getDescription()))
            throw new IntegrationException(Code.MISSING_ERROR_DETAILS, e);

        displayError(e.getHeader(), e.getDescription(), e);
    }

    public static void displayError(IntegrationException e) {
        if (e.getCode() == null)
            throw new IntegrationException(Code.MISSING_ERROR_DETAILS, e);

        displayIntegrationError(e.getMessage(), e);
    }

    public static void displayError(String header, String description) {
        displayError(header, description, null);
    }

    public static void displayError(String header, String description, Throwable cause) {
        var alert = buildAlert(
                Alert.AlertType.ERROR,
                translate("error.title"),
                translateIfNeeded(header),
                translateIfNeeded(description)
        );

        if (cause != null)
            alert.getDialogPane().setExpandableContent(buildErrorDetails(cause));

        alert.showAndWait();
    }

    public static void displayInfo(String header, String description) {
        var alert = buildAlert(
                Alert.AlertType.INFORMATION,
                translate("info.title"),
                translateIfNeeded(header),
                translateIfNeeded(description)
        );

        alert.showAndWait();
    }

    private static String translateIfNeeded(String input) {
        if (input.startsWith("error."))
            return translate(input);
        else
            return input;
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

    private static <T extends Throwable> Pane buildErrorDetails(T e) {
        e.printStackTrace();
        var stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        var stackTrace = stringWriter.toString();

        var details = new GridPane();
        details.setMaxWidth(Double.MAX_VALUE);

        var label = new Label(translate("error.details"));
        details.add(label, 0, 0);

        var textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        details.add(textArea, 0, 1);

        return details;
    }

    public static <U, T extends Dialog<U>> void addStylesheets(T dialog) {
        var stylesheets = dialog.getDialogPane().getStylesheets();

        stylesheets.add(requireNonNull(Main.class.getResource("shared.css")).toExternalForm());
        stylesheets.add(requireNonNull(Main.class.getResource("dialog.css")).toExternalForm());
        stylesheets.add(requireNonNull(Main.class.getResource("overrides.css")).toExternalForm());
    }
}
