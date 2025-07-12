package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

import static digital.slovensko.autogram.util.DSSUtils.buildTooltipLabel;
import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class PickKeyDialogController extends BaseController {
    private final Consumer<DSSPrivateKeyEntry> callback;
    private final List<DSSPrivateKeyEntry> keys;
    private final boolean expiredCertsEnabled;

    @FXML
    VBox formGroup;
    @FXML
    Text error;
    @FXML
    VBox mainBox;
    @FXML
    VBox radios;
    private ToggleGroup toggleGroup;


    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback, boolean expiredCertsEnabled) {
        this.keys = keys;
        this.callback = callback;
        this.expiredCertsEnabled = expiredCertsEnabled;
    }

    @Override
    public void initialize() {
        toggleGroup = new ToggleGroup();
        for (var key : keys) {
            Node badge = new HBox();
            if (!key.getCertificate().isValidOn(new java.util.Date())) {
                badge = SignatureBadgeFactory.createInfoBadge("Exspirovaný certifikát");

                if (!expiredCertsEnabled)
                    continue;
            }

            var radioButton = new RadioButton(parseCN(key.getCertificate().getSubject().getRFC2253()));
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(key);
            radios.getChildren().add(new HBox(radioButton, badge));

            var tooltip = new Tooltip(buildTooltipLabel(key));
            tooltip.setShowDuration(Duration.seconds(10));
            tooltip.setWrapText(true);
            tooltip.setPrefWidth(400);
            radioButton.setTooltip(tooltip);
        }

        if (keys.size() == 1)
            toggleGroup.getToggles().get(0).setSelected(true);
    }

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            GUIUtils.closeWindow(mainBox);
            var key = (DSSPrivateKeyEntry) toggleGroup.getSelectedToggle().getUserData();
            callback.accept(key);
        }
    }
}
