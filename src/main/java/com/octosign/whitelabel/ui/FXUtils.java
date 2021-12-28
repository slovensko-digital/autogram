package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;
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
            case 1 -> displayError("textfile:///home/michal/Desktop/main-signer/index-mini%20(copy).html\n.error", inputs[0]);
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
                translateIfNeeded("error.", header),
                translateIfNeeded("error.", description)
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
                translateIfNeeded("info.", header),
                translateIfNeeded("info.", description)
        );

        alert.showAndWait();
    }

    private static String translateIfNeeded(String prefix, String input) {
        if (input == null)
            return "";

        if (input.startsWith(prefix))
            return translate(input);
        else
            return input;
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

    private static Alert buildAlert(Alert.AlertType type, String title, String header, String description) {
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

    public static void displayLaunchStatus(boolean launchSuccessful) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(20));
        vbox.getChildren().add(hbox);
        hbox.setSpacing(20);

        try {
            var imageView = new ImageView(new Image(new FileInputStream("src/main/resources/com/octosign/whitelabel/ui/icon.png"), 128, 128, true, true));
            hbox.getChildren().add(imageView);

            var statusText = new Text(translate(launchSuccessful ? "text.signerReady" : "text.launchFailed"));
            statusText.setFont(Font.font("DejaVu Sans", 25));
            hbox.getChildren().add(statusText);
        } catch (FileNotFoundException e) {
            var text = new Text("Loading...");
            text.setFont(Font.font("DejaVu Sans", 25));
            hbox.getChildren().add(text);
        }

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(vbox, 512, 192); //, Color.TRANSPARENT);
        scene.setOnMouseClicked(e -> stage.close());
        scene.getStylesheets().addAll(getStylesheets());
        stage.setScene(scene);
        scheduleClosing(stage);
        stage.show();
    }

    public static void scheduleClosing(Stage stage) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (stage.isShowing())
                Platform.runLater(stage::close);
        });

        thread.setDaemon(true);
        thread.start();
    }
}
