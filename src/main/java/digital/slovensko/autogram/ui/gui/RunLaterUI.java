package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.PasswordLambda;
import digital.slovensko.autogram.core.PrivateKeyLambda;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TokenDriverLambda;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;

import java.util.List;

public class RunLaterUI implements UI {
    private final GUI gui;

    public RunLaterUI(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {

    }

    @Override
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, TokenDriverLambda callback) {

    }

    @Override
    public void showSigningDialog(SigningJob job) {
        Platform.runLater(() -> {
            gui.showSigningDialog(job);
        });
    }

    @Override
    public void hideSigningDialog(SigningJob job) {
        Platform.runLater(() -> {
            gui.hideSigningDialog(job);
        });
    }

    @Override
    public void refreshSigningKey() {
        throw new RuntimeException();
    }

    @Override
    public void showError(AutogramException e) {
        throw new RuntimeException();
    }

    @Override
    public void showPasswordDialogAndThen(TokenDriver driver, PasswordLambda callback) {
        throw new RuntimeException();
    }

    @Override
    public void showPickFileDialog() {
        throw new RuntimeException();
    }
}
