package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.util.DSSUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SigningDialogController {
    private SigningJob signingJob;
    private Autogram autogram;

    @FXML
    public Button mainButton;

    public SigningDialogController(SigningJob signingJob, Autogram autogram) {
        this.signingJob = signingJob;
        this.autogram = autogram;
    }

    public void initialize() {
        refreshSigningKey();
    }

    public void onMainButtonPressed(ActionEvent event) {
        if(autogram.getActiveSigningKey() == null) {
            new Thread(() -> {
                autogram.pickSigningKey();
            }).start();
        } else {
            new Thread(() -> {
                autogram.sign(signingJob);
            }).start();
        }
    }

    public void refreshSigningKey() {
        if(autogram.getActiveSigningKey() == null) {
            mainButton.setText("Načítať certifikáty");
        } else {
            mainButton.setText("Podpísať ako " + DSSUtils.parseCN(autogram.getActiveSigningKey().getCertificate().getSubject().getRFC2253()));
        }
    }

    public void hide() {
        var window = mainButton.getScene().getRoot().getScene().getWindow();
        if(window instanceof Stage) {
            ((Stage) window).close();
        }
    }

}
