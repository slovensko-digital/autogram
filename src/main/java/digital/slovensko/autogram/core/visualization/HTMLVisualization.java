package digital.slovensko.autogram.core.visualization;
import digital.slovensko.autogram.ui.gui.SigningDialogController;

public class HTMLVisualization extends Visualization {
    private String document;

    public HTMLVisualization(String html) {
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