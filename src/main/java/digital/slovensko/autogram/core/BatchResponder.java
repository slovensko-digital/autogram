package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

public abstract class BatchResponder {
    abstract public void onBatchStartSuccess(Batch batch);

    abstract public void onBatchStartFailure(AutogramException error);
}
