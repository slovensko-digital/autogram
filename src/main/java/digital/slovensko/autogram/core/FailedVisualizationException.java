package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.IgnorableException;

public class FailedVisualizationException extends IgnorableException {

    public FailedVisualizationException(Throwable e, SigningJob job, Runnable onContinueCallback,
            Runnable onCancelCallback) {
        super(e, job, onContinueCallback, onCancelCallback);
    }

    public FailedVisualizationException(Throwable e) {
        super(e, null, null, null);
    }
}
