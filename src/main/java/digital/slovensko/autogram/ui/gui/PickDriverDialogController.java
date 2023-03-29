package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PrivateKeyLambda;
import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class PickDriverDialogController {
    private TokenDriverLambda callback;
    private List<TokenDriver> drivers;

    @FXML
    VBox mainBox;
    private ToggleGroup toggleGroup;

    public PickDriverDialogController(List<TokenDriver> drivers, TokenDriverLambda callback) {
        this.callback = callback;
        this.drivers = drivers;
    }

    public void initialize() {
        toggleGroup = new ToggleGroup();
        for (TokenDriver driver: drivers) {
            var radioButton = new RadioButton(driver.getName());
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(driver);
            mainBox.getChildren().add(radioButton);
        }
    }

    public void onPickDriverButtonAction(ActionEvent event) {
        ((Stage) mainBox.getScene().getWindow()).close(); // TODO refactor
        new Thread(()-> {
            var driver = (TokenDriver) toggleGroup.getSelectedToggle().getUserData();
            callback.call(driver);
        }).start();
    }
}
