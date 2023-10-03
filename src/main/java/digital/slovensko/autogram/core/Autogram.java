package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.core.visualization.UnsupportedVisualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.PDFUtils;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;
    /** Current batch, should be null if no batch was started yet */
    private Batch batch = null;
    private final DriverDetector driverDetector;
    private final boolean shouldDisplayVisualizationError;
    private final Integer slotId;

    public Autogram(UI ui, boolean shouldDisplayVisualizationError) {
        this(ui, shouldDisplayVisualizationError, new DefaultDriverDetector(), -1);
    }

    public Autogram(UI ui, boolean shouldDisplayVisualizationError , DriverDetector driverDetector) {
        this(ui, shouldDisplayVisualizationError, driverDetector, -1);
    }

    public Autogram(UI ui, boolean shouldDisplayVisualizationError , Integer slotId) {
        this(ui, shouldDisplayVisualizationError, new DefaultDriverDetector(), slotId);
    }

    public Autogram(UI ui, boolean shouldDisplayVisualizationError , DriverDetector driverDetector, Integer slotId) {
        this.ui = ui;
        this.driverDetector = driverDetector;
        this.slotId = slotId;
        this.shouldDisplayVisualizationError = shouldDisplayVisualizationError;
    }

    public void sign(SigningJob job) {
        ui.onUIThreadDo(()
        -> ui.startSigning(job, this));
    }

    public void checkAndValidateSignatures(SigningJob job) {
        checkSignatures(job);

        var reports = SignatureValidator.getInstance().getSignatureValidationReport(job);
        if (!reports.haveSignatures())
            return;

        ui.onUIThreadDo(() -> ui.onSignatureValidationCompleted(reports));
    }

    private void checkSignatures(SigningJob job) {
        var reports = SignatureValidator.getSignatureCheckReport(job);
        ui.onUIThreadDo(() -> ui.onSignatureCheckCompleted(reports));
    }

    public void checkPDFACompliance(SigningJob job) {
        if (!job.shouldCheckPDFCompliance())
            return;

        ui.onWorkThreadDo(() -> {
            var result = new PDFAStructureValidator().validate(job.getDocument());
            if (!result.isCompliant()) {
                ui.onUIThreadDo(() -> ui.onPDFAComplianceCheckFailed(job));
            }
        });
    }

    public void startVisualization(SigningJob job) {
        ui.onWorkThreadDo(() -> {
            if (PDFUtils.isPdfAndPasswordProtected(job.getDocument())) {
                ui.onUIThreadDo(() -> {
                    ui.showError(new AutogramException("Nastala chyba", "Dokument je chránený heslom", "Snažíte sa podpísať dokument chránený heslom, čo je funkcionalita, ktorá nie je podporovaná.\n\nOdstráňte ochranu heslom a potom budete môcť dokument podpísať."));
                });
                return;
            }

            try {
                var visualization = DocumentVisualizationBuilder.fromJob(job);
                ui.onUIThreadDo(() -> ui.showVisualization(visualization, this));
            } catch (Exception e) {
                Runnable onContinue = () -> ui.showVisualization(new UnsupportedVisualization(job), this);

                if (shouldDisplayVisualizationError) {
                    ui.onUIThreadDo(
                            () -> ui.showIgnorableExceptionDialog(new FailedVisualizationException(e, job, onContinue)));
                } else {
                    ui.onUIThreadDo(onContinue);
                }
            }
        });
    }

    public void sign(SigningJob job, SigningKey signingKey) {
        ui.onWorkThreadDo(() -> {
            try {
                job.signWithKeyAndRespond(signingKey);
                ui.onUIThreadDo(() -> ui.onSigningSuccess(job));
            } catch (DSSException e) {
                onSigningFailed(AutogramException.createFromDSSException(e));
            } catch (IllegalArgumentException e) {
                onSigningFailed(AutogramException.createFromIllegalArgumentException(e));
            } catch (Exception e) {
                onSigningFailed(new UnrecognizedException(e));
            }
        });
    }

    /**
     * Starts a batch - ask user - get signing key - start batch - return batch ID
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param responder              - callback for http response
     */
    public void batchStart(int totalNumberOfDocuments, BatchResponder responder) {
        if (batch != null && !batch.isEnded())
            throw new BatchConflictException("Iné hromadné podpisovanie už prebieha");
        batch = new Batch(totalNumberOfDocuments);

        var startBatchTask = new AutogramBatchStartCallback(batch, responder);

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
        if (batch == null) throw new BatchNotStartedException(); // TODO replace with checked exception

        batch.addJob(batchId);

        ui.onWorkThreadDo(() -> {
            ui.signBatch(job, batch.getSigningKey());
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
            ui.cancelBatch(batch);
        });
        return batch.isAllProcessed();
    }

    public Batch getBatch(String batchId) {
        if (batch == null) throw new BatchNotStartedException(); // TODO replace with checked exception
        batch.validate(batchId);
        return batch;
    }

    public void pickSigningKeyAndThen(Consumer<SigningKey> callback) {
        var drivers = driverDetector.getAvailableDrivers();
        ui.pickTokenDriverAndThen(drivers,
                (driver) -> requestPasswordAndThen(driver, callback));
    }

    public void requestPasswordAndThen(TokenDriver driver, Consumer<SigningKey> callback) {
        ui.requestPasswordAndThen(driver, (password) -> ui.onWorkThreadDo(
                () -> fetchKeysAndThen(driver, password, callback)));
    }

    private void fetchKeysAndThen(TokenDriver driver, char[] password, Consumer<SigningKey> callback) {
        try {
            var token = driver.createTokenWithPassword(slotId, password);
            var keys = token.getKeys();

            ui.onUIThreadDo(
                    () -> ui.pickKeyAndThen(keys, (privateKey) -> callback.accept(new SigningKey(token, privateKey))));
        } catch (DSSException e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(AutogramException.createFromDSSException(e)));
        }
    }

    public void checkForUpdate() {
        ui.onWorkThreadDo(() -> {
            if (!Updater.newVersionAvailable())
                return;
            ui.onUIThreadDo(ui::onUpdateAvailable);
        });
    }

    public void onAboutInfo() {
        ui.onAboutInfo();
    }

    public void onDocumentSaved(File targetFile) {
        ui.onUIThreadDo(() -> ui.onDocumentSaved(targetFile));
    }

    public void onDocumentBatchSaved(BatchUiResult result) {
        ui.onUIThreadDo(() -> ui.onDocumentBatchSaved(result));
    }

    public void onSigningFailed(AutogramException e) {
        ui.onUIThreadDo(() -> ui.onSigningFailed(e));
    }

    public void initializeSignatureValidator(ScheduledExecutorService scheduledExecutorService, ExecutorService cachedExecutorService) {
        ui.onWorkThreadDo(() -> {
            SignatureValidator.getInstance().initialize(cachedExecutorService);
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> SignatureValidator.getInstance().refresh(),
            480, 480, java.util.concurrent.TimeUnit.MINUTES);
    }
}
