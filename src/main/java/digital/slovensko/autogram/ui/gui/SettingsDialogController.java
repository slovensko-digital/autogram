package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.drivers.TokenDriver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class SettingsDialogController {

    @FXML
    private ChoiceBox<String> signatureTypeBox;

    @FXML
    private ChoiceBox<String> driverBox;

    @FXML
    private CheckBox checkBox;

    @FXML
    private CheckBox correctDocumentDisplaycheckBox;

    @FXML
    private CheckBox signatureValidationCheckBox;

    @FXML
    private CheckBox checkPDFAComplianceCheckBox;

    @FXML
    private CheckBox localServerEnabledCheckBox;

    @FXML
    private CheckBox belCheckBox;

    @FXML
    private CheckBox bulCheckBox;

    @FXML
    private CheckBox czeCheckBox;

    @FXML
    private CheckBox croCheckBox;

    @FXML
    private CheckBox cypCheckBox;

    @FXML
    private CheckBox denCheckBox;

    @FXML
    private CheckBox estCheckBox;

    @FXML
    private CheckBox finCheckBox;

    @FXML
    private CheckBox fraCheckBox;

    @FXML
    HBox radios;

    private ToggleGroup toggleGroup;


    @FXML
    private TableView tableView;


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
    }

    public void onSaveButtonAction() {
        System.out.println("Save");
    }

    public void onCancelButtonAction() {
        System.out.println("Cancel");
    }
}
