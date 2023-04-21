package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.DSSException;

import java.util.function.Consumer;

public class Autogram {
    private final UI ui;

    public Autogram(UI ui) {
        this.ui = ui;
    }

    public void sign(SigningJob job) {
        ui.onUIThreadDo(()
        -> optionallyMakeCompliantAndThen(job, (compliantJob)
        -> ui.startSigning(compliantJob, this)));
    }

    private void optionallyMakeCompliantAndThen(SigningJob job, Consumer<SigningJob> callback) {
        if (job.isCompliant()) {
            callback.accept(job);
            return;
        }

        ui.askIfShouldMakeCompliantAndThen(job, (answer)
        -> callback.accept(answer ? job.makeCompliant() : job));
    }

    public void sign(SigningJob job, SigningKey signingKey) {
        ui.onWorkThreadDo(() -> {
            try {
                job.signWithKeyAndRespond(signingKey);
                ui.onUIThreadDo(()
                -> ui.onSigningSuccess(job));
            } catch (DSSException e) {
                ui.onUIThreadDo(()
                -> ui.onSigningFailed(AutogramException.createFromDSSException(e)));
            } catch (Exception e) {
                ui.onUIThreadDo(()
                -> ui.onSigningFailed(new UnrecognizedException(e)));
            }
        });
    }

    public void pickSigningKeyAndThen(Consumer<SigningKey> callback) {
        var drivers = TokenDriver.getAvailableDrivers(); // TODO handle empty driver list with ui.showError?
        ui.pickTokenDriverAndThen(drivers, (driver)
        -> ui.requestPasswordAndThen(driver, (password)
        -> ui.onWorkThreadDo(()
        -> fetchKeysAndThen(driver, password, (key)
        -> callback.accept(key)))));
    }

    private void fetchKeysAndThen(TokenDriver driver, char[] password, Consumer<SigningKey> callback) {
        try {
            var token = driver.createTokenWithPassword(password);
            var keys = token.getKeys();
            ui.onUIThreadDo(()
            -> ui.pickKeyAndThen(keys, (privateKey)
            -> callback.accept(new SigningKey(token, privateKey))));
        } catch (DSSException e) {
            ui.onUIThreadDo(()
            -> ui.onPickSigningKeyFailed(AutogramException.createFromDSSException(e)));
        }
    }
}
