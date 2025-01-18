package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.*;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.core.visualization.UnsupportedVisualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.model.AutogramDocument;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.Logging;
import digital.slovensko.autogram.util.PDFUtils;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;
    private final UserSettings settings;
    /** Current batch, should be null if no batch was started yet */
    private Batch batch = null;
    private final PasswordManager passwordManager;

    public Autogram(UI ui, UserSettings settings) {
        this.ui = ui;
        this.settings = settings;
        this.passwordManager = new PasswordManager(ui, this.settings);
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
            // PDF/A doesn't support encryption
            if (job.getDocument().hasOpenDocumentPassword()) {
                ui.onUIThreadDo(() -> ui.onPDFAComplianceCheckFailed(job));
                return;
            }

            var result = new PDFAStructureValidator().validate(job.getDocument());
            if (!result.isCompliant()) {
                ui.onUIThreadDo(() -> ui.onPDFAComplianceCheckFailed(job));
            }
        });
    }

    public void handleProtectedPdfDocument(AutogramDocument document) {
        var protection = PDFUtils.determinePDFProtection(document);
        if (protection == PDFUtils.PDFProtection.NONE)
            return;

        var password = ui.getDocumentPassword(document);
        switch (protection) {
            case OPEN_DOCUMENT_PASSWORD -> document.setOpenDocumentPassword(password);
            case MASTER_PASSWORD -> document.setMasterPassword(password);
        }
    }

    public SigningJob buildSigningJobFromFile(File file, Responder responder, boolean checkPDFACompliance, SignatureLevel signatureType, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        var document = SigningJob.createDSSFileDocumentFromFile(file);
        handleProtectedPdfDocument(document);

        var parameters = SigningJob.getParametersForFile(document, checkPDFACompliance, signatureType, isEn319132, tspSource, plainXmlEnabled);
        return SigningJob.build(document, parameters, responder);
    }

    public void startVisualization(SigningJob job) {
        ui.onWorkThreadDo(() -> {
            try {
                var visualization = DocumentVisualizationBuilder.fromJob(job, settings);
                ui.onUIThreadDo(() -> ui.showVisualization(visualization, this));
            } catch (AutogramException e) {
                ui.onUIThreadDo(() -> ui.showError(e));
            } catch (Exception e) {
                Runnable onContinue = () -> ui.showVisualization(new UnsupportedVisualization(job), this);

                if (settings.isCorrectDocumentDisplay()) {
                    ui.onUIThreadDo(
                            () -> ui.showIgnorableExceptionDialog(new FailedVisualizationException(e, job, onContinue)));
                } else {
                    ui.onUIThreadDo(onContinue);
                }
            }
        });
    }

    private void signCommonAndThen(SigningJob job, SigningKey signingKey, Consumer<SigningJob> callback) {
        try {
            job.signWithKeyAndRespond(signingKey);
            if (batch == null || batch.isEnded() || batch.isAllProcessed())
                passwordManager.reset();

            callback.accept(job);
        } catch (PINIncorrectException e) {
            passwordManager.reset();
            throw e;
        } catch (AutogramException e) {
            throw e;
        } catch (DSSException e) {
            throw AutogramException.createFromDSSException(e);
        } catch (IllegalArgumentException e) {
            throw AutogramException.createFromIllegalArgumentException(e);
        } catch (Exception e) {
            throw new UnrecognizedException(e);
        }
    }

    public void sign(SigningJob job, SigningKey signingKey) {
        ui.onWorkThreadDo(() -> {
            try {
                signCommonAndThen(job, signingKey, (jobNew) -> {
                    ui.onUIThreadDo(() -> ui.onSigningSuccess(jobNew));
                });
            } catch (ResponseNetworkErrorException e) {
                onSigningFailed(e, job);
            } catch (AutogramException e) {
                onSigningFailed(e);
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
            try {
                signCommonAndThen(job, batch.getSigningKey(), (jobNew) -> {
                    Logging.log("GUI: Signing batch job: " + job.hashCode() + " file " + job.getDocument().getName());
                });
            } catch (AutogramException e) {
                job.onDocumentSignFailed(e);
                if (!e.batchCanContinue()) {
                    ui.onUIThreadDo(() -> {
                        ui.cancelBatch(batch);
                    });
                    throw e;
                }
            } catch (Exception e) {
                AutogramException autogramException = new AutogramException("Document signing has failed", "", "", e);
                job.onDocumentSignFailed(autogramException);
            }
            ui.onUIThreadDo(() -> {
                ui.updateBatch();
            });
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
        var drivers = settings.getDriverDetector().getAvailableDrivers();
        ui.pickTokenDriverAndThen(drivers,
                (driver) -> {
                    ui.onWorkThreadDo(() -> {
                        fetchKeysAndThen(driver, callback);
                    });
                }
        );
    }

    private void fetchKeysAndThen(TokenDriver driver, Consumer<SigningKey> callback) {
        try {
            var token = driver.createToken(passwordManager, settings);
            var keys = token.getKeys();

            ui.onUIThreadDo(
                    () -> ui.pickKeyAndThen(keys, driver, (privateKey) -> callback.accept(new SigningKey(token, privateKey))));
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

    public void onSigningFailed(AutogramException e, SigningJob job) {
        ui.onUIThreadDo(() -> ui.onSigningFailed(e, job));
    }

    public void onSigningFailed(AutogramException e) {
        ui.onUIThreadDo(() -> ui.onSigningFailed(e));
    }

    public void initializeSignatureValidator(ScheduledExecutorService scheduledExecutorService, ExecutorService cachedExecutorService, List<String> tlCountries) {
        ui.onWorkThreadDo(() -> {
            SignatureValidator.getInstance().initialize(cachedExecutorService, tlCountries);
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> SignatureValidator.getInstance().refresh(),
                480, 480, java.util.concurrent.TimeUnit.MINUTES);
    }

    public TSPSource getTspSource() {
        return settings.getTspSource();
    }

    public boolean isPlainXmlEnabled() {
        return settings.isPlainXmlEnabled();
    }
}
