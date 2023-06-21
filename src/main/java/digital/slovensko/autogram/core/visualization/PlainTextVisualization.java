package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class PlainTextVisualization extends Visualization {
    private String document;

    public PlainTextVisualization(String plainText) {
        this.document = plainText;
    }

    @Override
    public void show(SigningDialogController controller) {
        controller.showPlainTextVisualization(this.document);
    }
}