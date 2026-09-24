package digital.slovensko.autogram.core.visualization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.DSSDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;

public class PDFVisualization extends Visualization {
    private final DSSDocument document;


    public PDFVisualization(DSSDocument document) {
        super(document.getName());
        this.document = document;
    }

    private ArrayList<byte []> getPdfImages(int pdfDpi) throws IOException {
        var pdfDocument = Loader.loadPDF(this.document.openStream().readAllBytes());
        var pdfRenderer = new PDFRenderer(pdfDocument);
        var divs = new ArrayList<byte[]>();
        for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
            var os = new ByteArrayOutputStream();
            var bim = pdfRenderer.renderImageWithDPI(page, pdfDpi, ImageType.RGB);
            ImageIO.write(bim, "png", os);
            divs.add(os.toByteArray());
        }

        pdfDocument.close();

        return divs;
    }

    @Override
    public void initialize(Visualizer visualizer) throws IOException {
        visualizer.setPrefWidth();
        visualizer.showPDFVisualization(getPdfImages(visualizer.getPdfDpi()));
    }
}