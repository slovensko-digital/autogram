package digital.slovensko.autogram.core.visualization;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import digital.slovensko.autogram.core.FailedVisualizationException;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.DSSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;

public class PDFVisualization extends Visualization {
    private final DSSDocument document;

    public PDFVisualization(DSSDocument document, SigningJob job) {
        super(job);
        this.document = document;
    }

    private String getBase64EncodedDocument() {
        try (var is = document.openStream()) {
            return new String(Base64.getEncoder().encode(is.readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<String> getHTML() throws IOException {
        var pdfDocument = PDDocument.load(this.document.openStream());
        var pdfRenderer = new PDFRenderer(pdfDocument);
        var divs = new ArrayList<String>();
        for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
            var os = new ByteArrayOutputStream();
            var bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            ImageIO.write(bim, "png", os);
            divs.add("<div><img src=\"data:image/png;base64, " + new String(Base64.getEncoder().encode(os.toByteArray())) + "\" /></div>");
        }

        pdfDocument.close();

        return divs;
    }

    @Override
    public void initialize(Visualizer visualizer) throws IOException {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPDFVisualization(getHTML());
    }
}