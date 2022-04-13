package com.octosign.whitelabel.ui.utils;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.ui.Main;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.utils.Utils.isNullOrBlank;
import static java.util.Objects.requireNonNull;

public class FXUtils {

    /**
     * Displays simple generic error alert (without any error details)
     * Useful to notify user that an error occured and application will be closed
     * @param inputs
     */
    public static void displaySimpleError(String... inputs) {
        switch (inputs.length) {
            case 0 -> displayError("text.error", "error.general.description");
            case 1 -> displayError("text.error", inputs[0]);
            case 2 -> displayError(inputs[0], inputs[1]);
            default -> displaySimpleError();
        }
    }

    /**
     * Display error alert with details (inferred from UserException)
     * @param e Cause of the error
     */
    public static void displayError(UserException e) {
        requireNonNull(e);
        if (isNullOrBlank(e.getHeader()) && isNullOrBlank(e.getDescription()))
            throw new IntegrationException(Code.UNEXPECTED_ERROR, e);

        displayError(e.getHeader(), e.getDescription(), e);
    }

    /**
     * Display error alert with details (inferred from IntegrationException)
     * @param e Cause of the error
     */
    public static void displayError(IntegrationException e) {
        requireNonNull(e);
        displayError("error.integration.header", e.getMessage(), e);
    }

    /**
     * Displays error alert with header and description (without any additional error details)
     *
     */
    public static void displayError(String header, String description) {
        displayError(header, description, null);
    }

    /**
     * Displays error alert (with optional error description included)
     *
     * @param header
     * @param description
     * @param cause         Optional cause of the error
     */
    public static void displayError(String header, String description, Throwable cause) {
        var alert = buildAlert(
                Alert.AlertType.ERROR,
                translate("text.error"),
                translate(header),
                translate(description)
        );

        if (cause != null)
            alert.getDialogPane().setExpandableContent(buildErrorDetails(cause));

        alert.showAndWait();
    }

    /**
     * Displays info alert (without error description included)
     *
     * @param header
     * @param description
     */
    public static void displayInfo(String header, String description) {
        var alert = buildAlert(
                Alert.AlertType.INFORMATION,
                translate("text.info"),
                translate(header),
                translate(description)
        );

        alert.showAndWait();
    }

    /**
     * Displays warning alert
     *
     * @param header
     * @param description
     */
    public static void displayWarning(String header, String description) {
        var alert= buildWarning(header, description);

        alert.showAndWait();
    }

    public static Alert buildWarning(String header, String description) {
        return buildAlert(
                Alert.AlertType.NONE,
                translate("text.warn"),
                translate(header),
                translate(description)
        );
    }

    /**
     * Displays alert of a specified type (without error description included)
     *
     * @param type          Determines overall tone of the dialog (e.g. Alert.CONFIRMATION)
     * @param title         The upmost title of the displayed alert window
     * @param header        Alert header
     * @param description   Alert detailed description
     */
    public static void displayAlert(Alert.AlertType type, String title, String header, String description) {
        var alert = buildAlert(type, title, header, description);

        alert.showAndWait();
    }

    public static Alert buildAlert(Alert.AlertType type, String title, String header, String description) {
        var alert = new Alert(type);
        Optional.ofNullable(title).ifPresent(alert::setTitle);
        Optional.ofNullable(header).ifPresent(alert::setHeaderText);
        Optional.ofNullable(description).ifPresent(alert::setContentText);

        addCustomStyles(alert);
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

    /**
     * Style given dialog with customized stylesheets.
     *
     * @param dialog    Target dialog which style is affected
     * @param <U>
     * @param <T>
     */
    public static <U, T extends Dialog<U>> void addCustomStyles(T dialog) {
        dialog.getDialogPane().getStylesheets().addAll(getStylesheets());
    }

    public static List<String> getStylesheets() {
        return Stream.of("shared.css" , "dialog.css", "overrides.css")
            .map(filename -> requireNonNull(Main.class.getResource(filename)).toExternalForm())
            .toList();
    }

    public static Alert transformDialogTo(Alert.AlertType type, String i18nKey, Alert alert) {
        alert.setAlertType(type);
        alert.setTitle(translate(String.format("text.%s", toPrefix(type))));
        alert.setHeaderText(translate(String.format("%s.%s.header", toPrefix(type), i18nKey)));
        alert.setContentText(translate(String.format("%s.%s.description", toPrefix(type), i18nKey)));

        ButtonType continueType = new ButtonType(translate("btn.continue"), ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(continueType);
        return alert;
    }

    public static String toPrefix(Alert.AlertType dialogType) {
        return switch (dialogType) {
            case INFORMATION -> "info";
            case WARNING -> "warn";
            case ERROR -> "error";
            case CONFIRMATION -> throw new RuntimeException("Not used yet, first create dictionary entries!");
            case NONE -> throw new RuntimeException("Not used yet, first create dictionary entries!");
        };
    }

    public static Stage getCurrentStage(Node source) {
        return (Stage) source.getScene().getWindow();
    }

    public static void bringToForeground(Stage stage) {
        stage.requestFocus();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Platform.runLater(() -> {
            try {
                Thread.sleep(42);
                stage.setAlwaysOnTop(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
