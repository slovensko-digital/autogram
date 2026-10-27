package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.core.errors.BatchTooManyDocumentsException;
import digital.slovensko.autogram.util.Logging;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.ALREADY_ENDED;
import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.CANNOT_RESTART;
import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.NOT_STARTED;

enum BatchState {
    INITIALIZED, STARTED, ENDED
}

/**
 * A session for signing multiple documents with one key or interactively.
 *
 * This class is used for checking runtime conditions and tracking progress.
 * Documents of one batch can be signed and counted from several threads (HTTP, FX and workers).
 */
public class SigningBatch implements Batch {
    /** Bulk: the client sends the first document right after it gets the batch ID. */
    static final Duration BULK_FIRST_DOCUMENT_TIMEOUT = Duration.ofMinutes(5);
    /** Bulk: nobody looks at the documents, so a long pause between them means the client is gone. */
    static final Duration BULK_IDLE_TIMEOUT = Duration.ofMinutes(1);
    /** Interactive: a client that went away (e.g. a closed browser tab) must not block Autogram forever. */
    static final Duration INTERACTIVE_IDLE_TIMEOUT = Duration.ofMinutes(10);

    private final String batchId = generateNewBatchId();
    private final int totalNumberOfDocuments;
    private final Clock clock;
    private final boolean local;
    private SignatureChoice signatureChoice;

    private BatchState state = BatchState.INITIALIZED;
    /** Ended because it expired, so the client keeps getting BATCH_EXPIRED. */
    private boolean expired = false;
    private SigningKey signingKey = null;
    private SigningMode mode = SigningMode.BULK;

    /** Documents added to the batch that didn't get a result yet. */
    private final Set<SigningJob> pendingJobs = new LinkedHashSet<>();
    /** Start of the batch or the last added or finished document, null before start. */
    private Instant lastActivity;
    private int addedDocumentsCount = 0;
    private int successfulDocumentsCount = 0;
    private int failedDocumentsCount = 0;

    public SigningBatch(int totalNumberOfDocuments) {
        this(totalNumberOfDocuments, Clock.systemUTC());
    }

    SigningBatch(int totalNumberOfDocuments, Clock clock) {
        this(totalNumberOfDocuments, clock, false);
    }

    SigningBatch(int totalNumberOfDocuments, Clock clock, boolean local) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        this.clock = clock;
        this.local = local;
    }

    @Override
    public synchronized boolean canSelectSignatureGoal() {
        return local && !isInteractive();
    }

    @Override
    public synchronized void setSignatureChoice(SignatureChoice choice) {
        if (!canSelectSignatureGoal() || state != BatchState.INITIALIZED)
            throw new IllegalStateException("The signature goal must be selected before a local bulk operation starts");
        signatureChoice = Objects.requireNonNull(choice);
    }

    @Override
    public synchronized SignatureChoice getSignatureChoice() {
        return signatureChoice;
    }

    public synchronized void start(SigningKey key) {
        if (state != BatchState.INITIALIZED)
            throw new BatchEndedException(CANNOT_RESTART);
        if (canSelectSignatureGoal() && signatureChoice == null)
            throw new IllegalStateException("Select a signature goal before starting a local bulk operation");
        state = BatchState.STARTED;
        signingKey = key;
        lastActivity = clock.instant();
    }

    public synchronized void setMode(SigningMode mode) {
        if (state != BatchState.INITIALIZED)
            throw new IllegalStateException("Signing mode must be selected before batch start");
        this.mode = mode;
    }

    public synchronized boolean isInteractive() {
        return mode == SigningMode.INTERACTIVE;
    }

    public boolean isPresent() {
        return true;
    }

    public synchronized void ensureCanStartNewBatch() {
        if (!isEnded())
            throw new BatchConflictException();
    }

    public synchronized boolean shouldResetPasswordAfterSigning() {
        return isEnded() || isAllProcessed();
    }

    public synchronized void addJob(String batchId, SigningJob job) {
        validate(batchId);
        if (this.totalNumberOfDocuments <= this.addedDocumentsCount)
            throw new BatchTooManyDocumentsException();

        addedDocumentsCount++;
        pendingJobs.add(job);
        lastActivity = clock.instant();
    }

    public synchronized void success(SigningJob job) {
        successfulDocumentsCount++;
        jobFinished(job);
        Logging.log("Batch " + batchId + " success");
        log();
    }

    public synchronized void failure(SigningJob job) {
        failedDocumentsCount++;
        jobFinished(job);
        Logging.log("Batch " + batchId + " failed");
        log();
    }

    public synchronized void failure() {
        failure(null);
    }

    private void jobFinished(SigningJob job) {
        if (job != null)
            pendingJobs.remove(job);
        if (lastActivity != null)
            lastActivity = clock.instant();
    }

    public synchronized List<SigningJob> end() {
        return end(false);
    }

    public synchronized List<SigningJob> expire() {
        return end(true);
    }

    private List<SigningJob> end(boolean expired) {
        if (state != BatchState.ENDED)
            this.expired = expired;
        state = BatchState.ENDED;
        var unfinished = new ArrayList<>(pendingJobs);
        pendingJobs.clear();
        return unfinished;
    }

    private void validateInternal() {
        if (state == BatchState.INITIALIZED)
            throw new BatchEndedException(NOT_STARTED);

        if (state == BatchState.ENDED && expired)
            throw new BatchExpiredException();

        if (state == BatchState.ENDED)
            throw new BatchEndedException(ALREADY_ENDED);

        if (isExpired()) {
            throw new BatchExpiredException();
        }
    }

    public synchronized void validate(String batchId) {
        validateInternal();

        if (!this.batchId.equals(batchId)) throw new BatchInvalidIdException();
    }

    public synchronized void validateId(String batchId) {
        // the ID of a batch that hasn't started wasn't given to anyone yet
        if (!this.batchId.equals(batchId)) throw new BatchInvalidIdException();

        if (state == BatchState.INITIALIZED)
            throw new BatchEndedException(NOT_STARTED);
    }

    // public getters

    public synchronized String getBatchId() {
        validate(batchId);

        return batchId;
    }

    public synchronized boolean isEnded() {
        return state == BatchState.ENDED;
    }

    public synchronized boolean isAllProcessed() {
        return getProcessedDocumentsCount() >= totalNumberOfDocuments;
    }

    public synchronized boolean isKeyChangeAllowed() {
        return state == BatchState.INITIALIZED;
    }

    public int getTotalNumberOfDocuments() {
        return totalNumberOfDocuments;
    }

    public synchronized int getProcessedDocumentsCount(){
        return successfulDocumentsCount + failedDocumentsCount;
    }

    public synchronized SigningKey getSigningKey() {
        return signingKey;
    }

    /**
     * A started batch expires after a pause with no document in progress, e.g. when the client went away. It never
     * expires while a document waits for the user (a signing window, a PIN dialog).
     */
    public synchronized boolean isExpired() {
        if (state != BatchState.STARTED || !pendingJobs.isEmpty())
            return false;

        return clock.instant().isAfter(lastActivity.plus(getIdleTimeout()));
    }

    private Duration getIdleTimeout() {
        if (isInteractive())
            return INTERACTIVE_IDLE_TIMEOUT;

        return addedDocumentsCount == 0 ? BULK_FIRST_DOCUMENT_TIMEOUT : BULK_IDLE_TIMEOUT;
    }

    // private
    private static String generateNewBatchId() {
        return UUID.randomUUID().toString();
    }

    public synchronized void log() {
        Logging.log("Batch " + batchId + " state: " + state + " processed: " + addedDocumentsCount + " total: " + totalNumberOfDocuments);
    }

}
