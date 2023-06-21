package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;

public abstract class Visualization {
    private final SigningJob job;

    protected Visualization(SigningJob job) {
        this.job = job;
    }

    public abstract void initialize(Visualizer visualizer);

    public SigningJob getJob() {
        return job;
    }

    protected double getVisualizationWidth() {
        return job.getVisualizationWidth();
    }
}
