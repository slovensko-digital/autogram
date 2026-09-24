package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.Visualizer;

public class PlainTextVisualization extends Visualization {
    private final String document;

    public PlainTextVisualization(String plainText, String name) {
        super(name);
        this.document = plainText;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth();
        visualizer.showPlainTextVisualization(document);
    }
}