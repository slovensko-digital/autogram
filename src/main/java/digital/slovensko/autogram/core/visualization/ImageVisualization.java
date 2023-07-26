package digital.slovensko.autogram.core.visualization;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;

public class ImageVisualization extends Visualization {
    private final DSSDocument document;

    public ImageVisualization(DSSDocument document, SigningJob job) {
        super(job);
        this.document = document;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showImageVisualization(document);
    }
}