package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchInProgressException;
import digital.slovensko.autogram.core.errors.CertificatesReadingConsentRejectedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.PdfPasswordProtectedException;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.TokenRemovedException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.drivers.CardReaders;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.drivers.TokenSlot;
import digital.slovensko.autogram.server.CertificatesResponder;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pdfa.PDFAStructureValidator;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.ALREADY_ENDED;

public class Autogram {
    private static final long TOKEN_SEARCH_TIMEOUT_MILLIS = 10_000;
    private static final long CARD_READERS_CHECK_TIMEOUT_MILLIS = 3_000;
    private static final String EID_EP_SLOT_LABEL = "Sig_EP";
    private static final long BATCH_EXPIRY_CHECK_MILLIS = 5_000;

    /**
     * Drivers left out of the search for tokens, they are offered every time (unless card readers don't respond) and
     * loaded only when the user picks them.
     * <p>
     * eObčanka (Czech eID) driver: we've seen it hang in C_Initialize, waiting for its own thread talking to readers.
     * SunPKCS11 initializes drivers one at a time (PKCS11.getInstance is synchronized), so a hanging driver blocks
     * every other PKCS#11 driver until Autogram is restarted, and the app can't quit since the driver's destructor
     * waits for the hanging thread. It also crashes the JVM on exit unless finalized. Searching it would expose all
     * users who just have it installed, while it's rarely used here.
     * <p>
     * MONET+ ProID+Q driver is searched, though initializing it after the user logged in to an eID card breaks eID
     * klient - every later login fails with CKR_FUNCTION_FAILED. It's initialized only once, in the first search with
     * a card inserted, which comes before any login, later searches just list its slots. Should it break eID klient
     * anyway, connecting to the eID card recovers, see PKCS11TokenDriver#recoverToken.
     */
    private static final Set<String> DRIVERS_SKIPPED_IN_TOKEN_SEARCH = Set.of(
            DefaultDriverDetector.TokenDriverShortnames.CZ_EID);

    private final UI ui;
    private final long tokenSearchTimeoutMillis;
    private final Supplier<CardReaders.State> cardReadersCheck;
    private final UserSettings settings;
    /** Current batch, or NoBatch before the first batch starts. Replaced only under the Autogram lock. */
    private volatile Batch batch = new NoBatch();
    /** Guarded by the Autogram lock. */
    private BatchResponder pendingBatchResponder;
    /** One signature at a time, so a mistyped cached PIN reaches the card only once. */
    private final Object signingLock = new Object();
    private final PasswordManager passwordManager;
    private Timer tokenSessionTimer = null;
    private Clock clock = Clock.systemUTC();
    /** True if a token was used without asking, and the user didn't get to its keys yet */
    private final AtomicBoolean automaticTokenPickUnfinished = new AtomicBoolean(false);
    /** True if the user asked for another certificate, the next token pick lets them choose */
    private final AtomicBoolean anotherTokenRequested = new AtomicBoolean(false);

    public Autogram(UI ui, UserSettings settings) {
        this(ui, settings, TOKEN_SEARCH_TIMEOUT_MILLIS, () -> CardReaders.check(CARD_READERS_CHECK_TIMEOUT_MILLIS));
    }

    Autogram(UI ui, UserSettings settings, long tokenSearchTimeoutMillis, Supplier<CardReaders.State> cardReadersCheck) {
        this.tokenSearchTimeoutMillis = tokenSearchTimeoutMillis;
        this.cardReadersCheck = cardReadersCheck;
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
            finishBatch(job.getBatch(), new SigningCanceledByUserException());
        }
        job.onJobCanceled();
    }

    public void skipCurrentDocument(SigningJob job) {
        try {
            job.onSkipCurrentDocument();
        } finally {
            ui.onUIThreadDo(() -> ui.closeSigningJob(job));
            updateBatch(job.getBatch());
        }
    }

    public void skipRemainingDocuments(SigningJob job) {
        finishBatch(job.getBatch(), new SigningCanceledByUserException());
        job.onJobSkipRemainingDocuments();
        ui.onUIThreadDo(() -> ui.closeSigningJob(job));
    }

    public void recordBatchSubmissionFailure(Batch submittedBatch) {
        if (batch != submittedBatch)
            return;

        submittedBatch.failure();
        updateBatch(submittedBatch);
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
                    ui.showError(new PdfPasswordProtectedException());
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
        try {
            signedDocument = signWithPINRetry(job, signingKey);
        } catch (AutogramException e) {
            onFailure.accept(e);
            return;
        } catch (Exception e) {
            onFailure.accept(new UnrecognizedException(e));
            return;
        }

        var delivered = job.onJobSigned(signedDocument);
        if (job.getBatch().shouldResetPasswordAfterSigning())
            passwordManager.reset();
        if (delivered)
            onSuccess.accept(job);
        else // closed by the user while signing, the signature is dropped
            ui.onUIThreadDo(ui::enableSigningOnAllJobs);
    }

    private SignedDocument signWithPINRetry(SigningJob job, SigningKey signingKey) {
        synchronized (signingLock) {
            while (true) {
                try {
                    return signWithKey(job, signingKey);
                } catch (PINIncorrectException e) {
                    // Retry only if Autogram supplied the PIN, so the retry asks the user again instead of looping.
                    if (!passwordManager.wasContextSpecificPasswordRequested())
                        throw e;
                    passwordManager.preparePINRetry();
                }
            }
        }
    }

    private SignedDocument signWithKey(SigningJob job, SigningKey signingKey) {
        // The batch may have ended while this document waited, e.g. for another one's PIN.
        if (job.isPartOfBatch() && job.getBatch().isEnded())
            throw new BatchEndedException(ALREADY_ENDED);

        var previousBatch = passwordManager.setBatchContext(job.getBatch());
        passwordManager.startSigningAttempt();
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
        // Documents outside a running batch wait until it ends; the UI keeps them disabled meanwhile.
        expireBatchIfNeeded();
        if (!job.isPartOfBatch() && hasActiveBatch()) {
            onSigningFailed(new BatchConflictException());
            return;
        }

        ui.onWorkThreadDo(() -> {
            try {
                signCommonAndThen(job, signingKey,
                        signedJob -> ui.onUIThreadDo(() -> ui.onSigningSuccess(signedJob)),
                        error -> handleSigningFailure(job, error));
            } catch (ResponseNetworkErrorException e) {
                onSigningFailed(e, job);
            } catch (Exception e) {
                // The result was already counted, e.g. the responder failed to save or send it.
                onSigningFailed(new UnrecognizedException(e), job);
            } finally {
                if (job.isPartOfBatch())
                    updateBatch(job.getBatch());
            }
        });
    }

    private void handleSigningFailure(SigningJob job, AutogramException error) {
        if (error instanceof TokenRemovedException && job.isPartOfBatch() && job.getBatch().isInteractive()) {
            // the user sees the document and can sign it once the card is back, with a new connection
            passwordManager.reset();
            ui.onUIThreadDo(() -> ui.onSigningFailed(error));
            return;
        }
        if (error.shouldReturnToSigning() && (!job.isPartOfBatch() || job.getBatch().isInteractive())) {
            passwordManager.reset();
            ui.onUIThreadDo(ui::enableSigningOnAllJobs);
            return;
        }
        if (error instanceof ResponseNetworkErrorException) {
            onSigningFailed(error, job);
            return;
        }
        if (!job.isPartOfBatch()) {
            onSigningFailed(error);
            return;
        }

        var delivered = error.batchCanContinue()
                ? job.onJobSignFailed(error)
                // other documents of the batch end with the same error, this one included
                : endBatch(job.getBatch(), error).contains(job) || job.onJobSignFailed(error);
        if (delivered)
            ui.onUIThreadDo(() -> ui.onSigningFailed(error, job));
        else
            ui.onUIThreadDo(ui::enableSigningOnAllJobs);
    }

    /**
     * Starts a batch - ask user - get signing key - start batch - return batch ID
     *
     * @param totalNumberOfDocuments - expected number of documents to be signed
     * @param responder              - callback for http response
     */
    public void startBatchSigning(int totalNumberOfDocuments, BatchResponder responder) {
        var selectedBatch = new SigningBatch(totalNumberOfDocuments, clock);
        expireBatchIfNeeded();
        synchronized (this) {
            batch.ensureCanStartNewBatch();
            batch = selectedBatch;
            pendingBatchResponder = responder;
        }
        ui.onUIThreadDo(() -> ui.selectBatchMode(selectedBatch, mode -> {
            synchronized (this) {
                if (pendingBatchResponder != responder || batch != selectedBatch || selectedBatch.isEnded()) return;
                selectedBatch.setMode(mode);
                if (selectedBatch.isInteractive()) {
                    pendingBatchResponder = null;
                    selectedBatch.start(null);
                }
            }
            if (selectedBatch.isInteractive()) {
                watchBatchExpiry(selectedBatch);
                notifyBatchStarted(selectedBatch, responder);
            } else {
                ui.startBatch(selectedBatch, this);
            }
        }, () -> cancelBatch(selectedBatch)));
    }

    /** Hands out the responder waiting for the start of this batch at most once, so only one answer is sent. */
    private synchronized BatchResponder takePendingBatchResponder(Batch batch) {
        if (this.batch != batch)
            return null;

        var responder = pendingBatchResponder;
        pendingBatchResponder = null;
        return responder;
    }

    public void signBatchWithKey(Batch batch, SigningKey key) {
        var responder = takePendingBatchResponder(batch);
        if (responder == null)
            return;

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

        watchBatchExpiry(batch);
        notifyBatchStarted(batch, responder);
    }

    /** A failure there would leave the batch running with nobody to use it, so it ends the batch. */
    private void notifyBatchStarted(Batch batch, BatchResponder responder) {
        try {
            responder.onBatchStartSuccess(batch);
        } catch (Exception e) {
            var error = e instanceof AutogramException autogramException ? autogramException
                    : new AutogramException("BATCH_START_FAILED", e, e);
            finishBatch(batch, error);
            onSigningFailed(error);
        }
    }

    public void cancelBatch(Batch batch) {
        if (this.batch != batch || batch.isEnded())
            return;

        var responder = takePendingBatchResponder(batch);
        finishBatch(batch, new SigningCanceledByUserException());
        if (responder != null)
            responder.onBatchStartFailure(new BatchCanceledException());
    }

    /** From the start of a batch until it ends, including mode and key selection. */
    private boolean hasActiveBatch() {
        var currentBatch = batch;
        return currentBatch.isPresent() && !currentBatch.isEnded();
    }

    public void finishBatch(Batch batch) {
        finishBatch(batch, new BatchEndedException(ALREADY_ENDED));
    }

    /**
     * Ends the batch. Its documents without a result yet get this one and their windows close, so nothing more is
     * signed in it.
     */
    public void finishBatch(Batch batch, AutogramException resultOfUnfinishedDocuments) {
        endBatch(batch, resultOfUnfinishedDocuments);
    }

    /** @return documents that got the result from here */
    private List<SigningJob> endBatch(Batch batch, AutogramException resultOfUnfinishedDocuments) {
        List<SigningJob> unfinished;
        synchronized (this) {
            if (this.batch != batch || batch.isEnded())
                return List.of();

            unfinished = resultOfUnfinishedDocuments instanceof BatchExpiredException ? batch.expire() : batch.end();
        }

        var delivered = new ArrayList<SigningJob>();
        for (var job : unfinished) {
            try {
                if (job.onJobSignFailed(resultOfUnfinishedDocuments))
                    delivered.add(job);
            } catch (Exception e) {
                // counted already, e.g. the client went away; the rest still gets its result
                delivered.add(job);
                Logging.log("Unable to deliver the end of batch for " + job.getName() + ": " + e);
            }
            ui.onUIThreadDo(() -> ui.closeSigningJob(job));
        }
        passwordManager.reset();
        ui.onUIThreadDo(ui::closeBatch);
        return delivered;
    }

    /** Ends the current batch when it's expired, e.g. the client went away. */
    void expireBatchIfNeeded() {
        var currentBatch = batch;
        if (currentBatch.isExpired())
            finishBatch(currentBatch, new BatchExpiredException());
    }

    /** Ends the batch once it expires, so its window closes and other signing isn't blocked. */
    private void watchBatchExpiry(Batch watchedBatch) {
        var timer = new Timer("batch-expiry", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (batch != watchedBatch || watchedBatch.isEnded()) {
                    timer.cancel();
                    return;
                }
                expireBatchIfNeeded();
            }
        }, BATCH_EXPIRY_CHECK_MILLIS, BATCH_EXPIRY_CHECK_MILLIS);
    }

    void setClock(Clock clock) {
        this.clock = clock;
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
        var currentBatch = batch;
        if (currentBatch.isPresent() && job.getBatch() != currentBatch)
            throw new BatchConflictException();
        try {
            currentBatch.addJob(batchId, job);
        } catch (BatchExpiredException e) {
            finishBatch(currentBatch, e);
            throw e;
        }
        if (job.getBatch().isInteractive()) {
            startSigning(job);
            return;
        }
        ui.onWorkThreadDo(() -> {
            try {
                signCommonAndThen(job, job.getBatch().getSigningKey(), signedJob -> {}, error -> {
                    // e.g. a locked PIN, a removed card or the PIN dialog closed by the user: the batch ends before
                    // the client learns about it, and the rest gets the same error
                    if (error.batchCanContinue() || !endBatch(job.getBatch(), error).contains(job))
                        job.onJobSignFailed(error);
                    // the batch window closes, the user should learn why (a removed card also drops the key)
                    if (!error.batchCanContinue() && !(error instanceof PasswordNotProvidedException))
                        onSigningFailed(error);
                });
            } finally {
                // Runs even when the responder fails, so a fully processed batch always ends.
                updateBatch(job.getBatch());
                ui.onUIThreadDo(ui::updateBatch);
            }
        });
    }

    /**
     * End the batch
     *
     * @param batchId - current batch ID, used to authenticate the request
     */
    public boolean endBatchSigning(String batchId) {
        var currentBatch = batch;
        // also a batch that ended on its own (all documents processed, expired), so the client learns how it went
        currentBatch.validateId(batchId);
        finishBatch(currentBatch);
        return currentBatch.isAllProcessed();
    }

    public Batch getBatch(String batchId) {
        var currentBatch = batch;
        currentBatch.validate(batchId);
        return currentBatch;
    }

    public void pickSigningKeyAndThen(Consumer<SigningKey> callback) {
        pickSigningKeyAndThen(null, callback);
    }

    public void pickSigningKeyAndThen(SigningJob job, Consumer<SigningKey> callback) {
        var drivers = settings.getDriverDetector().getAvailableDrivers();
        pickTokenAndThen(drivers, (option) -> fetchKeysAndThen(option, (key) -> {
            onTokenUsed(option);
            callback.accept(key);
        }, job), null);
    }

    /**
     * The next token pick lets the user choose, even if the only card would be used without asking - e.g. they want to
     * sign with another certificate, which may be in a keystore file. With nothing else to choose, the card is used.
     */
    public void requestAnotherToken() {
        anotherTokenRequested.set(true);
    }

    /**
     * Finds tokens (cards) of all drivers and lets the user pick one. The only card found is used without asking,
     * unless the user didn't finish with it last time (e.g. canceled entering PIN) or asked for another certificate
     * and there is something else to pick - then they get to choose. Callback is called on work thread.
     */
    private void pickTokenAndThen(List<TokenDriver> drivers, Consumer<TokenOption> callback, Runnable onCancel) {
        ui.onUIThreadDo(ui::onTokenSearchStarted);
        ui.onWorkThreadDo(() -> {
            var options = findTokenOptions(drivers);
            var automaticOption = options.getAutomaticOption();
            var previousUnfinished = automaticTokenPickUnfinished.getAndSet(false);
            var anotherRequested = anotherTokenRequested.getAndSet(false) && options.found().size() > 1;
            if (automaticOption.isPresent() && !previousUnfinished && !anotherRequested) {
                automaticTokenPickUnfinished.set(true);
                callback.accept(automaticOption.get());
                return;
            }

            ui.onUIThreadDo(() -> ui.pickTokenAndThen(options,
                    () -> findTokenOptions(drivers),
                    (option) -> ui.onWorkThreadDo(() -> callback.accept(option)),
                    onCancel));
        });
    }

    private TokenOptions findTokenOptions(List<TokenDriver> drivers) {
        var searchesCards = drivers.stream()
                .filter((driver) -> !DRIVERS_SKIPPED_IN_TOKEN_SEARCH.contains(driver.getShortname()))
                .anyMatch(TokenDriver::needsInsertedCard);
        var cardReaders = searchesCards ? cardReadersCheck.get() : CardReaders.State.UNKNOWN;
        if (cardReaders == CardReaders.State.NOT_RESPONDING) {
            // card drivers would hang in PC/SC as well, and might hang the app on exit - don't load any
            Logging.log("Card readers not responding, tokens are not searched");
            return notRespondingOptions(drivers);
        }

        var found = new ArrayList<TokenOption>();
        var otherDrivers = new ArrayList<TokenDriver>();
        var unavailableDrivers = new ArrayList<TokenDriver>();
        var searchedDrivers = new ArrayList<TokenDriver>();
        for (var driver : drivers) {
            if (DRIVERS_SKIPPED_IN_TOKEN_SEARCH.contains(driver.getShortname()))
                otherDrivers.add(driver);
            // nothing for card drivers to find, no need to load them
            else if (cardReaders == CardReaders.State.NO_CARD && driver.needsInsertedCard())
                unavailableDrivers.add(driver);
            else
                searchedDrivers.add(driver);
        }

        // drivers are asked in parallel, so that a hanging one (e.g. eID klient waiting for a card) doesn't hold up others
        var executor = Executors.newCachedThreadPool((runnable) -> {
            var thread = new Thread(runnable, "token-search");
            thread.setDaemon(true);
            return thread;
        });

        try {
            var futures = searchedDrivers.stream().map((driver) -> executor.submit(() -> driver.getSlotsWithToken(settings))).toList();
            var deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(tokenSearchTimeoutMillis);
            var notResponding = false;

            for (int i = 0; i < searchedDrivers.size(); i++) {
                var driver = searchedDrivers.get(i);
                Optional<List<TokenSlot>> slots;
                try {
                    slots = futures.get(i).get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                } catch (TimeoutException e) {
                    // it may be stuck in PC/SC and would hang when used too, the user can search again later
                    Logging.log("Search for tokens timed out on " + driver.getName());
                    notResponding = true;
                    unavailableDrivers.add(driver);
                    continue;
                } catch (ExecutionException e) {
                    Logging.log("Unable to find tokens of " + driver.getName() + ": " + e);
                    otherDrivers.add(driver);
                    continue;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    otherDrivers.add(driver);
                    continue;
                }

                if (slots.isEmpty()) {
                    found.add(new TokenOption(driver, null));
                    continue;
                }

                var visibleSlots = slots.get().stream()
                        .filter((slot) -> settings.isEidEpSlotsEnabled() || !isEidEpSlot(driver, slot))
                        .toList();
                if (visibleSlots.isEmpty())
                    unavailableDrivers.add(driver);
                else
                    visibleSlots.forEach((slot) -> found.add(new TokenOption(driver, slot)));
            }

            otherDrivers.sort(Comparator.comparingInt(drivers::indexOf));
            unavailableDrivers.sort(Comparator.comparingInt(drivers::indexOf));
            return new TokenOptions(found, otherDrivers, unavailableDrivers, notResponding);
        } finally {
            executor.shutdownNow();
        }
    }

    // drivers without cards don't need PC/SC, the user can still use them - not card drivers, not even eObčanka that
    // isn't searched, it would hang in PC/SC
    private static TokenOptions notRespondingOptions(List<TokenDriver> drivers) {
        var found = drivers.stream().filter(Predicate.not(TokenDriver::needsInsertedCard))
                .map((driver) -> new TokenOption(driver, null)).toList();
        var unavailableDrivers = drivers.stream().filter(TokenDriver::needsInsertedCard).toList();
        return new TokenOptions(found, List.of(), unavailableDrivers, true);
    }

    private void onTokenUsed(TokenOption option) {
        automaticTokenPickUnfinished.set(false);
        settings.setLastUsedToken(LastUsedToken.of(option));
    }

    // eID card has two tokens, Sig_ZEP with the qualified certificate and Sig_EP which is practically never used
    private static boolean isEidEpSlot(TokenDriver driver, TokenSlot slot) {
        return driver.getShortname().equals(DefaultDriverDetector.TokenDriverShortnames.EID)
                && slot.label().equalsIgnoreCase(EID_EP_SLOT_LABEL);
    }

    private void fetchKeysAndThen(TokenOption option, Consumer<SigningKey> callback, SigningJob job) {
        var driver = option.driver();
        try {
            var connected = connectToToken(option);
            var token = connected.token();
            var keys = connected.keys();
            resetTokenSessionTimer();

            Consumer<DSSPrivateKeyEntry> onPicked = privateKey -> {
                // A cached PIN belongs to the previous key, possibly another card.
                passwordManager.reset();
                callback.accept(new SigningKey(token, privateKey));
            };
            Runnable onCancel = () -> ui.onWorkThreadDo(token::close);
            ui.onUIThreadDo(() -> {
                if (job == null)
                    ui.pickKeyAndThen(keys, driver, onPicked, onCancel);
                else
                    ui.pickKeyAndThen(keys, driver, onPicked, onCancel, job);
            });
        } catch (DSSException e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(AutogramException.createFromDSSException(e)));
        } catch (AutogramException e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(e));
        } catch (Exception e) {
            ui.onUIThreadDo(() -> ui.onPickSigningKeyFailed(new UnrecognizedException(e)));
        }
    }

    private record ConnectedToken(AbstractKeyStoreTokenConnection token, List<DSSPrivateKeyEntry> keys) implements AutoCloseable {
        @Override
        public void close() {
            token.close();
        }
    }

    /**
     * Connects to the token and reads its keys. If the driver fails in a way it can recover from, connects once again.
     */
    private ConnectedToken connectToToken(TokenOption option) {
        var driver = option.driver();
        var token = driver.createToken(passwordManager, settings, option.slot());
        try {
            return new ConnectedToken(token, token.getKeys());
        } catch (DSSException e) {
            // before recovering, the driver may not handle the connection afterwards
            token.close();
            var recovered = driver.recoverToken(passwordManager, settings, option.slot(), e);
            if (recovered.isEmpty())
                throw e;

            Logging.log("Connecting again to " + driver.getName() + " after: " + e);
            return new ConnectedToken(recovered.get(), recovered.get().getKeys());
        }
    }

    public void checkForUpdate() {
        ui.onWorkThreadDo(() -> {
            if (!Updater.newVersionAvailable())
                return;
            ui.onUIThreadDo(ui::onUpdateAvailable);
        });
    }

    public void checkForLegacyInstallation() {
        ui.onWorkThreadDo(() -> {
            var installations = LegacyInstallation.findWhenRunningFromMsix();
            if (installations.isEmpty())
                return;
            ui.onUIThreadDo(() -> ui.onLegacyInstallationFound(installations));
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
        SignatureValidator.getInstance().reloadStarting();
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
                // Not resetting the PIN here, it may be in use right now; picking a key again resets it.
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
        // reading certificates connects to the card again, closing that connection logs the batch key out
        expireBatchIfNeeded();
        if (hasActiveBatch()) {
            responder.onError(new BatchInProgressException());
            return;
        }

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
        Runnable onCancel = () -> {
            consentDialogCloseCallback.run();
            responder.onError(new SigningCanceledByUserException());
        };

        pickTokenAndThen(drivers, (option) -> getCertificatesFromToken(responder, option, consentDialogCloseCallback), onCancel);
    }

    private void getCertificatesFromToken(CertificatesResponder responder, TokenOption option, Runnable consentDialogCloseCallback) {
        // a batch may have started while the user was consenting or picking the card
        if (hasActiveBatch()) {
            ui.onUIThreadDo(consentDialogCloseCallback);
            responder.onError(new BatchInProgressException());
            return;
        }

        try (var connected = connectToToken(option)) {
            var keys = connected.keys();
            resetTokenSessionTimer();
            onTokenUsed(option);
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
    }
}
