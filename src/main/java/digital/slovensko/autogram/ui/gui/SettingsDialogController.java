package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.DefaultDriverDetector;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import digital.slovensko.autogram.drivers.FakeTokenDriver;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;

public class SettingsDialogController extends BaseController {
    @FXML
    private ChoiceBox<SignatureLevel> signatureLevelChoiceBoxBox;
    @FXML
    private HBox tsaEnabledRadios;
    @FXML
    private ChoiceBox<String> tsaChoiceBox;
    @FXML
    private TextField customTsaServerTextField;
    @FXML
    private HBox bulkEnabledRadios;
    @FXML
    private HBox en319132Radios;
    @FXML
    private HBox plainXmlEnabledRadios;
    @FXML
    private TextField tokenSessionTimeoutTextField;
    @FXML
    private ChoiceBox<TokenDriver> driverChoiceBox;
    @FXML
    private VBox trustedCountriesList;
    @FXML
    private HBox correctDocumentDisplayRadios;
    @FXML
    private HBox signatureValidationRadios;
    @FXML
    private HBox checkPDFAComplianceRadios;
    @FXML
    private HBox expiredCertsRadios;
    @FXML
    private HBox localServerEnabledRadios;
    @FXML
    private ChoiceBox<SupportedLanguage> languageChoiceBox;
    @FXML
    private ChoiceBox<String> pdfDpiChoiceBox;
    @FXML
    private TextField customKeystorePathTextField;
    @FXML
    private TextField customPKCS11DriverPathTextField;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button closeButton;
    @FXML
    private VBox driverSlot;

    private final UserSettings userSettings;
    private final List<String> preDefinedTsaServers = List.of(
            "http://tsa.baltstamp.lt,http://ts.quovadisglobal.com/eu",
            "http://tsa.baltstamp.lt",
            "http://ts.quovadisglobal.com/eu"
    );

    public SettingsDialogController(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    @Override
    public void initialize() {
        initializeSignatureLevelChoiceBox();
        initializeDriverChoiceBox();
        initializeTsaEnabled();
        initializeTsaServer();
        initializeBulkEnabledCheckbox();
        initializeEn319132CheckBox();
        initializePlainXmlEnabledCheckBox();
        initializeTokenSessionTimeoutTextField();
        initializeCorrectDocumentDisplayCheckBox();
        initializeSignatureValidationCheckBox();
        initializeCheckPDFAComplianceCheckBox();
        initializeExpiredCertsEnabledCheckBox();
        initializeLocalServerEnabledCheckBox();
        initializeTrustedCountriesList();
        initializeLanguageSettings();
        initializePdfDpiSettings();
        initializeCustomKeystoreSettings();
        initializeCustomPKCS11DriverPathSettings();
        initializeDriverSlot();
    }

    private void initializeSignatureLevelChoiceBox() {
        signatureLevelChoiceBoxBox.getItems().addAll(List.of(
                SignatureLevel.XAdES_BASELINE_B,
                SignatureLevel.PAdES_BASELINE_B,
                SignatureLevel.CAdES_BASELINE_B));
        signatureLevelChoiceBoxBox.setConverter(new SignatureLevelStringConverter());
        signatureLevelChoiceBoxBox.setValue(userSettings.getSignatureLevel());
        signatureLevelChoiceBoxBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    userSettings.setSignatureLevel(newValue);
                });
    }

    private void initializeDriverChoiceBox() {
        var driverDetector = new DefaultDriverDetector(userSettings);
        driverChoiceBox.setConverter(new TokenDriverStringConverter(driverDetector));
        driverChoiceBox.getItems().add(new FakeTokenDriver(i18n("settings.signing.defaultDriver.none.label"), null, "none", ""));
        driverChoiceBox.getItems().addAll(driverDetector.getAvailableDrivers());
        var defaultDriver = driverChoiceBox.getItems().stream()
                .filter(d -> d != null && d.getShortname().equals(userSettings.getDefaultDriver())).findFirst();
        driverChoiceBox.setValue(defaultDriver.orElse(null));
        driverChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            userSettings.setDriver(newValue.getShortname());
        });
    }

    private void initializeTsaEnabled() {
        initializeBooleanRadios(tsaEnabledRadios, userSettings::setTsaEnabled, userSettings.getTsaEnabled());
    }

    private void initializeTsaServer() {
        final var USE_CUSTOM_TSA_LABEL = i18n("settings.signing.tsaServer.custom.label");

        customTsaServerTextField.setText(userSettings.getCustomTsaServer());
        customTsaServerTextField.setOnKeyTyped((e) -> {
            userSettings.setCustomTsaServer(customTsaServerTextField.getText());
            userSettings.setTsaServer(customTsaServerTextField.getText());
        });

        tsaChoiceBox.getItems().add(USE_CUSTOM_TSA_LABEL);
        for (var server : preDefinedTsaServers)
            tsaChoiceBox.getItems().add(server);

        tsaChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           if (newValue.equals(USE_CUSTOM_TSA_LABEL)) {
               userSettings.setTsaServer(customTsaServerTextField.getText());
               customTsaServerTextField.setDisable(false);
               return;
           }

           userSettings.setTsaServer(newValue);
           customTsaServerTextField.setDisable(true);
        });

        if (!preDefinedTsaServers.contains(userSettings.getTsaServer())) {
            tsaChoiceBox.setValue(USE_CUSTOM_TSA_LABEL);
            customTsaServerTextField.setText(userSettings.getTsaServer());
            userSettings.setCustomTsaServer(userSettings.getTsaServer());
            return;
        }

        tsaChoiceBox.setValue(userSettings.getTsaServer());
        customTsaServerTextField.setDisable(true);
    }

    private void initializeBooleanRadios(HBox parent, Consumer<Boolean> consumer, boolean defaultValue) {
        var toggleGroup = new ToggleGroup();

        var yes = new RadioButton(i18n("general.yes"));
        yes.setToggleGroup(toggleGroup);
        parent.getChildren().add(yes);

        var no = new RadioButton(i18n("general.no"));
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

    private void initializeBulkEnabledCheckbox() {
        initializeBooleanRadios(bulkEnabledRadios, userSettings::setBulkEnabled, userSettings.isBulkEnabled());
    }

    private void initializeEn319132CheckBox() {
        initializeBooleanRadios(en319132Radios, userSettings::setEn319132, userSettings.isEn319132());
    }

    private void initializePlainXmlEnabledCheckBox() {
        initializeBooleanRadios(plainXmlEnabledRadios, userSettings::setPlainXmlEnabled, userSettings.isPlainXmlEnabled());
    }

    private void initializeCorrectDocumentDisplayCheckBox() {
        initializeBooleanRadios(correctDocumentDisplayRadios, userSettings::setCorrectDocumentDisplay,
                userSettings.isCorrectDocumentDisplay());
    }

    private void initializeSignatureValidationCheckBox() {
        initializeBooleanRadios(signatureValidationRadios, userSettings::setSignaturesValidity,
                userSettings.isSignaturesValidity());
    }

    private void initializeCheckPDFAComplianceCheckBox() {
        initializeBooleanRadios(checkPDFAComplianceRadios, userSettings::setPdfaCompliance,
                userSettings.isPdfaCompliance());
    }

    private void initializeExpiredCertsEnabledCheckBox() {
        initializeBooleanRadios(expiredCertsRadios, userSettings::setExpiredCertsEnabled,
                userSettings.isExpiredCertsEnabled());
    }

    private void initializeLocalServerEnabledCheckBox() {
        initializeBooleanRadios(localServerEnabledRadios, userSettings::setServerEnabled,
                userSettings.isServerEnabled());
    }

    private void initializeTrustedCountriesList() {
        var europeanCountries = List.of(
                new Country("BE"),
                new Country("BG"),
                new Country("CZ"),
                new Country("HR"),
                new Country("CY"),
                new Country("DK"),
                new Country("EE"),
                new Country("FI"),
                new Country("FR"),
                new Country("EL", "GR"),
                new Country("NL"),
                new Country("IE"),
                new Country("LT"),
                new Country("LV"),
                new Country("LU"),
                new Country("HU"),
                new Country("MT"),
                new Country("DE"),
                new Country("PL"),
                new Country("PT"),
                new Country("AT"),
                new Country("RO"),
                new Country("SK"),
                new Country("SI"),
                new Country("ES"),
                new Country("SE"),
                new Country("IT"));

        var trustedList = userSettings.getTrustedList();
        trustedCountriesList.getChildren().addAll(europeanCountries.stream()
                .map(country -> createCountryElement(country, trustedList.contains(country.getShortname()))).toList());
    }

    private HBox createCountryElement(Country country, boolean isCountryInTrustedList) {
        var countryBox = new VBox(new TextFlow(new Text(country.getName(resources.getLocale()))));
        countryBox.getStyleClass().add("left");

        var checkBox = new CheckBox(isCountryInTrustedList ? i18n("general.on.label") : i18n("general.off.label"));
        checkBox.setSelected(isCountryInTrustedList);
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) {
                userSettings.addToTrustedList(country.getShortname());
                checkBox.setText(i18n("general.on.label"));
            } else {
                userSettings.removeFromTrustedList(country.getShortname());
                checkBox.setText(i18n("general.off.label"));
            }
        });

        return new HBox(countryBox, new VBox(checkBox));
    }


    private void initializeLanguageSettings() {
        var items = observableArrayList(SupportedLanguage.values());
        items.addFirst(SupportedLanguage.SYSTEM);
        languageChoiceBox.setItems(items);
        languageChoiceBox.setValue(userSettings.getLanguage().orElse(SupportedLanguage.SYSTEM));
        languageChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SupportedLanguage language) {
                if (language == SupportedLanguage.SYSTEM) {
                    return i18n("settings.other.language.system.label");
                }

                return language.getDisplayLanguage();
            }

            @Override
            public SupportedLanguage fromString(String string) {
                return null; // not editable, will never be called
            }
        });
        languageChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    userSettings.setLanguage(newValue);
                });
    }

    private void initializePdfDpiSettings() {
        pdfDpiChoiceBox.getItems().addAll("50 dpi", "70 dpi", "100 dpi", "150 dpi", "200 dpi", "300 dpi");
        pdfDpiChoiceBox.setValue(String.valueOf(userSettings.getPdfDpi()) + " dpi");
        pdfDpiChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                    userSettings.setPdfDpi(Integer.parseInt(newValue.replace(" dpi", ""))));
    }

    private void initializeCustomKeystoreSettings() {
        customKeystorePathTextField.setText(userSettings.getCustomKeystorePath());
        customKeystorePathTextField.setOnKeyTyped((e) -> {
            userSettings.setCustomKeystorePath(customKeystorePathTextField.getText());
        });
    }

    private void initializeTokenSessionTimeoutTextField() {
        tokenSessionTimeoutTextField.setTextFormatter(new TextFormatter <> (change -> change.getControlNewText().matches("[0-9]*") ? change : null));
        tokenSessionTimeoutTextField.setText(String.valueOf(userSettings.getTokenSessionTimeout()));
        tokenSessionTimeoutTextField.setOnKeyTyped((e) -> {
            if (!tokenSessionTimeoutTextField.getText().isEmpty())
                userSettings.setTokenSessionTimeout(Long.parseLong(tokenSessionTimeoutTextField.getText()));
        });
    }

    private void initializeCustomPKCS11DriverPathSettings() {
        customPKCS11DriverPathTextField.setText(userSettings.getCustomPKCS11DriverPath());
        customPKCS11DriverPathTextField.setOnKeyTyped((e) -> {
            userSettings.setCustomPKCS11DriverPath(customPKCS11DriverPathTextField.getText());
        });
    }

    private void initializeDriverSlot(){
        var driverDetector = new DefaultDriverDetector(userSettings);
        var drivers = driverDetector.getAvailableDrivers();
        if (drivers.isEmpty()) {
            Text info = new Text(i18n("settings.other.slotIndex.noSourceDetected.text"));
            info.getStyleClass().add("autogram-description");
            driverSlot.getChildren().add(info);
            driverSlot.getStyleClass().add("autogram-description");
        } else {
            for (TokenDriver tokenDriver : drivers) {
                Text driverName = new Text(tokenDriver.getName());
                driverName.getStyleClass().add("autogram-label");
                driverSlot.getChildren().add(driverName);

                final var DEFAULT_LABEL = i18n("settings.other.slotIndex.defaultSlot.label");
                ChoiceBox<String> slotIndex = new ChoiceBox<>();
                slotIndex.getItems().addAll(DEFAULT_LABEL, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15");
                slotIndex.setValue(userSettings.getDriverSlotIndex(tokenDriver.getShortname()) == -1 ? DEFAULT_LABEL : String.valueOf(userSettings.getDriverSlotIndex(tokenDriver.getShortname())));
                slotIndex.getSelectionModel().selectedItemProperty()
                        .addListener((observable, oldValue, newValue) -> {
                            if (newValue.equals(DEFAULT_LABEL))
                                newValue = "-1";

                            userSettings.setDriverSlotIndex(tokenDriver.getShortname(), Integer.parseInt(newValue));
                        });
                driverSlot.getChildren().add(slotIndex);
            }
        }
    }

    public void onSaveButtonAction() {

        userSettings.save();

        var stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    public void onResetButtonAction() {

        var controller = new SettingsResetDialogController();
        controller.setUserSettings(userSettings);
        controller.setResetButton(resetButton);

        var root = GUIUtils.loadFXML(controller, "settings-reset-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("settings.reset.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }
}
