package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.ui.Visualizer;
import eu.europa.esig.dss.model.DSSDocument;
import javafx.concurrent.Task;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

public class PDFVisualization extends Visualization {
    private final DSSDocument document;
    private final UserSettings settings;
    private final Executor executor;
    private final List<? extends Task<byte[]>> pages;


    public PDFVisualization(DSSDocument document, SigningJob job, UserSettings settings, Executor executor)
            throws IOException {
        super(job);
        this.document = document;
        this.settings = settings;
        this.executor = executor;
        pages = getPdfImages();
    }

    @Override
    public void initialize(Visualizer visualizer) throws IOException {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPDFVisualization(pages);
    }

    private List<? extends Task<byte[]>> getPdfImages() throws IOException {
        return IntStream.range(0, getPageCount())
                .mapToObj(PdfPageRenderingTask::new)
                .peek(executor::execute)
                .toList();
    }

    private byte[] renderPageAsImage(int pageNumber, PDDocument pdf) throws IOException {
        var pdfRenderer = new PDFRenderer(pdf);
        var bim = pdfRenderer.renderImageWithDPI(pageNumber, settings.getPdfDpi(), ImageType.RGB);
        try (var os = new ByteArrayOutputStream()) {
            ImageIO.write(bim, "png", os);
            return os.toByteArray();
        }
    }

    private int getPageCount() throws IOException {
        try (
                var inputStream = this.document.openStream();
                var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))
        ) {
            return pdfDocument.getNumberOfPages();
        }
    }

    private class PdfPageRenderingTask extends Task<byte[]> {
        private final int pageNumber;

        public PdfPageRenderingTask(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        @Override
        protected byte[] call() throws IOException {
            try (
                    var inputStream = document.openStream();
                    PDDocument pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))
            ) {
                if (isCancelled()) throw new CancellationException();

                return renderPageAsImage(pageNumber, pdfDocument);
            }
        }
    }

}