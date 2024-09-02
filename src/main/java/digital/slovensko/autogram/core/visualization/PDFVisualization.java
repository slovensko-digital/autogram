package digital.slovensko.autogram.core.visualization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.model.AutogramDocument;
import digital.slovensko.autogram.ui.Visualizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;

public class PDFVisualization extends Visualization {
    private final AutogramDocument document;
    private final UserSettings settings;


    public PDFVisualization(AutogramDocument document, SigningJob job, UserSettings settings) {
        super(job);
        this.document = document;
        this.settings = settings;
    }

    private ArrayList<byte []> getPdfImages() throws IOException {
        var pdfDocument = PDDocument.load(this.document.getDocument().openStream(), new String(this.document.getOpenDocumentPassword()));
        var pdfRenderer = new PDFRenderer(pdfDocument);
        var divs = new ArrayList<byte[]>();
        for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
            var os = new ByteArrayOutputStream();
            var bim = pdfRenderer.renderImageWithDPI(page, settings.getPdfDpi(), ImageType.RGB);
            ImageIO.write(bim, "png", os);
            divs.add(os.toByteArray());
        }

        pdfDocument.close();

        return divs;
    }

    @Override
    public void initialize(Visualizer visualizer) throws IOException {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPDFVisualization(getPdfImages());
    }
}