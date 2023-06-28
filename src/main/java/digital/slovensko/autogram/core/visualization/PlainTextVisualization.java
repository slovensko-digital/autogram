package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;

public class PlainTextVisualization extends Visualization {
    private final String document;

    public PlainTextVisualization(String plainText, SigningJob job) {
        super(job);
        this.document = plainText;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPlainTextVisualization(document);
    }
}