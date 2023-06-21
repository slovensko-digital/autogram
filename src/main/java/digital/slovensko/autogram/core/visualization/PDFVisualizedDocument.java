package digital.slovensko.autogram.core.visualization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import digital.slovensko.autogram.core.VisualizationType;
import digital.slovensko.autogram.ui.gui.SigningDialogController;
import eu.europa.esig.dss.model.CommonDocument;

public class PDFVisualizedDocument extends VisualizedDocument {
    VisualizationType type = VisualizationType.PDF;
    CommonDocument document;

    public PDFVisualizedDocument(CommonDocument document) {
        this.document = document;
    }

    private String getBase64EncodedDocument() {
        try (var is = document.openStream()) {
            return new String(Base64.getEncoder().encode(is.readAllBytes()),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void show(SigningDialogController controller) {
        controller.showPDFVisualization(getBase64EncodedDocument());
    }
}