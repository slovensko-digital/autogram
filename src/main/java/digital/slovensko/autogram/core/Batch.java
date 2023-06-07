package digital.slovensko.autogram.core;

import java.util.Date;
import java.util.UUID;

import digital.slovensko.autogram.core.errors.BatchEndedException;
import digital.slovensko.autogram.core.errors.BatchExpiredException;

enum BatchState {
    INITIALIZED, STARTED, ENDED
}


/**
 * Batch is a session for signing multiple documents with the same key.
 * 
 * This class is used for checking runtime conditions and tracking progress. 
 */
public class Batch {
    private final String batchId = getNewBatchId();
    private final int totalNumberOfDocuments;

    private BatchState state = BatchState.INITIALIZED;

    private Date expriationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 10);
    private int processedDocumentsCount = 0;


    public Batch(int totalNumberOfDocuments) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        resetExpirationDate();
    }

    // methods
    public void start() {
        if (state != BatchState.INITIALIZED)
            throw new BatchEndedException("Starting existing batch is not allowed");
        state = BatchState.STARTED;
    }

    public void addJob(String batchId, SigningJob job) {
        validate(batchId);

        if (this.totalNumberOfDocuments <= this.processedDocumentsCount)
            throw new IllegalAccessError("Sent more sign requests than declared at start");

        processedDocumentsCount++;
    }

    public void end() {
        state = BatchState.ENDED;
    }

    public void validateInternal() {
        if (state == BatchState.INITIALIZED)
            throw new BatchEndedException("Batch is not started");

        if (state == BatchState.ENDED)
            throw new BatchEndedException("Batch session has ended");

        if (isExpired()) {
            throw new BatchExpiredException("Batch session has expired");
        }
    }

    public void validate(String batchId) {
        validateInternal();

        if (!this.batchId.equals(batchId))
            throw new IllegalArgumentException("Batch session ID does not match");

        // System.out.println("Batch session " + batchId + " is valid with current session ("
        // + this.batchId + ")");
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
        return processedDocumentsCount >= totalNumberOfDocuments;
    }

    public boolean isKeyChangeAllowed() {
        return state == BatchState.INITIALIZED;
    }

    public int getTotalNumberOfDocuments() {
        return totalNumberOfDocuments;
    }

    public int getProcessedDocumentsCount() {
        return processedDocumentsCount;
    }

    // private getters

    private static String getNewBatchId() {
        // TODO maybe there is more secure alternative to UUID?
        return UUID.randomUUID().toString();
    }

    private boolean isExpired() {
        return expriationDate.before(new Date());
    }

    public void resetExpirationDate() {
        expriationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 1 minute
    }

}
