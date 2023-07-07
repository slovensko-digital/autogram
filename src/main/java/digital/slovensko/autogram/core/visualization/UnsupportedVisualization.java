package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;

public class UnsupportedVisualization extends Visualization {
    public UnsupportedVisualization(SigningJob job) {
        super(job);
    }

    public void initialize(Visualizer visualizer) {
        // no pref width, keep default
        visualizer.showUnsupportedVisualization();
    }
}