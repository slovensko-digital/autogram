package digital.slovensko.autogram.ui.gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class SettingsDialogController {

    @FXML
    private ChoiceBox<String> signatureTypeBox;

    @FXML
    private ChoiceBox<String> driverBox;

    @FXML
    private CheckBox checkBox;

    @FXML
    HBox radios;

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

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("Option enabled");
            } else {
                System.out.println("Option disabled");
            }
        });

        var togetherRadioButton = new RadioButton("Spoločne");
        radios.getChildren().add(togetherRadioButton);
        var individaullyRadioButton = new RadioButton("Samostatne");
        radios.getChildren().add(individaullyRadioButton);
    }
}
