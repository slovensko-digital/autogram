package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PrivateKeyLambda;
import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class PickKeyDialogController {
    public PrivateKeyLambda callback;
    public List<TokenDriver> drivers;
    public List<DSSPrivateKeyEntry> keys;

    @FXML
    Label keyLabel;

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        ((Stage) keyLabel.getScene().getWindow()).close(); // TODO refactor
        new Thread(() -> {
            callback.call(keys.get(0));
        }).start();
    }
}
