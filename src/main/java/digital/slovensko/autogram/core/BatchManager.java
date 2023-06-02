package digital.slovensko.autogram.core;

import java.util.Date;
import java.util.UUID;

public class BatchManager { // better name? container/handler/manager
    private String batchId;
    private SigningKey signingKey;
    private Date expriationDate = new Date();
    private int totalNumberOfDocuments;
    private int processedDocumentsCount;

    public BatchManager() {
    }

    // methods

    public void initialize(int totalNumberOfDocuments) {
        if (batchId != null || this.signingKey != null)
            throw new IllegalStateException("Batch session is already started");

        resetExpirationDate();
        batchId = getNewBatchId();
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        this.processedDocumentsCount = 0;
    }

    public void start(SigningKey signingKey) {
        this.signingKey = signingKey;
    }

    public void addJob(String batchId, SigningJob job) {
        validate(batchId);

        processedDocumentsCount++;
    }

    public void reset() {
        batchId = null;
        signingKey = null;
        expriationDate = new Date();
        totalNumberOfDocuments = 0;
        processedDocumentsCount = 0;
    }

    public void end(String batchId) {
        if (this.batchId == null || signingKey == null)
            throw new IllegalStateException("Batch session is not started");

        if (!this.batchId.equals(batchId))
            throw new IllegalArgumentException("Batch session ID does not match");

        if (this.processedDocumentsCount < this.totalNumberOfDocuments) {
            reset();
            throw new IllegalStateException("Session didn't get all items");
        }

        reset();
    }

    public boolean validate(String batchId) {
        if (this.batchId == null || this.signingKey == null)
            throw new IllegalStateException("Batch session is not started");

        if (isExpired()) {
            reset();
            throw new IllegalStateException("Batch session is expired");
        }

        if (!this.batchId.equals(batchId))
            throw new IllegalArgumentException("Batch session ID does not match");

        if (this.totalNumberOfDocuments <= this.processedDocumentsCount)
            throw new IllegalAccessError("Sent more sign requests than declared at start");

        System.out.println("Batch session " + batchId + " is valid with current session ("
                + this.batchId + ")");
        return true;
    }

    // public getters

    public String getBatchId() {
        validate(batchId);

        return batchId;
    }

    // public SigningKey getSigningKey(String batchId) {
    // validate(batchId);
    // if (isExpired())
    // throw new IllegalStateException("Batch session is expired");

    // if (this.totalNumberOfDocuments <= this.processedDocumentsCount)
    // throw new IllegalAccessError("Sent more sign requests than declared at
    // start");

    // resetExpirationDate();
    // return signingKey;
    // }

    public int getTotalNumberOfDocuments() {
        return totalNumberOfDocuments;
    }

    public int getProcessedDocumentsCount() {
        return processedDocumentsCount;
    }

    // private getters

    private String getNewBatchId() {
        // TODO maybe there is more secure alternative to UUID?
        return UUID.randomUUID().toString();
    }

    private boolean isExpired() {
        return expriationDate.before(new Date());
    }

    // private setters

    public void resetExpirationDate() {
        expriationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 1 minute
    }

}
