package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.util.Logging;

import java.util.Date;
import java.util.UUID;

import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.ALREADY_ENDED;
import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.CANNOT_RESTART;
import static digital.slovensko.autogram.core.errors.BatchEndedException.Error.NOT_STARTED;

enum BatchState {
    INITIALIZED, STARTED, ENDED
}

/** A signing session that tracks expected and completed documents. */
public class Batch {
    /** Interactive signing needs time for the user to review and authorize each document. */
    private static final long INTERACTIVE_DOCUMENT_TIMEOUT_MILLIS = 1000L * 60 * 5;
    private static final long AUTOMATED_DOCUMENT_TIMEOUT_MILLIS = 1000L * 60;
    private static final long INITIAL_TIMEOUT_MILLIS = 1000L * 60 * 5;

    private final String batchId = generateNewBatchId();
    private final int totalNumberOfDocuments;

    private BatchState state = BatchState.INITIALIZED;
    private SigningKey signingKey = null;
    private SigningMode mode = SigningMode.BULK;

    private Date expirationDate;
    private int addedDocumentsCount = 0;
    private int successfulDocumentsCount = 0;
    private int failedDocumentsCount = 0;

    public Batch(int totalNumberOfDocuments) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        expirationDate = new Date(System.currentTimeMillis() + INITIAL_TIMEOUT_MILLIS);
    }

    public void start(SigningKey key) {
        if (state != BatchState.INITIALIZED)
            throw new BatchEndedException(CANNOT_RESTART);
        state = BatchState.STARTED;
        signingKey = key;
    }

    /** The signing mode of this batch; must be set before it is started. */
    public void setMode(SigningMode mode) {
        if (state != BatchState.INITIALIZED)
            throw new IllegalStateException("Signing mode must be set before the batch is started");

        this.mode = mode;
    }

    public SigningMode getMode() {
        return mode;
    }

    public boolean isPresent() {
        return true;
    }

    public boolean hasMultipleDocuments() {
        return totalNumberOfDocuments > 1;
    }

    public boolean isInteractive() {
        return mode == SigningMode.INTERACTIVE;
    }

    public void addJob(String batchId) {
        validate(batchId);
        resetExpirationDate();

        if (this.totalNumberOfDocuments <= this.addedDocumentsCount)
            throw new IllegalAccessError("Sent more sign requests than declared at start");

        addedDocumentsCount++;
    }

    public void onJobSuccess() {
        successfulDocumentsCount++;
        Logging.log("Batch " + batchId + " success");
        log();
    }

    public void onJobFailure() {
        failedDocumentsCount++;
        Logging.log("Batch " + batchId + " failed");
        log();
    }

    public void end() {
        state = BatchState.ENDED;
    }

    private void validateInternal() {
        if (state == BatchState.INITIALIZED)
            throw new BatchEndedException(NOT_STARTED);

        if (state == BatchState.ENDED)
            throw new BatchEndedException(ALREADY_ENDED);

        if (isExpired()) {
            throw new BatchExpiredException();
        }
    }

    public void validate(String batchId) {
        validateInternal();

        if (!this.batchId.equals(batchId)) throw new BatchInvalidIdException();
    }

    public boolean hasBatchId(String batchId) {
        return this.batchId.equals(batchId);
    }

    public String getBatchId() {
        validate(batchId);

        return batchId;
    }

    /** Returns the batch id without validating the batch state (e.g. for cache lookups). */
    public String getId() {
        return batchId;
    }

    public boolean isEnded() {
        return state == BatchState.ENDED;
    }

    /** True while the batch may still accept documents (initialized or started). */
    public boolean isActive() {
        return state != BatchState.ENDED;
    }

    public boolean isAllProcessed() {
        return getProcessedDocumentsCount() >= totalNumberOfDocuments;
    }

    public boolean isKeyChangeAllowed() {
        return state == BatchState.INITIALIZED;
    }

    public int getTotalNumberOfDocuments() {
        return totalNumberOfDocuments;
    }

    public int getProcessedDocumentsCount(){
        return successfulDocumentsCount + failedDocumentsCount;
    }

    public SigningKey getSigningKey() {
        return signingKey;
    }

    private static String generateNewBatchId() {
        return UUID.randomUUID().toString();
    }

    private boolean isExpired() {
        return expirationDate.before(new Date());
    }

    public void resetExpirationDate() {
        expirationDate = new Date(System.currentTimeMillis() + documentTimeoutMillis());
    }

    long documentTimeoutMillis() {
        return isInteractive() ? INTERACTIVE_DOCUMENT_TIMEOUT_MILLIS : AUTOMATED_DOCUMENT_TIMEOUT_MILLIS;
    }

    public void log() {
        Logging.log("Batch " + batchId + " state: " + state + " processed: " + addedDocumentsCount + " total: " + totalNumberOfDocuments);
    }

}
