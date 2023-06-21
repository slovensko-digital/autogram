package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.gui.SigningDialogController;

public abstract class Visualization {
    public void show(SigningDialogController controller) {
        controller.showUnsupportedVisualization();
    }
}