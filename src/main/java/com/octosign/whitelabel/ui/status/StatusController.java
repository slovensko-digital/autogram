package com.octosign.whitelabel.ui.status;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for the status window
 */
public class StatusController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button exitButton;

    @FXML
    private void onExitButtonAction() {
        var stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

}
