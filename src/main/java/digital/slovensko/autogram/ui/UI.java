package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;

public interface UI {
    void start(Autogram autogram, String[] args);

    void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback);

    void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback);

    void showSigningDialog(SigningJob job, Autogram autogram);

    void hideSigningDialog(SigningJob job, Autogram autogram);

    void refreshSigningKey();

    void showError(AutogramException e);
}
