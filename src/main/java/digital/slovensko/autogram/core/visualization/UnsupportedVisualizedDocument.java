package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.VisualizationType;
import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class UnsupportedVisualizedDocument extends VisualizedDocument {
    VisualizationType type = VisualizationType.Unsupported;

    @Override
    public void show(SigningDialogController controller) {
        controller.showUnsupportedVisualization();
    }
}