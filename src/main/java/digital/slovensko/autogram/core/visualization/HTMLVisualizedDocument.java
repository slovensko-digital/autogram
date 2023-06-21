package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.VisualizationType;
import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class HTMLVisualizedDocument extends VisualizedDocument {
    private String document;
    VisualizationType type = VisualizationType.HTML;

    public HTMLVisualizedDocument(String html) {
        this.document = html;
    }

    public String getDocument() {
        return this.document;
    }

    @Override
    public void show(SigningDialogController controller) {
        controller.showHTMLVisualization(document);
    }
}