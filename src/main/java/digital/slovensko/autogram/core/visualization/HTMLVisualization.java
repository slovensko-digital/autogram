package digital.slovensko.autogram.core.visualization;
import digital.slovensko.autogram.ui.Visualizer;

public class HTMLVisualization extends Visualization {
    private final String document;

    public HTMLVisualization(String html, String name) {
        super(name);
        this.document = html;
    }

    public String getDocument() {
        return this.document;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth();
        visualizer.showHTMLVisualization(document);
    }
}