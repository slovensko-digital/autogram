package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.Autogram;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;

public interface UI {
    void startSigning(SigningJob job, Autogram autogram);

    void pickTokenDriverAndThen(List<TokenDriver> drivers, TokenDriverLambda callback);

    void requestPasswordAndThen(TokenDriver driver, PasswordLambda callback);

    void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback);

    void onPickSigningKeyFailed(AutogramException e);

    void onSigningSuccess(SigningJob job);

    void onSigningFailed(AutogramException e);

    void onWorkThreadDo(Runnable callback);

    void onUIThreadDo(Runnable callback);
}
