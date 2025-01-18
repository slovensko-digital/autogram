package digital.slovensko.autogram.ui.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PDFPasswordController {
    private final String questionText;
    private final String descriptionText;
    private final String errorText;

    private char[] password;

    @FXML
    PasswordField passwordField;
    @FXML
    Text question;
    @FXML
    Text description;
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

    public PDFPasswordController(String questionText, String description, String blankPasswordErrorText) {
        this.questionText = questionText;
        this.descriptionText = description;
        this.errorText = blankPasswordErrorText;
    }

    public void initialize() {
        question.setText(questionText);

        if (descriptionText != null) {
            description.setText(descriptionText);
            description.setManaged(true);
            description.setVisible(true);
        }

        error.setText(errorText);
    }

    public void onPasswordAction() {
        if (passwordField.getText().isEmpty()) {
            error.setManaged(true);
            error.setVisible(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            passwordField.getStyleClass().add("autogram-input--error");

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
}
