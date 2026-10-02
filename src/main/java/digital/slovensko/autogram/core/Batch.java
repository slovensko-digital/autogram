package digital.slovensko.autogram.core;

/** A batch signing session, or the absence of one before signing begins. */
public interface Batch {
    void start(SigningKey key);

    void ensureCanStartNewBatch();

    boolean shouldResetPasswordAfterSigning();

    void addJob(String batchId);

    void onJobSuccess();

    void onJobFailure();

    void end();

    void validate(String batchId);

    String getBatchId();

    boolean isEnded();

    boolean isAllProcessed();

    boolean isKeyChangeAllowed();

    int getTotalNumberOfDocuments();

    int getProcessedDocumentsCount();

    SigningKey getSigningKey();

    void log();
}
