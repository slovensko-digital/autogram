package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;
import digital.slovensko.autogram.core.errors.BatchInvalidIdException;
import digital.slovensko.autogram.core.errors.BatchConflictException;
import digital.slovensko.autogram.util.Logging;

import java.util.Date;
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
 */
public class SigningBatch implements Batch {
    private final String batchId = generateNewBatchId();
    private final int totalNumberOfDocuments;

    private BatchState state = BatchState.INITIALIZED;
    private SigningKey signingKey = null;
    private SigningMode mode = SigningMode.BULK;

    private Date expirationDate;
    private int addedDocumentsCount = 0;
    private int successfulDocumentsCount = 0;
    private int failedDocumentsCount = 0;

    public SigningBatch(int totalNumberOfDocuments) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        expirationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 minutes
    }

    public void start(SigningKey key) {
        if (state != BatchState.INITIALIZED)
            throw new BatchEndedException(CANNOT_RESTART);
        state = BatchState.STARTED;
        signingKey = key;
    }

    public void setMode(SigningMode mode) {
        if (state != BatchState.INITIALIZED)
            throw new IllegalStateException("Signing mode must be selected before batch start");
        this.mode = mode;
        if (isInteractive()) expirationDate = null;
    }

    public boolean isInteractive() {
        return mode == SigningMode.INTERACTIVE;
    }

    public boolean isPresent() {
        return true;
    }

    public void ensureCanStartNewBatch() {
        if (!isEnded())
            throw new BatchConflictException();
    }

    public boolean shouldResetPasswordAfterSigning() {
        return isEnded() || isAllProcessed();
    }

    public void addJob(String batchId) {
        validate(batchId);
        resetExpirationDate();

        if (this.totalNumberOfDocuments <= this.addedDocumentsCount)
            throw new IllegalAccessError("Sent more sign requests than declared at start");

        addedDocumentsCount++;
    }

    public void success() {
        successfulDocumentsCount++;
        Logging.log("Batch " + batchId + " success");
        log();
    }

    public void failure() {
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

    // public getters

    public String getBatchId() {
        validate(batchId);

        return batchId;
    }

    public boolean isEnded() {
        return state == BatchState.ENDED;
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

    // private
    private static String generateNewBatchId() {
        return UUID.randomUUID().toString();
    }

    boolean isExpired() {
        if (isInteractive()) return false;
        return expirationDate.before(new Date());
    }

    private void resetExpirationDate() {
        if (!isInteractive())
            expirationDate = new Date(System.currentTimeMillis() + 1000 * 60); // 1 minute
    }

    public void log() {
        Logging.log("Batch " + batchId + " state: " + state + " processed: " + addedDocumentsCount + " total: " + totalNumberOfDocuments);
    }

}
