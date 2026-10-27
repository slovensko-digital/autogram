package digital.slovensko.autogram.core;

import java.util.List;

/** A batch signing session, or the absence of one before signing begins. */
public interface Batch {
    void start(SigningKey key);

    void setMode(SigningMode mode);

    boolean isInteractive();

    boolean isPresent();

    void ensureCanStartNewBatch();

    boolean shouldResetPasswordAfterSigning();

    /** Adds a document; until it gets a result, the batch doesn't expire and ending the batch gives it a result. */
    void addJob(String batchId, SigningJob job);

    void success(SigningJob job);

    void failure(SigningJob job);

    /** A document that couldn't even be added, e.g. a file that can't be read. */
    void failure();

    /** Ends the batch and returns its documents that didn't get a result yet. */
    List<SigningJob> end();

    /** Like {@link #end()}, but later requests learn the batch expired. */
    List<SigningJob> expire();

    /** Checks the batch accepts documents under this ID: started, not ended, not expired. */
    void validate(String batchId);

    /** Checks only that the ID belongs to this batch, also when it ended or expired. */
    void validateId(String batchId);

    boolean isExpired();

    String getBatchId();

    boolean isEnded();

    boolean isAllProcessed();

    boolean isKeyChangeAllowed();

    int getTotalNumberOfDocuments();

    int getProcessedDocumentsCount();

    SigningKey getSigningKey();

    default boolean canSelectSignatureGoal() {
        return false;
    }

    default void setSignatureChoice(SignatureChoice choice) {
        throw new IllegalStateException("Only a local bulk operation can select a signature goal");
    }

    default SignatureChoice getSignatureChoice() {
        return null;
    }

    void log();
}
