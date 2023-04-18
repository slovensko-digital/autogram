package digital.slovensko.autogram;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.SigningKeyLambda;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.DSSException;

public class Autogram {
    private final UI ui;

    public Autogram(UI ui) {
        this.ui = ui;
    }

    public void sign(SigningJob job) {
        System.out.println("Starting signing for " + job);
        ui.onUIThreadDo(()
        -> ui.startSigning(job, this));
    }

    public void sign(SigningJob signingJob, SigningKey signingKey) {
        ui.onWorkThreadDo(() -> {
            try {
                signingJob.signWithKeyAndRespond(signingKey);
                ui.onUIThreadDo(()
                -> ui.onSigningSuccess(signingJob));
            } catch (DSSException e) {
                ui.onUIThreadDo(()
                -> ui.onSigningFailed(AutogramException.createFromDSSException(e)));
            } catch (Exception e) {
                ui.onUIThreadDo(()
                -> ui.onSigningFailed(new UnrecognizedException(e)));
            }
        });
    }

    public void pickSigningKeyAndThen(SigningKeyLambda callback) {
        var drivers = TokenDriver.getAvailableDrivers(); // TODO handle empty driver list with ui.showError?
        ui.pickTokenDriverAndThen(drivers, (driver)
        -> ui.requestPasswordAndThen(driver, password
        -> ui.onWorkThreadDo(()
        -> fetchKeysAndThen(driver, password, (key)
        -> callback.call(key)))));
    }

    private void fetchKeysAndThen(TokenDriver driver, char[] password, SigningKeyLambda callback) {
        try {
            var token = driver.createTokenWithPassword(password);
            var keys = token.getKeys();
            ui.onUIThreadDo(()
            -> ui.pickKeyAndThen(keys, (privateKey)
            -> callback.call(new SigningKey(token, privateKey))));
        } catch (DSSException e) {
            ui.onUIThreadDo(()
            -> ui.onPickSigningKeyFailed(AutogramException.createFromDSSException(e)));
        }
    }
}
