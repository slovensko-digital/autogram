package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.DefaultDriverDetector;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;

public class SettingsDialogController {

    @FXML
    private ChoiceBox<SignatureLevel> signatureLevelChoiceBoxBox;

    @FXML
    private CheckBox en319132CheckBox;

    @FXML
    private HBox batchSigningRadioButtons;

    private ToggleGroup toggleGroup;

    @FXML
    private ChoiceBox<TokenDriver> driverChoiceBox;

    @FXML
    private VBox trustedCountriesList;

    @FXML
    private CheckBox correctDocumentDisplayCheckBox;

    @FXML
    private CheckBox signatureValidationCheckBox;

    @FXML
    private CheckBox checkPDFAComplianceCheckBox;

    @FXML
    private CheckBox localServerEnabledCheckBox;

    @FXML
    private Button closeButton;

    private UserSettings userSettings;

    public void initialize() {
        userSettings = UserSettings.load();

        initializeSignatureLevelChoiceBox();
        initializeDriverChoiceBox();
        initializeEn319132CheckBox();
        initializeCorrectDocumentDisplayCheckBox();
        initializeSignatureValidationCheckBox();
        initializeCheckPDFAComplianceCheckBox();
        initializeLocalServerEnabledCheckBox();
        initializeBatchSigningRadioButtons();
        initializeTrustedCountriesList();
    }

    private void initializeSignatureLevelChoiceBox() {
        signatureLevelChoiceBoxBox.getItems().addAll(List.of(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B));
        signatureLevelChoiceBoxBox.setConverter(new SignatureLevelStringConverter());
        signatureLevelChoiceBoxBox.setValue(userSettings.getSignatureLevel());
        signatureLevelChoiceBoxBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignatureLevel(newValue);
        });
    }

    private void initializeDriverChoiceBox() {
        driverChoiceBox.setConverter(new TokenDriverStringConverter());
        driverChoiceBox.getItems().add(null);
        driverChoiceBox.getItems().addAll(new DefaultDriverDetector().getAvailableDrivers());
        driverChoiceBox.setValue(userSettings.getDriver());
        driverChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setDriver(newValue);
        });
    }

    private void initializeEn319132CheckBox() {
        en319132CheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setEn319132(newValue);
        });
        en319132CheckBox.setSelected(userSettings.isEn319132());
    }

    private void initializeCorrectDocumentDisplayCheckBox() {
        correctDocumentDisplayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setCorrectDocumentDisplay(newValue);
            correctDocumentDisplayCheckBox.setText(newValue ? "Zapnutá" : "Vypnutá");
        });
        correctDocumentDisplayCheckBox.setSelected(userSettings.isCorrectDocumentDisplay());
    }

    private void initializeSignatureValidationCheckBox() {
        signatureValidationCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignaturesValidity(newValue);
            signatureValidationCheckBox.setText(newValue ? "Zapnutá" : "Vypnutá");
        });
        signatureValidationCheckBox.setSelected(userSettings.isSignaturesValidity());
    }

    private void initializeCheckPDFAComplianceCheckBox() {
        checkPDFAComplianceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setPdfaCompliance(newValue);
            checkPDFAComplianceCheckBox.setText(newValue ? "Zapnutá" : "Vypnutá");
        });
        checkPDFAComplianceCheckBox.setSelected(userSettings.isPdfaCompliance());
    }

    private void initializeLocalServerEnabledCheckBox() {
        localServerEnabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setServerEnabled(newValue);
            localServerEnabledCheckBox.setText(newValue ? "Zapnutý" : "Vypnutý");
        });
        localServerEnabledCheckBox.setSelected(userSettings.isServerEnabled());
    }

    private void initializeBatchSigningRadioButtons() {
        toggleGroup = new ToggleGroup();

        var togetherRadioButton = new RadioButton("Spoločne");
        togetherRadioButton.setToggleGroup(toggleGroup);
        batchSigningRadioButtons.getChildren().add(togetherRadioButton);

        var individaullyRadioButton = new RadioButton("Samostatne");
        individaullyRadioButton.setToggleGroup(toggleGroup);
        batchSigningRadioButtons.getChildren().add(individaullyRadioButton);

        individaullyRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setSignIndividually(newValue);
        });
        individaullyRadioButton.setSelected(userSettings.isSignIndividually());
    }

    private void initializeTrustedCountriesList() {
        var europeanCountries = List.of(
                "Belgicko", "Bulharsko", "Česká republika",
                "Chorvátsko", "Cyprus", "Dánsko",
                "Estónsko", "Fínsko", "Francúzsko",
                "Grécko", "Holandsko", "Írsko",
                "Litva", "Lotyšsko", "Luxembursko",
                "Maďarsko", "Malta", "Nemecko",
                "Poľsko", "Portugalsko", "Rakúsko",
                "Rumunsko", "Slovensko", "Slovinsko",
                "Španielsko", "Švédsko", "Taliansko");

        List<String> trustedList = userSettings.getTrustedList();
        for (String country : europeanCountries) {
            var isInTrustedList = trustedList.contains(country);
            var hbox = createCountryElement(country, isInTrustedList);
            trustedCountriesList.getChildren().add(hbox);
        }
    }

    private HBox createCountryElement(String country, boolean isCountryInTrustedList) {
        var hbox = new HBox();
        hbox.setStyle("-fx-border-width: 0 0 1px 0; -fx-border-color: gray; -fx-padding: 5px;");

        var countryBox = new VBox();
        countryBox.setMinWidth(325);
        var countryText = new Text(country);
        countryText.getStyleClass().add("autogram-heading-s");
        var textFlow = new TextFlow(countryText);
        textFlow.getStyleClass().add("autogram-heading");
        countryBox.getChildren().add(textFlow);

        var checkBoxBox = new VBox();
        checkBoxBox.setAlignment(Pos.CENTER_LEFT);
        checkBoxBox.setMinWidth(325);
        var checkBoxLabel = isCountryInTrustedList ? "Zapnuté" : "Vypnuté";
        var checkBox = new CheckBox(checkBoxLabel);
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
        return hbox;
    }

    public void onSaveButtonAction() {
        userSettings.save();
    }

    public void onCancelButtonAction() {
        var stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
