package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.DefaultDriverDetector;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import digital.slovensko.autogram.drivers.FakeTokenDriver;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class SettingsDialogController {
    @FXML
    private ChoiceBox<SignatureLevel> signatureLevelChoiceBoxBox;
    @FXML
    private HBox en319132Radios;
    @FXML
    private ChoiceBox<TokenDriver> driverChoiceBox;
    @FXML
    private VBox trustedCountriesList;
    @FXML
    private ScrollPane trustedCountriesListScrollPane;
    @FXML
    private HBox correctDocumentDisplayRadios;
    @FXML
    private HBox signatureValidationRadios;
    @FXML
    private HBox checkPDFAComplianceRadios;
    @FXML
    private HBox localServerEnabledRadios;
    @FXML
    private Button saveButton;
    @FXML
    private Button closeButton;

    private final UserSettings userSettings;

    public SettingsDialogController(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public void initialize() {
        initializeSignatureLevelChoiceBox();
        initializeDriverChoiceBox();
        initializeEn319132CheckBox();
        initializeCorrectDocumentDisplayCheckBox();
        initializeSignatureValidationCheckBox();
        initializeCheckPDFAComplianceCheckBox();
        initializeLocalServerEnabledCheckBox();
        initializeTrustedCountriesList();
    }

    private void initializeSignatureLevelChoiceBox() {
        signatureLevelChoiceBoxBox.getItems().addAll(List.of(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B));
        signatureLevelChoiceBoxBox.setConverter(new SignatureLevelStringConverter());
        signatureLevelChoiceBoxBox.setValue(userSettings.getSignatureLevel());
        signatureLevelChoiceBoxBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    userSettings.setSignatureLevel(newValue);
                });
    }

    private void initializeDriverChoiceBox() {
        driverChoiceBox.setConverter(new TokenDriverStringConverter());
        driverChoiceBox.getItems().add(new FakeTokenDriver("Žiadny", null, false, "none"));
        driverChoiceBox.getItems().addAll(new DefaultDriverDetector().getAvailableDrivers());
        var defaultDriver = driverChoiceBox.getItems().stream()
                .filter(d -> d != null && d.getName().equals(userSettings.getDriver())).findFirst();
        driverChoiceBox.setValue(defaultDriver.orElse(null));
        driverChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setDriver(newValue.getName());
        });
    }

    private void initializeBooleanRadios(HBox parent, Consumer<Boolean> consumer, boolean defaultValue, String yesText,
            String noText) {
        var toggleGroup = new ToggleGroup();

        var yes = new RadioButton(yesText);
        yes.setToggleGroup(toggleGroup);
        parent.getChildren().add(yes);

        var no = new RadioButton(noText);
        no.setToggleGroup(toggleGroup);
        parent.getChildren().add(no);

        yes.selectedProperty().addListener((observable, oldValue, newValue) -> {
            consumer.accept(newValue);
        });

        if (defaultValue)
            yes.setSelected(true);
        else
            no.setSelected(true);
    }

    private void initializeBooleanRadios(HBox parent, Consumer<Boolean> consumer, boolean defaultValue) {
        initializeBooleanRadios(parent, consumer, defaultValue, "Áno", "Nie");
    }

    private void initializeEn319132CheckBox() {
        initializeBooleanRadios(en319132Radios, t -> userSettings.setEn319132(t), userSettings.isEn319132());
    }

    private void initializeCorrectDocumentDisplayCheckBox() {
        initializeBooleanRadios(correctDocumentDisplayRadios, t -> userSettings.setCorrectDocumentDisplay(t),
                userSettings.isCorrectDocumentDisplay());
    }

    private void initializeSignatureValidationCheckBox() {
        initializeBooleanRadios(signatureValidationRadios, t -> userSettings.setSignaturesValidity(t),
                userSettings.isSignaturesValidity());
    }

    private void initializeCheckPDFAComplianceCheckBox() {
        initializeBooleanRadios(checkPDFAComplianceRadios, t -> userSettings.setPdfaCompliance(t),
                userSettings.isPdfaCompliance());
    }

    private void initializeLocalServerEnabledCheckBox() {
        initializeBooleanRadios(localServerEnabledRadios, t -> userSettings.setServerEnabled(t),
                userSettings.isServerEnabled());
    }

    private void initializeTrustedCountriesList() {
        var europeanCountries = List.of(
                new Country("Belgicko", "BE"),
                new Country("Bulharsko", "BG"),
                new Country("Česká republika", "CZ"),
                new Country("Chorvátsko", "HR"),
                new Country("Cyprus", "CY"),
                new Country("Dánsko", "DK"),
                new Country("Estónsko", "EE"),
                new Country("Fínsko", "FI"),
                new Country("Francúzsko", "FR"),
                new Country("Grécko", "GR"),
                new Country("Holandsko", "NL"),
                new Country("Írsko", "IE"),
                new Country("Litva", "LT"),
                new Country("Lotyšsko", "LV"),
                new Country("Luxembursko", "LU"),
                new Country("Maďarsko", "HU"),
                new Country("Malta", "MT"),
                new Country("Nemecko", "DE"),
                new Country("Poľsko", "PL"),
                new Country("Portugalsko", "PT"),
                new Country("Rakúsko", "AT"),
                new Country("Rumunsko", "RO"),
                new Country("Slovensko", "SK"),
                new Country("Slovinsko", "SI"),
                new Country("Španielsko", "ES"),
                new Country("Švédsko", "SE"),
                new Country("Taliansko", "IT"));

        var trustedList = userSettings.getTrustedList();
        trustedCountriesList.getChildren().addAll(europeanCountries.stream()
                .map(country -> createCountryElement(country, trustedList.contains(country.getShortname()))).toList());
    }

    private HBox createCountryElement(Country country, boolean isCountryInTrustedList) {
        var countryBox = new VBox(new TextFlow(new Text(country.getName())));
        countryBox.getStyleClass().add("left");

        var checkBox = new CheckBox(isCountryInTrustedList ? "Zapnuté" : "Vypnuté");
        checkBox.setSelected(isCountryInTrustedList);
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) {
                userSettings.addToTrustedList(country.getShortname());
                checkBox.setText("Zapnuté");
            } else {
                userSettings.removeFromTrustedList(country.getShortname());
                checkBox.setText("Vypnuté");
            }
        });

        return new HBox(countryBox, new VBox(checkBox));
    }

    public void onCancelButtonAction() {
        var stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
