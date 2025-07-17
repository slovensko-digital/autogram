package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.IgnorableException;

public class FailedVisualizationException extends IgnorableException {

    public FailedVisualizationException(Throwable e, SigningJob job, Runnable onContinueCallback) {
        super(e, job, onContinueCallback);
    }
}
