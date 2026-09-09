package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;

import java.io.IOException;

public abstract class Visualization {
    private final SigningJob job;

    protected Visualization(SigningJob job) {
        this.job = job;
    }

    public abstract void initialize(Visualizer visualizer) throws IOException;

    public SigningJob getJob() {
        return job;
    }

    public String getDialogTitle(String documentLabel, Integer batchPosition) {
        var title = documentLabel;
        if (job.getDocument().getName() != null)
            title += " " + job.getDocument().getName();

        var batch = job.getBatch();
        if (batchPosition != null && batch != null)
            title += " (%d z %d)".formatted(batchPosition, batch.getTotalNumberOfDocuments());

        return title;
    }

    protected double getVisualizationWidth() {
        return job.getVisualizationWidth();
    }
}
