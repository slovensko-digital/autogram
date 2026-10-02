package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PasswordController extends BaseController {
    private final String questionKey;
    private final String errorKey;
    private final String subtitleKey;
    private final boolean isSigningStep;
    private final boolean allowEmpty;
    private final AutogramException previousError;

    private char[] password;

    @FXML
    PasswordField passwordField;
    @FXML
    Text question;
    @FXML
    Text subtitle;
    @FXML
    Text error;
    @FXML
    VBox formGroup;
    @FXML
    Button mainButton;
    @FXML
    Button cancelButton;
    @FXML
    VBox mainBox;

    public PasswordController(String questionKey, String blankPasswordErrorKey, String subtitleKey, boolean isSigningStep, boolean allowEmpty) {
        this(questionKey, blankPasswordErrorKey, subtitleKey, isSigningStep, allowEmpty, null);
    }

    public PasswordController(String questionKey, String blankPasswordErrorKey, String subtitleKey,
            boolean isSigningStep, boolean allowEmpty, AutogramException previousError) {
        this.questionKey = questionKey;
        this.errorKey = blankPasswordErrorKey;
        this.subtitleKey = subtitleKey;
        this.isSigningStep = isSigningStep;
        this.allowEmpty = allowEmpty;
        this.previousError = previousError;
    }

    @Override
    public void initialize() {
        question.setText(i18n(questionKey));
        error.setText(i18n(errorKey));
        if (previousError != null)
            showInputError(previousError.getSubheading(resources));
        if(subtitleKey != null) {
            subtitle.setText(i18n(subtitleKey));
            subtitle.setManaged(true);
            subtitle.setVisible(true);
        }

        if(isSigningStep) {
            mainButton.setText(i18n("general.sign.btn.single"));
            cancelButton.setManaged(true);
            cancelButton.setVisible(true);
        }
    }

    public void onPasswordAction() {
        if (passwordField.getText().isEmpty() && !allowEmpty) {
            showInputError(i18n(errorKey));

            formGroup.getScene().getWindow().sizeToScene();
            passwordField.requestFocus();
        } else {
            this.password = passwordField.getText().toCharArray();
            GUIUtils.closeWindow(mainBox);
        }
    }

    public void onCancelButtonPressed(ActionEvent event) {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public char[] getPassword() {
        return password;
    }

    private void showInputError(String message) {
        error.setText(message);
        error.setManaged(true);
        error.setVisible(true);
        if (!formGroup.getStyleClass().contains("autogram-form-group--error"))
            formGroup.getStyleClass().add("autogram-form-group--error");
        if (!passwordField.getStyleClass().contains("autogram-input--error"))
            passwordField.getStyleClass().add("autogram-input--error");
    }
}
