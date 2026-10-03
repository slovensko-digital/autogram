package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.CertificatesResponder;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;

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
    /** Current batch, or NoBatch before the first batch starts. */
    private Batch batch = new NoBatch();
    private BatchResponder pendingBatchResponder;
    private final PasswordManager passwordManager;
    private Timer tokenSessionTimer = null;

    public Autogram(UI ui, UserSettings settings) {
        this.ui = ui;
        this.settings = settings;
        this.passwordManager = new PasswordManager(ui, this.settings);
    }

    public void startSigning(SigningJob job) {
        ui.onUIThreadDo(()
        -> ui.startSigning(job, this));
    }

    public void cancelSigning(SigningJob job) {
        if (job.isPartOfBatch()) {
            finishBatch(job.getBatch());
        }
        job.onJobCanceled();
    }

    public void skipCurrentDocument(SigningJob job) {
        job.onSkipCurrentDocument();
        ui.onUIThreadDo(() -> ui.closeSigningJob(job));
        updateBatch(job.getBatch());
    }

    public void skipRemainingDocuments(SigningJob job) {
        finishBatch(job.getBatch());
        job.onJobSkipRemainingDocuments();
        ui.onUIThreadDo(() -> ui.closeSigningJob(job));
    }

    public void recordBatchSubmissionFailure(Batch submittedBatch) {
        if (batch == submittedBatch) submittedBatch.failure();
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
                ui.onUIThreadDo(() -> {
                    ui.showError(new AutogramException("LOCKED_PDF"));
                });
                return;
            }

            try {
                job.initializeVisualizations();
                ui.onUIThreadDo(() -> ui.showSigningJob(job, this));

            } catch (FailedVisualizationException e) {
                Runnable onContinue = () -> ui.showSigningJob(job, this);

                if (settings.isCorrectDocumentDisplay()) {
                    ui.onUIThreadDo(
                            () -> ui.showIgnorableExceptionDialog(new FailedVisualizationException(e, job, onContinue,
                                    () -> cancelSigning(job))));
                } else {
                    ui.onUIThreadDo(onContinue);
                }

            } catch (AutogramException e) {
                ui.onUIThreadDo(() -> ui.showError(e));
            }
        });
    }

    private void signCommonAndThen(SigningJob job, SigningKey signingKey,
            Consumer<SigningJob> onSuccess, Consumer<AutogramException> onFailure) {
        SignedDocument signedDocument;
        while (true) {
            try {
                signedDocument = signWithKey(job, signingKey);
                break;
            } catch (PINIncorrectException e) {
                passwordManager.preparePINRetry();
            } catch (AutogramException e) {
                onFailure.accept(e);
                return;
            } catch (Exception e) {
                onFailure.accept(new UnrecognizedException(e));
                return;
            }
        }

        job.onJobSigned(signedDocument);
        if (job.getBatch().shouldResetPasswordAfterSigning())
            passwordManager.reset();
        onSuccess.accept(job);
    }

    private SignedDocument signWithKey(SigningJob job, SigningKey signingKey) {
        var previousBatch = passwordManager.setBatchContext(job.getBatch());
        try {
            var signedDocument = job.signWithKey(signingKey, settings.getTspSource());
            resetTokenSessionTimer();
            if (job.getBatch().shouldResetPasswordAfterSigning())
                passwordManager.reset();
            return signedDocument;
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
        } finally {
            passwordManager.setBatchContext(previousBatch);
        }
    }

    public void sign(SigningJob job, SigningKey signingKey) {
        ui.onWorkThreadDo(() -> {
            try {
                signCommonAndThen(job, signingKey,
                        signedJob -> ui.onUIThreadDo(() -> {
                            ui.onSigningSuccess(signedJob);
                            if (signedJob.isPartOfBatch()) updateBatch(signedJob.getBatch());
                        }),
                        error -> handleSigningFailure(job, error));
            } catch (ResponseNetworkErrorException e) {
                onSigningFailed(e, job);
            } catch (Exception e) {
                onSigningFailed(new UnrecognizedException(e));
            }
        });
    }

    private void handleSigningFailure(SigningJob job, AutogramException error) {
        if (error instanceof ResponseNetworkErrorException) {
            onSigningFailed(error, job);
            return;
        }
        if (!job.isPartOfBatch()) {
            onSigningFailed(error);
            return;
        }

        if (!error.batchCanContinue()) finishBatch(job.getBatch());
        ui.onUIThreadDo(() -> ui.onSigningFailed(error, job));
        job.onJobSignFailed(error);
        updateBatch(job.getBatch());
    }

    /**
     * Starts a batch - ask user - get signing key - start batch - return batch ID
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param responder              - callback for http response
     */
    public void startBatchSigning(int totalNumberOfDocuments, BatchResponder responder) {
        batch.ensureCanStartNewBatch();
        var selectedBatch = new SigningBatch(totalNumberOfDocuments);
        batch = selectedBatch;
        pendingBatchResponder = responder;
        ui.onUIThreadDo(() -> ui.selectBatchMode(selectedBatch, mode -> {
            if (pendingBatchResponder != responder || batch != selectedBatch || selectedBatch.isEnded()) return;
            selectedBatch.setMode(mode);
            if (selectedBatch.isInteractive()) {
                pendingBatchResponder = null;
                selectedBatch.start(null);
                responder.onBatchStartSuccess(selectedBatch);
            } else {
                ui.startBatch(selectedBatch, this);
            }
        }, () -> cancelBatch(selectedBatch)));
    }

    public void signBatchWithKey(Batch batch, SigningKey key) {
        if (this.batch != batch || pendingBatchResponder == null)
            return;

        var responder = pendingBatchResponder;
        pendingBatchResponder = null;
        try {
            batch.start(key);
        } catch (Exception e) {
            finishBatch(batch);
            if (e instanceof AutogramException autogramException)
                responder.onBatchStartFailure(autogramException);
            else
                responder.onBatchStartFailure(new AutogramException("BATCH_START_FAILED", e, e));
            return;
        }

        responder.onBatchStartSuccess(batch);
    }

    public void cancelBatch(Batch batch) {
        if (this.batch != batch || batch.isEnded())
            return;

        var responder = pendingBatchResponder;
        pendingBatchResponder = null;
        finishBatch(batch);
        if (responder != null)
            responder.onBatchStartFailure(new BatchCanceledException());
    }

    public void finishBatch(Batch batch) {
        if (this.batch != batch || batch.isEnded())
            return;

        batch.end();
        passwordManager.reset();
        ui.onUIThreadDo(ui::closeBatch);
    }

    public void updateBatch(Batch batch) {
        if (this.batch != batch || batch.isEnded())
            return;

        batch.log();
        if (batch.isAllProcessed())
            finishBatch(batch);
    }

    /**
     * Sign a single document
     *
     * @param job
     * @param batchId - current batch ID, used to authenticate the request
     */
    public void batchSign(SigningInput input, Responder responder, String batchId) {
        batchSign(SigningJob.fromInput(input, responder, batch), batchId);
    }

    public void batchSign(SigningJob job, String batchId) {
        if (batch.isPresent() && job.getBatch() != batch)
            throw new BatchConflictException();
        batch.addJob(batchId);
        if (job.getBatch().isInteractive()) {
            startSigning(job);
            return;
        }
        ui.onWorkThreadDo(() -> {
            SignedDocument signedDocument;
            try {
                signedDocument = signWithKey(job, job.getBatch().getSigningKey());
            } catch (AutogramException e) {
                if (!e.batchCanContinue())
                    finishBatch(job.getBatch());
                job.onJobSignFailed(e);
                ui.onUIThreadDo(ui::updateBatch);
                return;
            }

            job.onJobSigned(signedDocument);
            if (job.getBatch().isAllProcessed())
                passwordManager.reset();
            ui.onUIThreadDo(ui::updateBatch);
        });
    }

    /**
     * End the batch
     *
     * @param batchId - current batch ID, used to authenticate the request
     */
    public boolean endBatchSigning(String batchId) {
        batch.validate(batchId);
        var currentBatch = batch;
        finishBatch(currentBatch);
        return currentBatch.isAllProcessed();
    }

    public Batch getBatch(String batchId) {
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
