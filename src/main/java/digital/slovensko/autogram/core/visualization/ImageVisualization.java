package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.DSSDocument;

public class ImageVisualization extends Visualization {
    private final DSSDocument document;

    public ImageVisualization(DSSDocument document) {
        super(document.getName());
        this.document = document;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth();
        visualizer.showImageVisualization(document);
    }
}