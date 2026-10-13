package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchNotStartedException;

import java.util.List;

/** The state before the first batch has been created. */
public final class NoBatch implements Batch {
    @Override
    public void setMode(SigningMode mode) {
        throw new BatchNotStartedException();
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public void start(SigningKey key) {
        throw new BatchNotStartedException();
    }

    @Override
    public void ensureCanStartNewBatch() {
    }

    @Override
    public boolean shouldResetPasswordAfterSigning() {
        return true;
    }

    @Override
    public void addJob(String batchId, SigningJob job) {
        throw new BatchNotStartedException();
    }

    @Override
    public void success(SigningJob job) {
        throw new BatchNotStartedException();
    }

    @Override
    public void failure(SigningJob job) {
        throw new BatchNotStartedException();
    }

    @Override
    public void failure() {
        throw new BatchNotStartedException();
    }

    @Override
    public List<SigningJob> end() {
        throw new BatchNotStartedException();
    }

    @Override
    public List<SigningJob> expire() {
        throw new BatchNotStartedException();
    }

    @Override
    public boolean isEnded() {
        return true;
    }

    @Override
    public void validate(String batchId) {
        throw new BatchNotStartedException();
    }

    @Override
    public void validateId(String batchId) {
        throw new BatchNotStartedException();
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public String getBatchId() {
        throw new BatchNotStartedException();
    }

    @Override
    public boolean isAllProcessed() {
        throw new BatchNotStartedException();
    }

    @Override
    public boolean isKeyChangeAllowed() {
        throw new BatchNotStartedException();
    }

    @Override
    public int getTotalNumberOfDocuments() {
        throw new BatchNotStartedException();
    }

    @Override
    public int getProcessedDocumentsCount() {
        throw new BatchNotStartedException();
    }

    @Override
    public SigningKey getSigningKey() {
        throw new BatchNotStartedException();
    }

    @Override
    public void log() {
        throw new BatchNotStartedException();
    }
}
