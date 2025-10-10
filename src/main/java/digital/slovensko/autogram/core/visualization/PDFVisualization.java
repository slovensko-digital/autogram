package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.DSSDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFVisualization extends Visualization {
    private final DSSDocument document;
    private final UserSettings settings;


    public PDFVisualization(DSSDocument document, SigningJob job, UserSettings settings) {
        super(job);
        this.document = document;
        this.settings = settings;
    }

    private List<byte[]> getPdfImages() throws IOException {
        try (
                var inputStream = this.document.openStream();
                var buffer = new RandomAccessReadBuffer(inputStream);
                var pdfDocument = Loader.loadPDF(buffer)
        ) {
            var pdfRenderer = new PDFRenderer(pdfDocument);
            var divs = new ArrayList<byte[]>();
            for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
                var bim = pdfRenderer.renderImageWithDPI(page, settings.getPdfDpi(), ImageType.RGB);
                try (var os = new ByteArrayOutputStream()) {
                    ImageIO.write(bim, "png", os);
                    divs.add(os.toByteArray());
                }
            }
            return divs;
        }

    }

    @Override
    public void initialize(Visualizer visualizer) throws IOException {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPDFVisualization(getPdfImages());
    }
}