package digital.slovensko.autogram.ui.gui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class PasswordController {
    private final Consumer<char[]> callback;

    @FXML
    PasswordField passwordField;
    @FXML
    Text error;
    @FXML
    VBox formGroup;
    @FXML
    VBox mainBox;

    public PasswordController(Consumer<char[]> callback) {
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
            GUIUtils.closeWindow(mainBox);
            new Thread(() -> {
                callback.accept(passwordField.getText().toCharArray());
            }).start();
        }
    }
}
