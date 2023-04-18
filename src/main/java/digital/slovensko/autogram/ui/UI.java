package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;

public interface UI {
    void showPickFileDialog();

    void showSigningDialog(SigningJob job);

    void pickTokenDriverAndThen(List<TokenDriver> drivers, TokenDriverLambda callback);

    void showPasswordDialogAndThen(TokenDriver driver, PasswordLambda callback);

    void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback);

    void hideSigningDialog(SigningJob job);

    void refreshSigningKey();

    void showError(AutogramException e);
}
