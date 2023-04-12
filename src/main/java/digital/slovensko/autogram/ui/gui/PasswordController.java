package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PasswordLambda;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PasswordController {
    private final PasswordLambda callback;

    @FXML
    PasswordField passwordField;
    @FXML
    Text error;
    @FXML
    VBox formGroup;
    @FXML
    VBox mainBox;

    public PasswordController(PasswordLambda callback) {
        this.callback = callback;
    }

    public void onPasswordAction() {
        if (passwordField.getText().equals("")) {
            error.setManaged(true);
            error.setVisible(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            passwordField.getStyleClass().add("autogram-input--error");

            formGroup.getScene().getWindow().sizeToScene();
            passwordField.requestFocus();
        } else {
            ((Stage) mainBox.getScene().getWindow()).close(); // TODO refactor
            new Thread(() -> {
                callback.call(passwordField.getText().toCharArray());
            }).start();
        }
    }
}
