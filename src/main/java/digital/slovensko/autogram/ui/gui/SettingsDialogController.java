package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.UserSettings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class SettingsDialogController {

    @FXML
    private ChoiceBox<String> signatureTypeBox;

    @FXML
    private ChoiceBox<String> driverBox;

    @FXML
    private CheckBox standardCheckBox;

    @FXML
    private CheckBox correctDocumentDisplaycheckBox;

    @FXML
    private CheckBox signatureValidationCheckBox;

    @FXML
    private CheckBox checkPDFAComplianceCheckBox;

    @FXML
    private CheckBox localServerEnabledCheckBox;

    @FXML
    HBox radios;

    private ToggleGroup toggleGroup;

    @FXML
    private VBox countryList;

    @FXML private Button closeButton;

    private UserSettings userSettings;

    public void initialize() {
        userSettings = UserSettings.load();

        signatureTypeBox.getItems().addAll("ASIC XADES", "ASIC CADES", "PADES");
        signatureTypeBox.setValue(userSettings.getSignatureType());
        signatureTypeBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignatureType(newValue);
        });

        driverBox.getItems().addAll("", "Občiansky preukaz (eID klient)", "I.CA SecureStore", "MONET+ ProID+Q", "Gemalto IDPrime 940", "Fake token driver");
        driverBox.setValue(userSettings.getDriver());
        driverBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setDriver(newValue);
        });

        standardCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setEn319132(newValue);
        });
        standardCheckBox.setSelected(userSettings.isEn319132());

        correctDocumentDisplaycheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setCorrectDocumentDisplay(newValue);
            if (newValue) {
                correctDocumentDisplaycheckBox.setText("Zapnutá");
            } else {
                correctDocumentDisplaycheckBox.setText("Vypnutá");
            }
        });
        correctDocumentDisplaycheckBox.setSelected(userSettings.isCorrectDocumentDisplay());

        signatureValidationCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignaturesValidity(newValue);
            if (newValue) {
                signatureValidationCheckBox.setText("Zapnutá");
            } else {
                signatureValidationCheckBox.setText("Vypnutá");
            }
        });
        signatureValidationCheckBox.setSelected(userSettings.isSignaturesValidity());

        checkPDFAComplianceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setPdfaCompliance(newValue);
            if (newValue) {
                checkPDFAComplianceCheckBox.setText("Zapnutá");
            } else {
                checkPDFAComplianceCheckBox.setText("Vypnutá");
            }
        });
        checkPDFAComplianceCheckBox.setSelected(userSettings.isPdfaCompliance());

        localServerEnabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setServerEnabled(newValue);
            if (newValue) {
                localServerEnabledCheckBox.setText("Zapnutý");
            } else {
                localServerEnabledCheckBox.setText("Vypnutý");
            }
        });
        localServerEnabledCheckBox.setSelected(userSettings.isServerEnabled());
        localServerEnabledCheckBox.setText(userSettings.isServerEnabled() ? "Zapnutý" : "Vypnutý");

        toggleGroup = new ToggleGroup();

        var togetherRadioButton = new RadioButton("Spoločne");
        togetherRadioButton.setToggleGroup(toggleGroup);
        radios.getChildren().add(togetherRadioButton);
        var individaullyRadioButton = new RadioButton("Samostatne");
        individaullyRadioButton.setToggleGroup(toggleGroup);
        individaullyRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignIndividually(newValue);
        });
        radios.getChildren().add(individaullyRadioButton);
//        radioButton.setUserData(driver);

        if (userSettings.isSignIndividually()) {
            individaullyRadioButton.setSelected(true);
        } else {
            togetherRadioButton.setSelected(true);
        }

        String[] countries = {
                "Belgicko", "Bulharsko", "Česká republika",
                "Chorvátsko", "Cyprus", "Dánsko",
                "Estónsko", "Fínsko", "Francúzsko",
                "Grécko", "Holandsko", "Írsko",
                "Litva", "Lotyšsko", "Luxembursko",
                "Maďarsko", "Malta", "Nemecko",
                "Poľsko", "Portugalsko", "Rakúsko",
                "Rumunsko", "Slovensko", "Slovinsko",
                "Španielsko", "Švédsko", "Taliansko"
        };

        List<String> trustedList = userSettings.getTrustedList();

        for (String country : countries) {
            HBox hbox = new HBox();
            hbox.getStyleClass().add("autogram-settings-body-container");

            VBox countryBox = new VBox();
            countryBox.setMinWidth(325);

            Text countryText = new Text(country);
            countryText.getStyleClass().add("autogram-heading-s");
            TextFlow textFlow = new TextFlow(countryText);
            textFlow.getStyleClass().add("autogram-heading");
            countryBox.getChildren().add(textFlow);

            VBox checkBoxBox = new VBox();
            checkBoxBox.setAlignment(Pos.CENTER_LEFT);
            checkBoxBox.setMinWidth(325);

            var isCountryInTrustedList = trustedList.contains(country);
            var checkBoxLabel = isCountryInTrustedList ? "Zapnuté" : "Vypnuté";
            CheckBox checkBox = new CheckBox(checkBoxLabel);
            checkBox.setSelected(isCountryInTrustedList);
            checkBox.getStyleClass().addAll("custom-checkbox");
            checkBoxBox.getChildren().add(checkBox);

            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    userSettings.addToTrustedList(country);
                    checkBox.setText("Zapnuté");
                } else {
                    userSettings.removeFromTrustedList(country);
                    checkBox.setText("Vypnuté");
                }
            });

            hbox.getChildren().addAll(countryBox, checkBoxBox);
            countryList.getChildren().add(hbox);
        }
    }

    public void onSaveButtonAction() {
        userSettings.saveSettings();
    }

    public void onCancelButtonAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
