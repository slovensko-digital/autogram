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

public class Autogram {
    private static final long TOKEN_SEARCH_TIMEOUT_MILLIS = 10_000;
    private static final long CARD_READERS_CHECK_TIMEOUT_MILLIS = 3_000;
    private static final String EID_EP_SLOT_LABEL = "Sig_EP";

    /**
     * Drivers left out of the search for tokens, they are offered only among other drivers and loaded only when the
     * user picks them.
     * <p>
     * eObčanka (Czech eID) driver: we've seen it hang in C_Initialize, waiting for its own thread talking to readers.
     * SunPKCS11 initializes drivers one at a time (PKCS11.getInstance is synchronized), so a hanging driver blocks
     * every other PKCS#11 driver until Autogram is restarted, and the app can't quit since the driver's destructor
     * waits for the hanging thread. It also crashes the JVM on exit unless finalized. Searching it would expose all
     * users who just have it installed, while it's rarely used here.
     * <p>
     * MONET+ ProID+Q driver: talking to cards of other drivers breaks eID klient once the user logged in to the eID
     * card - every later login fails with CKR_FUNCTION_FAILED until eID klient is initialized again. As the search runs
     * every time the user picks a certificate, they couldn't use their eID card again after signing with it.
     */
    private static final Set<String> DRIVERS_SKIPPED_IN_TOKEN_SEARCH = Set.of(
            DefaultDriverDetector.TokenDriverShortnames.CZ_EID,
            DefaultDriverDetector.TokenDriverShortnames.MONET);

    private final UI ui;
    private final long tokenSearchTimeoutMillis;
    private final Supplier<CardReaders.State> cardReadersCheck;
    private final UserSettings settings;
    /** Current batch, should be null if no batch was started yet */
    private Batch batch = null;
    private final PasswordManager passwordManager;
    private Timer tokenSessionTimer = null;
    /** True if a token was used without asking, and the user didn't get to its keys yet */
    private final AtomicBoolean automaticTokenPickUnfinished = new AtomicBoolean(false);

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
                            () -> ui.showIgnorableExceptionDialog(new FailedVisualizationException(e, job, onContinue)));
                } else {
                    ui.onUIThreadDo(onContinue);
                }

            } catch (AutogramException e) {
                ui.onUIThreadDo(() -> ui.showError(e));
            }
        });
    }

    private void signCommonAndThen(SigningJob job, SigningKey signingKey, Consumer<SigningJob> callback) {
        try {
            job.signWithKeyAndRespond(signingKey, settings.getTspSource());
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
            throw new BatchConflictException();
        batch = new Batch(totalNumberOfDocuments);

        var startBatchTask = new BatchStartCallback(batch, responder);

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
                    Logging.log("GUI: Signing batch job: " + job.hashCode() + " file " + job.getName());
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
        pickTokenAndThen(drivers, (option) -> fetchKeysAndThen(option, (key) -> {
            onTokenUsed(option);
            callback.accept(key);
        }), null);
    }

    /**
     * Finds tokens (cards) of all drivers and lets the user pick one. The only card found is used without asking,
     * unless the user didn't finish with it last time (e.g. canceled entering PIN) - then they get to choose.
     * Callback is called on work thread.
     */
    private void pickTokenAndThen(List<TokenDriver> drivers, Consumer<TokenOption> callback, Runnable onCancel) {
        ui.onUIThreadDo(ui::onTokenSearchStarted);
        ui.onWorkThreadDo(() -> {
            var options = findTokenOptions(drivers);
            var automaticOption = options.getAutomaticOption();
            var previousUnfinished = automaticTokenPickUnfinished.getAndSet(false);
            if (automaticOption.isPresent() && !previousUnfinished) {
                automaticTokenPickUnfinished.set(true);
                callback.accept(automaticOption.get());
                return;
            }

            ui.onUIThreadDo(() -> ui.pickTokenAndThen(options,
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
            return allDriversOptions(drivers);
        }

        // nothing for card drivers to find, no need to load them
        Predicate<TokenDriver> isSkipped = (driver) -> DRIVERS_SKIPPED_IN_TOKEN_SEARCH.contains(driver.getShortname())
                || (cardReaders == CardReaders.State.NO_CARD && driver.needsInsertedCard());

        // drivers are asked in parallel, so that a hanging one (e.g. eID klient waiting for a card) doesn't hold up others
        var executor = Executors.newCachedThreadPool((runnable) -> {
            var thread = new Thread(runnable, "token-search");
            thread.setDaemon(true);
            return thread;
        });

        try {
            var searchedDrivers = drivers.stream().filter(isSkipped.negate()).toList();
            var futures = searchedDrivers.stream().map((driver) -> executor.submit(() -> driver.getSlotsWithToken(settings))).toList();
            var deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(tokenSearchTimeoutMillis);
            var found = new ArrayList<TokenOption>();
            var otherDrivers = new ArrayList<TokenDriver>();

            for (int i = 0; i < searchedDrivers.size(); i++) {
                var driver = searchedDrivers.get(i);
                Optional<List<TokenSlot>> slots;
                try {
                    slots = futures.get(i).get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                } catch (TimeoutException e) {
                    // results would be incomplete, a hanging driver may also block others - offer all drivers instead
                    Logging.log("Search for tokens timed out on " + driver.getName());
                    return allDriversOptions(drivers);
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
                    otherDrivers.add(driver);
                else
                    visibleSlots.forEach((slot) -> found.add(new TokenOption(driver, slot)));
            }

            drivers.stream().filter(isSkipped).forEach(otherDrivers::add);
            otherDrivers.sort(Comparator.comparingInt(drivers::indexOf));
            return new TokenOptions(found, otherDrivers);
        } finally {
            executor.shutdownNow();
        }
    }

    // when tokens can't be searched, the user picks the driver as before
    private static TokenOptions allDriversOptions(List<TokenDriver> drivers) {
        return new TokenOptions(drivers.stream().map((driver) -> new TokenOption(driver, null)).toList(), List.of());
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

    private void fetchKeysAndThen(TokenOption option, Consumer<SigningKey> callback) {
        var driver = option.driver();
        try {
            var connected = connectToToken(option);
            var token = connected.token();
            var keys = connected.keys();
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
        Runnable onCancel = () -> {
            consentDialogCloseCallback.run();
            responder.onError(new SigningCanceledByUserException());
        };

        pickTokenAndThen(drivers, (option) -> getCertificatesFromToken(responder, option, consentDialogCloseCallback), onCancel);
    }

    private void getCertificatesFromToken(CertificatesResponder responder, TokenOption option, Runnable consentDialogCloseCallback) {
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
