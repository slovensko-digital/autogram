package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PrivateKeyLambda;
import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class PickKeyDialogController {
    private PrivateKeyLambda callback;
    private List<DSSPrivateKeyEntry> keys;

    @FXML
    VBox mainBox;
    private ToggleGroup toggleGroup;

    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        this.keys = keys;
        this.callback = callback;
    }

    public void initialize() {
        toggleGroup = new ToggleGroup();
        for (DSSPrivateKeyEntry key: keys) {
            var radioButton = new RadioButton(DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(key);
            mainBox.getChildren().add(radioButton);
        }
    }

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        ((Stage) mainBox.getScene().getWindow()).close(); // TODO refactor
        new Thread(() -> {
            var key = (DSSPrivateKeyEntry) toggleGroup.getSelectedToggle().getUserData();
            callback.call(key);
        }).start();
    }
}
