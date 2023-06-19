package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

public abstract class BatchStartResponder {
    abstract public void onBatchStartSuccess(String batchId);

    abstract public void onBatchStartFailure(AutogramException error);
}
