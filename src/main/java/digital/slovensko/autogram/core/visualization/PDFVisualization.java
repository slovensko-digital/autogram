package digital.slovensko.autogram.core.visualization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.pades.exception.InvalidPasswordException;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxDocumentReader;

public class PDFVisualization extends Visualization {
    private final CommonDocument document;

    public PDFVisualization(CommonDocument document, SigningJob job) {
        super(job);
        this.document = document;

        this.checkPdfPasswordProtection();
    }

    private String getBase64EncodedDocument() {
        try (var is = document.openStream()) {
            return new String(Base64.getEncoder().encode(is.readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPDFVisualization(getBase64EncodedDocument());
    }

    private void checkPdfPasswordProtection() {
        try {
            PdfBoxDocumentReader reader = new PdfBoxDocumentReader(document);
            reader.close();
        } catch (InvalidPasswordException e) {
            throw e;
        } catch (IOException e) {
        }
    }
}