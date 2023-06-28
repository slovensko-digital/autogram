package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;

public class IgnorableException extends AutogramException {
    private final SigningJob job;
    private final Runnable onContinueCallback;

    public IgnorableException(String heading, String subheading, String description, Throwable e, SigningJob job, Runnable onContinueCallback) {
        super(heading, subheading, description, e);
        this.job = job;
        this.onContinueCallback = onContinueCallback;
    }

    public SigningJob getJob() {
        return job;
    }

    public Runnable getOnContinueCallback() {
        return onContinueCallback;
    }
}
