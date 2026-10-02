package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;

/** Receives batch lifecycle outcomes. */
public interface BatchResponder {
    void onBatchStarted(Batch batch);

    void onBatchStartFailed(AutogramException error);
}
