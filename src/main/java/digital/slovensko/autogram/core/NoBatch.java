package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchNotStartedException;

/** Sentinel representing the absence of an active batch. */
final class NoBatch extends Batch {
    public NoBatch() {
        super(0);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isEnded() {
        return false;
    }

    @Override
    public boolean isAllProcessed() {
        return false;
    }

    @Override
    public boolean isKeyChangeAllowed() {
        return false;
    }

    @Override
    public void setMode(SigningMode mode) {
    }

    @Override
    public SigningMode getMode() {
        return SigningMode.BULK;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean hasMultipleDocuments() {
        return false;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public void start(SigningKey key) {
        throw new BatchNotStartedException();
    }

    @Override
    public void end() {
    }

    @Override
    public void onJobSuccess() {
    }

    @Override
    public void onJobFailure() {
    }

    @Override
    public void addJob(String batchId) {
        throw new BatchNotStartedException();
    }

    @Override
    public void validate(String batchId) {
        throw new BatchNotStartedException();
    }

    @Override
    public String getBatchId() {
        throw new BatchNotStartedException();
    }

    @Override
    public boolean hasBatchId(String batchId) {
        return false;
    }

    @Override
    public void resetExpirationDate() {
    }

    @Override
    public void log() {
    }
}
