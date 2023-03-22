package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PrivateKeyLambda;
import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class PickDriverDialogController {
    private TokenDriverLambda callback;
    private List<TokenDriver> drivers;

    @FXML
    Label driverLabel;

    public PickDriverDialogController(List<TokenDriver> drivers, TokenDriverLambda callback) {
        this.callback = callback;
        this.drivers = drivers;
    }

    public void initialize() {
        driverLabel.setText(drivers.get(0).getClass().toString());
    }

    public void onPickDriverButtonAction(ActionEvent event) {
        ((Stage) driverLabel.getScene().getWindow()).close(); // TODO refactor
        new Thread(()-> {
            callback.call(drivers.get(0));
        }).start();
    }
}
