package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.drivers.TokenDriver;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class PickDriverDialogController {
    private TokenDriverLambda callback;
    private List<? extends TokenDriver> drivers;

    @FXML
    VBox formGroup;
    @FXML
    Label error;
    @FXML
    VBox radios;
    @FXML
    VBox mainBox;
    private ToggleGroup toggleGroup;

    public PickDriverDialogController(List<? extends TokenDriver> drivers, TokenDriverLambda callback) {
        this.callback = callback;
        this.drivers = drivers;
    }

    public void initialize() {
        toggleGroup = new ToggleGroup();
        for (TokenDriver driver: drivers) {
            var radioButton = new RadioButton(driver.getName());
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(driver);
            radios.getChildren().add(radioButton);
        }
    }

    public void onPickDriverButtonAction() {
        if(toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
        } else {
            ((Stage) mainBox.getScene().getWindow()).close(); // TODO refactor
            new Thread(() -> {
                var driver = (TokenDriver) toggleGroup.getSelectedToggle().getUserData();
                callback.call(driver);
            }).start();
        }
    }
}
