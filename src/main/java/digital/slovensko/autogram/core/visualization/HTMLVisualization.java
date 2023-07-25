package digital.slovensko.autogram.core.visualization;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;

public class HTMLVisualization extends Visualization {
    private final String document;

    public HTMLVisualization(String html, SigningJob job) {
        super(job);
        this.document = html;
    }

    public String getDocument() {
        return this.document;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showHTMLVisualization(document);
    }
}