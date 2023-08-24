package digital.slovensko.autogram.ui.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

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


    public void initialize() {
        signatureTypeBox.getItems().addAll("ASIC XADES", "ASIC CADES", "PADES");
        signatureTypeBox.setValue("PADES");
        signatureTypeBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected: " + newValue);
        });


        driverBox.getItems().addAll("Občiansky preukaz (eID klient)", "I.CA SecureStore", "MONET+ ProID+Q", "Gemalto IDPrime 940", "Fake token driver");
        driverBox.setValue("Občiansky preukaz (eID klient)");
        driverBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected driver: " + newValue);
        });

        standardCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("Option enabled");
            } else {
                System.out.println("Option disabled");
            }
        });

        correctDocumentDisplaycheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                correctDocumentDisplaycheckBox.setText("Zapnutá");
            } else {
                correctDocumentDisplaycheckBox.setText("Vypnutá");
            }
        });

        signatureValidationCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                signatureValidationCheckBox.setText("Zapnutá");
            } else {
                signatureValidationCheckBox.setText("Vypnutá");
            }
        });

        checkPDFAComplianceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                checkPDFAComplianceCheckBox.setText("Zapnutá");
            } else {
                checkPDFAComplianceCheckBox.setText("Vypnutá");
            }
        });

        localServerEnabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                localServerEnabledCheckBox.setText("Zapnutý");
            } else {
                localServerEnabledCheckBox.setText("Vypnutý");
            }
        });


        toggleGroup = new ToggleGroup();

        var togetherRadioButton = new RadioButton("Spoločne");
        togetherRadioButton.setToggleGroup(toggleGroup);
        radios.getChildren().add(togetherRadioButton);
        var individaullyRadioButton = new RadioButton("Samostatne");
        individaullyRadioButton.setToggleGroup(toggleGroup);
        radios.getChildren().add(individaullyRadioButton);
//        radioButton.setUserData(driver);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected driver: " + newValue);
        });

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

            CheckBox checkBox = new CheckBox("Vypnuté");
            checkBox.getStyleClass().addAll("custom-checkbox");
            checkBoxBox.getChildren().add(checkBox);

            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    checkBox.setText("Zapnuté"); // Change text when selected
                } else {
                    checkBox.setText("Vypnuté"); // Change text when deselected
                }
            });

            hbox.getChildren().addAll(countryBox, checkBoxBox);
            countryList.getChildren().add(hbox);
        }
    }

    public void onSaveButtonAction() {
        System.out.println("Save");
    }

    public void onCancelButtonAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
