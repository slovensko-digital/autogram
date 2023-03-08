package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.drivers.TokenDriver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class PickDriverDialogController {
    public TokenDriverLambda callback;
    public List<TokenDriver> drivers;

    @FXML
    Label driverLabel;

    public void onPickDriverButtonAction(ActionEvent event) {
        ((Stage) driverLabel.getScene().getWindow()).close(); // TODO refactor
        new Thread(()-> {
            callback.call(drivers.get(0));
        }).start();
    }
}
