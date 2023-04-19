package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PrivateKeyLambda;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

import static digital.slovensko.autogram.util.DSSUtils.buildTooltipLabel;
import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class PickKeyDialogController {
    private PrivateKeyLambda callback;
    private List<DSSPrivateKeyEntry> keys;

    @FXML
    VBox formGroup;
    @FXML
    Text error;
    @FXML
    VBox mainBox;
    @FXML
    VBox radios;
    private ToggleGroup toggleGroup;


    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        this.keys = keys;
        this.callback = callback;
    }

    public void initialize() {
        toggleGroup = new ToggleGroup();
        for (DSSPrivateKeyEntry key : keys) {
            var radioButton = new RadioButton(parseCN(key.getCertificate().getSubject().getRFC2253()));
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(key);
            radios.getChildren().add(radioButton);

            Tooltip tooltip = new Tooltip(buildTooltipLabel(key));
            tooltip.setShowDuration(Duration.seconds(10));
            tooltip.setWrapText(true);
            tooltip.setPrefWidth(400);
            radioButton.setTooltip(tooltip);
        }
    }

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            ((Stage) mainBox.getScene().getWindow()).close(); // TODO refactor
            var key = (DSSPrivateKeyEntry) toggleGroup.getSelectedToggle().getUserData();
            callback.call(key);
        }
    }
}
