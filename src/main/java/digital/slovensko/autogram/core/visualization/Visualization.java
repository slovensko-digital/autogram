package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.Visualizer;

import java.io.IOException;

public abstract class Visualization {
    private final String name;

    protected Visualization(String name) {
        this.name = name;
    }

    public abstract void initialize(Visualizer visualizer) throws IOException;

    public String getName() {
        return name;
    }
}
