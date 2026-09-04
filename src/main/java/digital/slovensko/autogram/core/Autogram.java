package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.visualization.DocumentVisualizationBuilder;
import digital.slovensko.autogram.core.visualization.UnsupportedVisualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.CertificatesResponder;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.Logging;
import digital.slovensko.autogram.util.PDFUtils;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;
    private final UserSettings settings;
    /** Current batch, should be null if no batch was started yet */
    private Batch batch = null;
    private final PasswordManager passwordManager;
    private Timer tokenSessionTimer = null;

    public Autogram(UI ui, UserSettings settings) {
        this.ui = ui;
        this.settings = settings;
        this.passwordManager = new PasswordManager(ui);
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
                var error = new AutogramException("LOCKED_PDF");
                notifyBatchJobFailure(job, error);
                ui.onUIThreadDo(() -> {
                    ui.showError(error);
                });
                return;
            }

            try {
                var visualization = DocumentVisualizationBuilder.fromJob(job, settings);
                ui.onUIThreadDo(() -> ui.showVisualization(visualization, this));
            } catch (AutogramException e) {
                notifyBatchJobFailure(job, e);
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
            resetTokenSessionTimer();

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
            } catch (AutogramException e) {
                handleSigningFailure(job, e);
            } catch (Exception e) {
                handleSigningFailure(job, new UnrecognizedException(e));
            }
        });
    }

    private void handleSigningFailure(SigningJob job, AutogramException error) {
        if (job.isBatch()) {
            onSigningFailed(error, job);
            job.onDocumentSignFailed(error);
        } else if (error instanceof ResponseNetworkErrorException) {
            onSigningFailed(error, job);
        } else {
            onSigningFailed(error);
        }
    }

    private void notifyBatchJobFailure(SigningJob job, AutogramException error) {
        if (job.isBatch())
            job.onDocumentSignFailed(error);
    }

    /**
     * Starts a batch using the standard key-selection flow.
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param responder              - callback for handling batch responses
     */
    public void batchStart(int totalNumberOfDocuments, BatchResponder responder) {
        var newBatch = createBatch(totalNumberOfDocuments);
        startBatch(newBatch, responder);
    }

    public void batchStartWithModeSelection(int totalNumberOfDocuments, BatchResponder allAtOnceResponder,
            BatchResponder oneByOneResponder) {
        var newBatch = createBatch(totalNumberOfDocuments);
        ui.onUIThreadDo(() -> ui.selectBatchMode(newBatch, this, allAtOnceResponder, oneByOneResponder));
    }

    public void startBatch(Batch batch, BatchResponder responder) {
        ensureCurrentBatch(batch);
        var callback = new BatchStartCallback(batch, responder);
        ui.onUIThreadDo(() -> ui.startBatch(batch, this, callback));
    }

    public void startOneByOneBatch(Batch batch, BatchResponder responder) {
        ensureCurrentBatch(batch);
        try {
            batch.startOneByOne();
            responder.onBatchStartSuccess(batch);
        } catch (Exception e) {
            batch.end();
            passwordManager.reset();
            responder.onBatchStartFailure(toAutogramException(e));
        }
    }

    private Batch createBatch(int totalNumberOfDocuments) {
        if (batch != null && !batch.isEnded())
            throw new BatchConflictException();

        batch = new Batch(totalNumberOfDocuments);
        return batch;
    }

    private void ensureCurrentBatch(Batch batch) {
        if (this.batch != batch || batch.isEnded())
            throw new BatchConflictException();
    }

    private AutogramException toAutogramException(Exception error) {
        if (error instanceof AutogramException autogramException)
            return autogramException;

        return new AutogramException("BATCH_START_FAILED", error, error);
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
                AutogramException autogramException = new AutogramException("SIGNING_FAILED", e);
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
                },
                null
        );
    }

    private void fetchKeysAndThen(TokenDriver driver, Consumer<SigningKey> callback) {
        try {
            var token = driver.createToken(passwordManager, settings);
            var keys = token.getKeys();
            resetTokenSessionTimer();

            ui.onUIThreadDo(
                    () -> ui.pickKeyAndThen(keys, driver, (privateKey) -> callback.accept(new SigningKey(token, privateKey))));
        } catch (DSSException e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(AutogramException.createFromDSSException(e)));
        } catch (AutogramException e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(e));
        } catch (Exception e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(new UnrecognizedException(e)));
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
        if (batch != null)
            batch.end();
        passwordManager.reset();
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

    public void updateSignatureValidatorLotl(List<String> tlCountries) {
        ui.onWorkThreadDo(() -> SignatureValidator.getInstance().updateLotl(tlCountries));
    }

    public TSPSource getTspSource() {
        return settings.getTspSource();
    }

    public List<TokenDriver> getAvailableDrivers() {
        return settings.getDriverDetector().getAvailableDrivers();
    }

    public boolean isPlainXmlEnabled() {
        return settings.isPlainXmlEnabled();
    }

    private void stopTokenSessionTimer() {
        if (tokenSessionTimer == null)
            return;

        tokenSessionTimer.cancel();
    }

    private void startTokenSessionTimer() {
        var timerTask = new TimerTask() {
            @Override
            public void run() {
                ui.resetSigningKey();
            }
        };
        tokenSessionTimer = new Timer();
        tokenSessionTimer.schedule(timerTask, settings.getTokenSessionTimeout() * 60 * 1000);
    }

    private void resetTokenSessionTimer() {
        stopTokenSessionTimer();
        startTokenSessionTimer();
    }

    public void shutdown() {
        stopTokenSessionTimer();
    }

    public void consentCertificateReadingAndThen(CertificatesResponder responder, List<String> drivers) {
        var availableDrivers = settings.getDriverDetector().getAvailableDrivers();
        if (drivers != null && !drivers.isEmpty())
            availableDrivers = availableDrivers.stream().filter(driver -> drivers.contains(driver.getShortname())).toList();

        if (availableDrivers.isEmpty()) {
            responder.onError(new NoDriversDetectedException());
            return;
        }

        final var finalAvailableDrivers = availableDrivers;

        ui.onUIThreadDo(
            () -> {
                ui.consentCertificateReadingAndThen(
                    (consentDialogCloseCallback) -> {
                        ui.onWorkThreadDo(() -> {
                                getCertificates(responder, finalAvailableDrivers, consentDialogCloseCallback);
                            }
                        );
                    },
                    () -> {
                        responder.onError(new CertificatesReadingConsentRejectedException());
                    }
                );
            }
        );
    }

    public void getCertificates(CertificatesResponder responder, List<TokenDriver> drivers, Runnable consentDialogCloseCallback) {
        final var availableDrivers = drivers;
        ui.onUIThreadDo(() -> {
            ui.pickTokenDriverAndThen(availableDrivers,
                (driver) -> {
                    ui.onWorkThreadDo(() -> {
                        try (var token = driver.createToken(passwordManager, settings)) {
                            var keys = token.getKeys();
                            resetTokenSessionTimer();
                            ui.onUIThreadDo(consentDialogCloseCallback);
                            responder.onSuccess(keys.stream().map(key -> key.getCertificate().getCertificate()).toList());
                        } catch (DSSException e) {
                            var autogramException = AutogramException.createFromDSSException(e);
                            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(autogramException));
                            ui.onUIThreadDo(consentDialogCloseCallback);
                            responder.onError(autogramException);
                        } catch (AutogramException e) {
                            ui.onUIThreadDo(consentDialogCloseCallback);
                            responder.onError(e);
                        } catch (Exception e) {
                            ui.onUIThreadDo(consentDialogCloseCallback);
                            responder.onError(new UnrecognizedException(e));
                        }
                    });
                },
                () -> {
                    consentDialogCloseCallback.run();
                    responder.onError(new SigningCanceledByUserException());
                }
            );
        });
    }
}
