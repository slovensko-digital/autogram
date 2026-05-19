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

/**
 * Batch is a session for signing multiple documents with the same key.
 * 
 * This class is used for checking runtime conditions and tracking progress.
 */
public class Batch {
    private final String batchId = generateNewBatchId();
    private final int totalNumberOfDocuments;

    private BatchState state = BatchState.INITIALIZED;
    private SigningKey signingKey = null;

    private Date expirationDate;
    private int addedDocumentsCount = 0;
    private int successfulDocumentsCount = 0;
    private int failedDocumentsCount = 0;

    public Batch(int totalNumberOfDocuments) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        expirationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 minutes
    }

    public void start(SigningKey key) {
        if (state != BatchState.INITIALIZED)
            throw new BatchEndedException(CANNOT_RESTART);
        state = BatchState.STARTED;
        signingKey = key;
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

    private boolean isExpired() {
        return expirationDate.before(new Date());
    }

    public void resetExpirationDate() {
        expirationDate = new Date(System.currentTimeMillis() + 1000 * 60); // 1 minute
    }

    public void log() {
        Logging.log("Batch " + batchId + " state: " + state + " processed: " + addedDocumentsCount + " total: " + totalNumberOfDocuments);
    }

}
