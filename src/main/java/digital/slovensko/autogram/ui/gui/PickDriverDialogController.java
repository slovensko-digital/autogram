package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    Button showSignatureTypeButton;

    @FXML
    VBox signatureType;

    @FXML
    private ChoiceBox<SignatureLevel> signatureLevelChoiceBoxBox;

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
        initializeSignatureLevelChoiceBox();
    }

    private void initializeSignatureLevelChoiceBox() {
        var userSettings = UserSettings.load();
        signatureLevelChoiceBoxBox.getItems().addAll(List.of(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B));
        signatureLevelChoiceBoxBox.setConverter(new SignatureLevelStringConverter());
        signatureLevelChoiceBoxBox.setValue(userSettings.getSignatureLevel());
        signatureLevelChoiceBoxBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignatureLevel(newValue);
            userSettings.save();
        });
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

    public void onShowSignatureTypeAction() {
        if (signatureType.isVisible()) {
            signatureType.setManaged(false);
            signatureType.setVisible(false);
            showSignatureTypeButton.getStyleClass().remove("autogram-error-summary__more-open");
        } else {
            signatureType.setManaged(true);
            signatureType.setVisible(true);
            showSignatureTypeButton.getStyleClass().add("autogram-error-summary__more-open");
        }
        signatureType.getScene().getWindow().sizeToScene();
    }
}
