package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.VisualizationType;
import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class PlainTextVisualizedDocument extends VisualizedDocument {
    private String document;
    VisualizationType type = VisualizationType.PlainText;

    public PlainTextVisualizedDocument(String plainText) {
        this.document = plainText;
    }

    @Override
    public void show(SigningDialogController controller) {
        controller.showPlainTextVisualization(this.document);
    }
}