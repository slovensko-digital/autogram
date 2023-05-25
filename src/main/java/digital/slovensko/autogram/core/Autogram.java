package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;

import java.io.File;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;

    public Autogram(UI ui) {
        this.ui = ui;
    }

    public void sign(SigningJob job) {
        ui.onUIThreadDo(()
        -> ui.startSigning(job, this));

        if (job.shouldCheckPDFCompliance()) {
            ui.onWorkThreadDo(()
            -> checkPDFACompliance(job));
        }
    }

    private void checkPDFACompliance(SigningJob job) {
        var result = new PDFAStructureValidator().validate(job.getDocument());
        if(!result.isCompliant()) {
            ui.onUIThreadDo(() -> ui.onPDFAComplianceCheckFailed(job));
        }
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
            } catch (IllegalArgumentException e) {
                ui.onUIThreadDo(()
                        -> ui.onSigningFailed(AutogramException.createFromIllegalArgumentException(e)));
            } catch (Exception e) {
                ui.onUIThreadDo(()
                -> ui.onSigningFailed(new UnrecognizedException(e)));
            }
        });
    }

    public void pickSigningKeyAndThen(Consumer<SigningKey> callback) {
        var drivers = TokenDriver.getAvailableDrivers();
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

    public void checkForUpdate() {
        ui.onWorkThreadDo(() -> {
            if (!Updater.newVersionAvailable())
                return;

            ui.onUIThreadDo(()
            -> ui.onUpdateAvailable());
        });
    }

    public void onAboutInfo() {
        ui.onAboutInfo();
    }

    public void onDocumentSaved(File targetFile) {
        ui.onUIThreadDo(()
        -> ui.onDocumentSaved(targetFile));
    }
}
