package digital.slovensko.autogram.core.visualization;
import digital.slovensko.autogram.ui.gui.SigningDialogController;
import eu.europa.esig.dss.model.CommonDocument;

public class ImageVisualization extends Visualization {
    CommonDocument document;

    public ImageVisualization(CommonDocument document) {
        this.document = document;
    }

    public CommonDocument getCommonDocument() {
        return this.document;
    }

    @Override
    public void show(SigningDialogController controller) {
        controller.showImageVisualization(document);
    }
}