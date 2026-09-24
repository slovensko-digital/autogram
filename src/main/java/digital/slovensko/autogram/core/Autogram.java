package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.core.errors.BatchNotStartedException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Autogram {
    private final UI ui;
    private final UserSettings settings;
    /** Current batch, should be null if no batch was started yet */
    private Batch batch = null;
    /** Mode of the current batch; only a command parameter, not stored in {@link Batch}. */
    private SigningMode batchMode = SigningMode.AUTOMATED;
    private final PasswordManager passwordManager;
    /** Interactive signings that were submitted and are waiting for a key. */
    private final Map<SigningJob, SigningResponder> pendingSignings = new IdentityHashMap<>();
    private Timer tokenSessionTimer = null;

    public Autogram(UI ui, UserSettings settings) {
        this.ui = ui;
        this.settings = settings;
        this.passwordManager = new PasswordManager(ui);
    }

    // ------------------------------------------------------------------
    // Commands
    // ------------------------------------------------------------------

    /**
     * Submits a single document for interactive signing and remembers its reply
     * channel. The signing itself happens later via {@link #sign(SigningJob, SigningKey)}.
     */
    public void submit(SigningJob job, SigningResponder responder) {
        pendingSignings.put(job, responder);
        ui.onUIThreadDo(() -> ui.startSigning(job, this));
    }

    /** Performs a previously submitted interactive signing. */
    public void sign(SigningJob job, SigningKey signingKey) {
        var responder = pendingSignings.get(job);
        if (responder == null)
            throw new IllegalStateException("Signing job was not submitted for interactive signing");

        ui.onWorkThreadDo(() -> performSigning(job, signingKey, responder));
    }

    /**
     * Submits a document that belongs to the active batch. In {@link SigningMode#INTERACTIVE}
     * the document is signed through the interactive flow; in {@link SigningMode#AUTOMATED}
     * it is signed right away with the batch key.
     */
    public void submitToBatch(SigningJob job, String batchId, SigningResponder responder) {
        if (batch == null)
            throw new BatchNotStartedException();

        batch.addJob(batchId);

        if (batchMode == SigningMode.INTERACTIVE) {
            pendingSignings.put(job, responder);
            ui.onUIThreadDo(() -> ui.startSigning(job, this));
            return;
        }

        ui.onWorkThreadDo(() -> {
            SignedDocument signedDocument = null;
            AutogramException failure = null;
            while (true) {
                try {
                    signedDocument = signWithKey(job, batch.getSigningKey());
                    break;
                } catch (AutogramException e) {
                    // A retryable failure (e.g. a wrong PIN) must not kill the batch: the failed
                    // attempt already cleared the cached PIN, so retrying asks the user again.
                    if (e.isRetryable())
                        continue;

                    failure = e;
                    break;
                } catch (Exception e) {
                    failure = new AutogramException("SIGNING_FAILED", e);
                    break;
                }
            }

            // Deliver outside the signing try/catch: an exception thrown by the adapter's
            // responder is not a signing failure and must not be retried or re-counted.
            if (failure == null) {
                recordSuccess();
                responder.onDocumentSigned(signedDocument);
            } else {
                recordFailure();
                responder.onDocumentFailed(failure);
                if (!failure.batchCanContinue())
                    ui.onUIThreadDo(() -> ui.cancelBatch(batch));
            }

            ui.onUIThreadDo(ui::updateBatch);
        });
    }

    /**
     * Starts a batch in the given mode.
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param mode                   - automated (one key for all) or interactive (per document)
     * @param responder              - output port for batch and document events
     */
    public void startBatch(int totalNumberOfDocuments, SigningMode mode, SigningResponder responder) {
        var newBatch = createBatch(totalNumberOfDocuments);
        batchMode = mode;
        startBatch(newBatch, responder);
    }

    /**
     * Starts a batch after letting the frontend pick the mode. The batch is created
     * eagerly so that concurrent start requests are rejected before any dialog.
     */
    public void startBatchWithModeSelection(int totalNumberOfDocuments, SigningResponder responder) {
        var newBatch = createBatch(totalNumberOfDocuments);
        ui.onUIThreadDo(() -> ui.selectBatchMode(newBatch,
                mode -> {
                    batchMode = mode;
                    startBatch(newBatch, responder);
                },
                () -> {
                    newBatch.end();
                    passwordManager.reset();
                    responder.onBatchStartFailed(new BatchCanceledException());
                }));
    }

    private void startBatch(Batch batch, SigningResponder responder) {
        ensureCurrentBatch(batch);

        if (batchMode == SigningMode.INTERACTIVE) {
            try {
                batch.start(null);
                responder.onBatchStarted(batch, SigningMode.INTERACTIVE);
            } catch (Exception e) {
                batch.end();
                passwordManager.reset();
                responder.onBatchStartFailed(toAutogramException(e));
            }
            return;
        }

        var callback = new BatchStartCallback(batch, responder);
        ui.onUIThreadDo(() -> ui.startBatch(batch, this, callback));
    }

    /**
     * Ends the batch.
     *
     * @param batchId - current batch ID, used to authenticate the request
     */
    public boolean endBatch(String batchId) {
        if (batch == null)
            throw new BatchNotStartedException();

        if (batch.isEnded()) {
            if (!batch.hasBatchId(batchId))
                throw new BatchInvalidIdException();
            return false;
        }

        batch.validate(batchId);
        batch.end();
        passwordManager.reset();
        ui.onUIThreadDo(() -> ui.cancelBatch(batch));
        return batch.isAllProcessed();
    }

    /** The user skipped the current document; the batch continues with the next one. */
    public void skipCurrent(SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        recordFailure();
        responder.onDocumentSkipped();
    }

    /** The user skipped the current and all remaining documents; the batch ends. */
    public void skipRemaining(SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        recordFailure();
        endActiveBatch();
        responder.onDocumentSkippedRemaining();
    }

    /** The user cancelled a submitted document; the whole batch is aborted. */
    public void cancel(SigningJob job) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        recordFailure();
        endActiveBatch();
        responder.onDocumentCanceled();
    }

    /** Records documents that failed before they could be submitted to the active batch. */
    public void recordPreSubmissionFailure() {
        recordFailure();
    }

    /** Records documents that were aborted after a fatal batch failure. */
    public void recordAborted(int count) {
        for (var i = 0; i < count; i++)
            recordFailure();
    }

    /** Ends a batch from a UI action and clears any batch-scoped PIN cache. */
    public void endBatch(Batch batch) {
        if (this.batch == batch)
            batch.end();
        passwordManager.reset();
    }

    public Batch getBatch(String batchId) {
        if (batch == null) throw new BatchNotStartedException(); // TODO replace with checked exception
        batch.validate(batchId);
        return batch;
    }

    // ------------------------------------------------------------------
    // Signing
    // ------------------------------------------------------------------

    private void performSigning(SigningJob job, SigningKey signingKey, SigningResponder responder) {
        SignedDocument signedDocument;
        try {
            signedDocument = signWithKey(job, signingKey);
        } catch (AutogramException e) {
            handleSigningFailure(job, responder, e);
            return;
        } catch (Exception e) {
            handleSigningFailure(job, responder, new UnrecognizedException(e));
            return;
        }

        // Deliver outside the signing try/catch: an exception thrown by the adapter's
        // responder is not a signing failure and must not be re-counted.
        pendingSignings.remove(job);
        recordSuccess();
        responder.onDocumentSigned(signedDocument);
        ui.onUIThreadDo(() -> ui.onSigningSuccess(job));
    }

    private SignedDocument signWithKey(SigningJob job, SigningKey signingKey) {
        var signedDocumentRef = new AtomicReference<SignedDocument>();
        Runnable signing = () -> {
            try {
                signedDocumentRef.set(job.signWithKey(signingKey, settings.getTspSource()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UnrecognizedException(e);
            }
        };

        try {
            var jobBatch = job.getBatch();
            if (jobBatch != null)
                passwordManager.withCachedPIN(jobBatch, signing);
            else
                passwordManager.withoutCachedPIN(signing);

            resetTokenSessionTimer();

            if (batch == null || batch.isEnded() || batch.isAllProcessed())
                passwordManager.reset();
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

        return signedDocumentRef.get();
    }

    private void handleSigningFailure(SigningJob job, SigningResponder responder, AutogramException error) {
        // A pending job still has its dialog open, so the user may retry after a
        // retryable failure. A direct (non-pending) signing has no dialog to keep.
        var isPending = pendingSignings.get(job) == responder;

        if (isPending && error.isRetryable()) {
            // Keep the dialog open and the pending signing registered so the user can
            // retry the same document. Any cached PIN was already cleared.
            ui.onUIThreadDo(() -> ui.onSigningFailed(error));
            return;
        }

        if (error instanceof ResponseNetworkErrorException) {
            // The response channel itself failed; do not try to respond again.
            ui.onUIThreadDo(() -> ui.onSigningFailed(error, job));
            pendingSignings.remove(job);
            return;
        }

        pendingSignings.remove(job);
        recordFailure();
        ui.onUIThreadDo(() -> {
            if (isPending || job.isPartOfBatch())
                ui.onSigningFailed(error, job);
            else
                ui.onSigningFailed(error);
        });
        responder.onDocumentFailed(error);
    }

    private void recordSuccess() {
        if (batch != null)
            batch.onJobSuccess();
    }

    private void recordFailure() {
        if (batch != null)
            batch.onJobFailure();
    }

    private void endActiveBatch() {
        if (batch != null)
            batch.end();
        passwordManager.reset();
    }

    // ------------------------------------------------------------------
    // Batch lifecycle helpers
    // ------------------------------------------------------------------

    private Batch createBatch(int totalNumberOfDocuments) {
        ensureNoActiveBatch();

        batch = new Batch(totalNumberOfDocuments);
        return batch;
    }

    private void ensureNoActiveBatch() {
        if (batch != null && !batch.isEnded())
            throw new BatchConflictException();
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

    // ------------------------------------------------------------------
    // Visualization and validation
    // ------------------------------------------------------------------

    public void startVisualization(SigningJob job) {
        ui.onWorkThreadDo(() -> {
            if (job.getDocuments().stream().anyMatch(d -> d.isPDFAndPasswordProtected())) {
                var error = new AutogramException("LOCKED_PDF");
                notifyJobFailure(job, error);
                ui.onUIThreadDo(() -> ui.showError(error));
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
                            () -> ui.showIgnorableExceptionDialog(
                                    new FailedVisualizationException(e, job, onContinue, onCancel)));
                } else {
                    ui.onUIThreadDo(onContinue);
                }

            } catch (AutogramException e) {
                notifyJobFailure(job, e);
                ui.onUIThreadDo(() -> ui.showError(e));
            }
        });
    }

    private void notifyJobFailure(SigningJob job, AutogramException error) {
        var responder = pendingSignings.remove(job);
        if (responder == null)
            return;

        recordFailure();
        responder.onDocumentFailed(error);
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

    // ------------------------------------------------------------------
    // Keys, certificates, updates
    // ------------------------------------------------------------------

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
            // The token is intentionally kept open and handed over to the SigningKey.
            //noinspection resource
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
