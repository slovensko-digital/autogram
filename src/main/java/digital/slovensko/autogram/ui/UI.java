package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;
import java.util.function.Consumer;

public interface UI {
    void askIfShouldMakeCompliantAndThen(SigningJob job, Consumer<Boolean> callback);

    void startSigning(SigningJob job, Autogram autogram);

    void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback);

    void requestPasswordAndThen(TokenDriver driver, Consumer<char[]> callback);

    void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback);

    void onPickSigningKeyFailed(AutogramException e);

    void onSigningSuccess(SigningJob job);

    void onSigningFailed(AutogramException e);

    void onWorkThreadDo(Runnable callback);

    void onUIThreadDo(Runnable callback);
}
