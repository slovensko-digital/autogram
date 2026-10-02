package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.CertificatesResponder;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;
    private final UserSettings settings;
    /** Current batch, {@link NoBatch} if no batch was started yet */
    private Batch batch = new NoBatch();
    private final PasswordManager passwordManager;
    /** Jobs waiting for the user to sign them, with the responder to deliver the outcome to */
    private final Map<SigningJob, SigningResponder> pendingSignings = new IdentityHashMap<>();
    private Timer tokenSessionTimer = null;

    public Autogram(UI ui, UserSettings settings) {
        this.ui = ui;
        this.settings = settings;
        this.passwordManager = new PasswordManager(ui);
    }

    public void startSigning(SigningJob job, SigningResponder responder) {
        pendingSignings.put(job, responder);
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

        var documentsToCheck = job.getDocuments().stream().filter(d -> d.isPDF()).toList();
        ui.onWorkThreadDo(() -> {
            for (var document : documentsToCheck) {
                var result = new PDFAStructureValidator().validate(document.toDssDocument());
                if (!result.isCompliant()) {
                    ui.onUIThreadDo(() -> ui.onPDFAComplianceCheckFailed(job));
                    return;
                }
            }
        });
    }

    public void startVisualization(SigningJob job) {
        ui.onWorkThreadDo(() -> {
            if (job.getDocuments().stream().anyMatch(d -> d.isPDFAndPasswordProtected())) {
                var e = new AutogramException("LOCKED_PDF");
                onVisualizationFailed(e, job);
                ui.onUIThreadDo(() -> {
                    ui.showError(e);
                });
                return;
            }

            try {
                job.initializeVisualizations();
                ui.onUIThreadDo(() -> ui.showSigningJob(job, this));

            } catch (FailedVisualizationException e) {
                Runnable onContinue = () -> ui.showSigningJob(job, this);
                Runnable onCancel = () -> cancel(job);

                if (settings.isCorrectDocumentDisplay()) {
                    ui.onUIThreadDo(
                            () -> ui.showIgnorableExceptionDialog(new FailedVisualizationException(e, job, onContinue, onCancel)));
                } else {
                    ui.onUIThreadDo(onContinue);
                }

            } catch (AutogramException e) {
                onVisualizationFailed(e, job);
                ui.onUIThreadDo(() -> ui.showError(e));
            }
        });
    }

    private void onVisualizationFailed(AutogramException e, SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        batch.onJobFailure();
        responder.onDocumentFailed(e);
    }

    private SignedDocument signCommon(SigningJob job, SigningKey signingKey) {
        try {
            var signedDocument = signWithKeyRetryingPIN(job, signingKey);
            resetTokenSessionTimer();

            if (!batch.isActive() || batch.isAllProcessed())
                passwordManager.reset();

            return signedDocument;
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

    /** Signs the job, asking for the PIN again (with the error shown) while it is rejected. */
    private SignedDocument signWithKeyRetryingPIN(SigningJob job, SigningKey signingKey) throws Exception {
        Callable<SignedDocument> signing = () -> job.signWithKey(signingKey, settings.getTspSource());
        while (true) {
            try {
                if (job.isPartOfBatch())
                    return passwordManager.withCachedPIN(job.getBatch(), signing);

                return passwordManager.withoutCachedPIN(signing);
            } catch (PINIncorrectException e) {
                passwordManager.onContextSpecificPasswordRejected(e);
            }
        }
    }

    public void sign(SigningJob job, SigningKey signingKey) {
        var responder = pendingSignings.get(job);
        if (responder == null)
            throw new IllegalStateException("Signing job was not submitted for interactive signing");

        ui.onWorkThreadDo(() -> {
            SignedDocument signedDocument;
            try {
                signedDocument = signCommon(job, signingKey);
            } catch (AutogramException e) {
                onSigningFailed(e, job, responder);
                return;
            }

            // The responder is called outside the signing try/catch: its failure is not a signing failure.
            pendingSignings.remove(job);
            passwordManager.clearContextSpecificPasswordError();
            batch.onJobSuccess();
            responder.onDocumentSigned(signedDocument);
            ui.onUIThreadDo(() -> ui.onSigningSuccess(job));
        });
    }

    private void onSigningFailed(AutogramException e, SigningJob job, SigningResponder responder) {
        pendingSignings.remove(job);
        passwordManager.clearContextSpecificPasswordError();
        batch.onJobFailure();
        if (job.isPartOfBatch() && !e.batchCanContinue())
            endActiveBatch();

        ui.onUIThreadDo(() -> ui.onSigningFailed(e, job));
        responder.onDocumentFailed(e);
    }

    /**
     * Starts a batch - ask user for signing mode - (get signing key) - start batch - return batch ID
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param responder              - callback for http response
     */
    public void startBatchWithModeSelection(int totalNumberOfDocuments, BatchResponder responder) {
        var newBatch = createBatch(totalNumberOfDocuments);
        var startBatchTask = new BatchStartCallback(newBatch, responder);

        ui.onUIThreadDo(() -> {
            ui.selectBatchMode(newBatch, mode -> {
                newBatch.setMode(mode);
                startBatch(newBatch, startBatchTask);
            }, startBatchTask::cancel);
        });
    }

    /**
     * Starts a batch in the given mode - (get signing key) - start batch - return batch ID
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param mode                   - bulk (one key for all) or interactive (per document)
     * @param responder              - callback for http response
     */
    public void startBatch(int totalNumberOfDocuments, SigningMode mode, BatchResponder responder) {
        var newBatch = createBatch(totalNumberOfDocuments);
        newBatch.setMode(mode);
        startBatch(newBatch, new BatchStartCallback(newBatch, responder));
    }

    private Batch createBatch(int totalNumberOfDocuments) {
        if (batch.isActive())
            throw new BatchConflictException();
        batch = new Batch(totalNumberOfDocuments);
        return batch;
    }

    private void startBatch(Batch batch, BatchStartCallback startBatchTask) {
        if (this.batch != batch || batch.isEnded())
            throw new BatchConflictException();

        if (batch.isInteractive()) {
            startBatchTask.accept(null);
            return;
        }

        ui.onUIThreadDo(() -> {
            ui.startBatch(batch, this, startBatchTask);
        });
    }

    /**
     * Sign a single document
     *
     * @param job
     * @param batchId   - current batch ID, used to authenticate the request
     * @param responder - callback for the result, in interactive mode called once the user signs the document
     */
    public void batchSign(SigningJob job, String batchId, SigningResponder responder) {
        batch.addJob(batchId);

        if (batch.isInteractive()) {
            startSigning(job, responder);
            return;
        }

        ui.onWorkThreadDo(() -> {
            SignedDocument signedDocument;
            try {
                signedDocument = signCommon(job, batch.getSigningKey());
            } catch (AutogramException e) {
                passwordManager.clearContextSpecificPasswordError();
                batch.onJobFailure();
                responder.onDocumentFailed(e);
                if (!e.batchCanContinue()) {
                    ui.onUIThreadDo(() -> {
                        ui.cancelBatch(batch);
                    });
                }
                ui.onUIThreadDo(() -> {
                    ui.updateBatch();
                });
                return;
            }

            passwordManager.clearContextSpecificPasswordError();
            batch.onJobSuccess();
            responder.onDocumentSigned(signedDocument);
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
    public boolean endBatch(String batchId) {
        if (batch.isEnded()) {
            if (!batch.hasBatchId(batchId))
                throw new BatchInvalidIdException();
            return false;
        }

        batch.validate(batchId);
        batch.end();
        passwordManager.reset();
        ui.onUIThreadDo(() -> {
            ui.cancelBatch(batch);
        });
        return batch.isAllProcessed();
    }

    /** Ends the batch from the GUI batch dialog. */
    public void endBatch(Batch batch) {
        if (this.batch == batch)
            batch.end();
        passwordManager.reset();
    }

    private void endActiveBatch() {
        batch.end();
        passwordManager.reset();
    }

    public Batch getBatch(String batchId) {
        batch.validate(batchId);
        return batch;
    }

    /** The user skipped the current document; the batch continues with the next one. */
    public void skipCurrent(SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        passwordManager.clearContextSpecificPasswordError();
        batch.onJobFailure();
        responder.onDocumentSkipped();
    }

    /** The user skipped the current and all remaining documents; the batch ends. */
    public void skipRemaining(SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        batch.onJobFailure();
        endActiveBatch();
        responder.onDocumentSkippedRemaining();
    }

    /** The user cancelled a submitted document; the whole batch is aborted. */
    public void cancel(SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        batch.onJobFailure();
        endActiveBatch();
        responder.onDocumentCanceled();
    }

    /** A batch document failed before it could be submitted for signing. */
    public void recordPreSubmissionFailure() {
        batch.onJobFailure();
    }

    /** The given number of batch documents will never be submitted for signing. */
    public void recordAborted(int count) {
        for (var i = 0; i < count; i++)
            batch.onJobFailure();
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
        endActiveBatch();
        ui.onUIThreadDo(() -> ui.onDocumentBatchSaved(result));
    }

    public void onSigningFailed(AutogramException e) {
        ui.onUIThreadDo(() -> ui.onSigningFailed(e));
    }

    public void clearContextSpecificPasswordError() {
        passwordManager.clearContextSpecificPasswordError();
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
