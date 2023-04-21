package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SigningJob;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class MakeCompliantDialogController implements SuppressedFocusController {
    private final SigningJob job;
    private final Consumer<Boolean> callback;

    @FXML
    Pane formGroup;
    @FXML
    Text error;
    @FXML
    Pane radios;
    @FXML
    VBox mainBox;
    private ToggleGroup toggleGroup;

    public MakeCompliantDialogController(SigningJob job, Consumer<Boolean> callback) {
        this.job = job;
        this.callback = callback;
    }

    public void initialize() {
        toggleGroup = new ToggleGroup();

        var yesRadio = new RadioButton("Áno");
        yesRadio.setToggleGroup(toggleGroup);
        yesRadio.setUserData(Boolean.TRUE);

        var noRadio = new RadioButton("Nie");
        noRadio.setToggleGroup(toggleGroup);
        noRadio.setUserData(Boolean.FALSE);

        radios.getChildren().addAll(yesRadio, noRadio);
    }

    public void onPickAnswerButtonAction() {
        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            ((Stage) mainBox.getScene().getWindow()).close();
            var makeCompliant = (Boolean) toggleGroup.getSelectedToggle().getUserData();
            callback.accept(makeCompliant);
        }
    }


    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
