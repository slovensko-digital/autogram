package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.VisualizationType;
import digital.slovensko.autogram.ui.gui.SigningDialogController;

public abstract class VisualizedDocument {
    public final VisualizationType type = null;

    public void show(SigningDialogController controller) {
        controller.showUnsupportedVisualization();
    }
}