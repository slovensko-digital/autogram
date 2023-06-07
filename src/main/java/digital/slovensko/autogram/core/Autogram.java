package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.ServerBatchStartResponder;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;
import java.io.File;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;
    /** Current batch, should be null if no batch was started yet */
    private Batch batch = null;
    private final DriverDetector driverDetector;

    public Autogram(UI ui) {
        this(ui, new DefaultDriverDetector());
    }

    public Autogram(UI ui, DriverDetector driverDetector) {
        this.ui = ui;
        this.driverDetector = driverDetector;
    }

    public void sign(SigningJob job) {
        // TODO toto je divne kedze autogram->UI(thread)->Dialog->Autogram
        ui.onUIThreadDo(() -> ui.startSigning(job, this));

        if (job.shouldCheckPDFCompliance()) {
            ui.onWorkThreadDo(() -> checkPDFACompliance(job));
        }
    }

    private void checkPDFACompliance(SigningJob job) {
        var result = new PDFAStructureValidator().validate(job.getDocument());
        if (!result.isCompliant()) {
            ui.onUIThreadDo(() -> ui.onPDFAComplianceCheckFailed(job));
        }
    }

    public void sign(SigningJob job, SigningKey signingKey) {
        ui.onWorkThreadDo(() -> {
            try {
                job.signWithKeyAndRespond(signingKey);
                ui.onUIThreadDo(() -> ui.onSigningSuccess(job));
            } catch (DSSException e) {
                ui.onUIThreadDo(
                        () -> ui.onSigningFailed(AutogramException.createFromDSSException(e)));
            } catch (IllegalArgumentException e) {
                ui.onUIThreadDo(() -> ui
                        .onSigningFailed(AutogramException.createFromIllegalArgumentException(e)));
            } catch (Exception e) {
                ui.onUIThreadDo(() -> ui.onSigningFailed(new UnrecognizedException(e)));
            }
        });
    }

    /**
     * Starts a batch - ask user - get signing key - start batch - return batch ID
     * 
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param responder - callback for http response
     */
    public void batchStart(int totalNumberOfDocuments, ServerBatchStartResponder responder) {
        if (batch != null && !batch.isEnded())
            throw new BatchConflictException("Another batch is already running");
        batch = new Batch(totalNumberOfDocuments);


        var startBatchTask = new AutogramBatchStartCallback() {
            @Override
            protected Void call() throws Exception {
                // System.out.println("Starting batch on thread " +
                // Thread.currentThread().getName());
                batch.start();
                return null;
            }

            @Override
            protected void failed() {
                var e = getException();
                if (e instanceof AutogramException)
                    responder.onBatchStartFailure((AutogramException) e);
                else {
                    System.out.println("Batch start failed with exception: " + e);
                    responder.onBatchStartFailure(
                            new AutogramException("Unkown error occured while starting batch", "",
                                    "Batch start failed with exception: " + e, e));
                }
            }

            @Override
            protected void succeeded() {
                responder.onBatchStartSuccess(batch.getBatchId());
            }
        };

        ui.onUIThreadDo(() -> {
            ui.startBatch(batch, this, startBatchTask);
        });
    }

    /**
     * Sign a single document
     * 
     * @param job
     * @param batchId - current batch ID, used to authenticate the request
     */
    public void batchSign(SigningJob job, String batchId) {
        if (batch == null)
            throw new BatchNotStartedException("Batch not running");

        batch.addJob(batchId, job);
        ui.onWorkThreadDo(() -> {
            ui.signBatch(job);
        });
    }

    /**
     * End the batch
     * 
     * @param batchId - current batch ID, used to authenticate the request
     */
    public boolean batchEnd(String batchId) {
        batch.validate(batchId);
        batch.end();
        ui.onUIThreadDo(() -> {
            ui.cancelBatch(batch, null);
        });
        return batch.isAllProcessed();
    }

    public void pickSigningKeyAndThen(Consumer<SigningKey> callback) {
        var drivers = driverDetector.getAvailableDrivers();
        ui.pickTokenDriverAndThen(drivers,
                (driver) -> ui.requestPasswordAndThen(driver, (password) -> ui.onWorkThreadDo(
                        () -> fetchKeysAndThen(driver, password, (key) -> callback.accept(key)))));
    }

    private void fetchKeysAndThen(TokenDriver driver, char[] password,
            Consumer<SigningKey> callback) {
        try {
            var token = driver.createTokenWithPassword(password);
            var keys = token.getKeys();
            ui.onUIThreadDo(() -> ui.pickKeyAndThen(keys,
                    (privateKey) -> callback.accept(new SigningKey(token, privateKey))));
        } catch (DSSException e) {
            ui.onUIThreadDo(
                    () -> ui.onPickSigningKeyFailed(AutogramException.createFromDSSException(e)));
        }
    }

    public void checkForUpdate() {
        ui.onWorkThreadDo(() -> {
            if (!Updater.newVersionAvailable())
                return;

            ui.onUIThreadDo(() -> ui.onUpdateAvailable());
        });
    }

    public void onAboutInfo() {
        ui.onAboutInfo();
    }

    public void onDocumentSaved(File targetFile) {
        ui.onUIThreadDo(() -> ui.onDocumentSaved(targetFile));
    }
}
