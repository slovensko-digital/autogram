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
    private PrivateKeyLambda callback;
    private List<DSSPrivateKeyEntry> keys;

    @FXML
    Label keyLabel;

    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        this.keys = keys;
        this.callback = callback;
    }

    public void initialize() {
        keyLabel.setText(keys.get(0).getCertificate().getSubject().getPrettyPrintRFC2253());
    }

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        ((Stage) keyLabel.getScene().getWindow()).close(); // TODO refactor
        new Thread(() -> {
            callback.call(keys.get(0));
        }).start();
    }
}
