package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.Visualizer;

public class UnsupportedVisualization extends Visualization {
    public UnsupportedVisualization(String name) {
        super(name);
    }

    public void initialize(Visualizer visualizer) {
        // no pref width, keep default
        visualizer.showUnsupportedVisualization();
    }
}