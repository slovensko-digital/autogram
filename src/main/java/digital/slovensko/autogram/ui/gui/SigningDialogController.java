package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SigningDialogController {
    private SigningJob signingJob;
    private Autogram autogram;

    @FXML
    public Button mainButton;

    public SigningDialogController(SigningJob signingJob, Autogram autogram) {
        this.signingJob = signingJob;
        this.autogram = autogram;
    }

    public void onMainButtonAction(ActionEvent event) {
        new Thread(() -> {
            autogram.sign(signingJob);
        }).start();
    }
}
