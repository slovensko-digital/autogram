package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class UnsupportedVisualization extends Visualization {

    @Override
    public void show(SigningDialogController controller) {
        controller.showUnsupportedVisualization();
    }
}