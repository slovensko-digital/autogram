package digital.slovensko.autogram.ui;

import eu.europa.esig.dss.model.DSSDocument;

import java.util.List;

public interface Visualizer {
    void showUnsupportedVisualization();

    void showPDFVisualization(List<byte[]> base64EncodedDocument);

    void showHTMLVisualization(String document);

    void showPlainTextVisualization(String document);

    void showImageVisualization(DSSDocument document);

    void setPrefWidth(double visualizationWidth);
}
