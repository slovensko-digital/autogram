package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class FailedVisualization extends Visualization {
    private Exception error;

    public FailedVisualization(Exception error) {
        this.error = error;
    }

    public Exception getError() {
        return error;
    }

    @Override
    public void show(SigningDialogController controller) {
        controller.showUnsupportedVisualization();
        controller.showFailedTransformationError(error);
    }
}
