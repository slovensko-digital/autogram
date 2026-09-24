package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.BatchNotStartedException;

/**
 * Null object for "no batch started". Replaces the {@code batch == null} checks
 * in {@link Autogram} with no-op calls; anything that needs a real batch (batch
 * id validation, adding documents) still fails with
 * {@link BatchNotStartedException}, so the HTTP contract is unchanged.
 */
final class BatchNotStarted extends Batch {
    public BatchNotStarted() {
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
        return SigningMode.AUTOMATED;
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