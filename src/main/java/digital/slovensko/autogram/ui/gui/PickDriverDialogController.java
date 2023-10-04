package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.drivers.TokenDriver;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class PickDriverDialogController {
    private final Consumer<TokenDriver> callback;
    private final List<? extends TokenDriver> drivers;

    @FXML
    VBox formGroup;
    @FXML
    Text error;
    @FXML
    VBox radios;
    @FXML
    VBox mainBox;
    private ToggleGroup toggleGroup;

    public PickDriverDialogController(List<? extends TokenDriver> drivers, Consumer<TokenDriver> callback) {
        this.drivers = drivers;
        this.callback = callback;
    }

    public void initialize() {
        toggleGroup = new ToggleGroup();
        for (TokenDriver driver : drivers) {
            var radioButton = new RadioButton(driver.getName());
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(driver);
            radios.getChildren().add(radioButton);
        }
    }

    public void onPickDriverButtonAction() {
        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            GUIUtils.closeWindow(mainBox);
            var driver = (TokenDriver) toggleGroup.getSelectedToggle().getUserData();
            callback.accept(driver);
        }
    }
}
