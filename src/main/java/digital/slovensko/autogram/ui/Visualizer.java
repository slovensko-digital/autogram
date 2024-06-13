package digital.slovensko.autogram.ui;

import eu.europa.esig.dss.model.DSSDocument;

import java.util.ArrayList;

public interface Visualizer {
    void showUnsupportedVisualization();

    void showPDFVisualization(ArrayList<byte[]> base64EncodedDocument);

    void showHTMLVisualization(String document);

    void showPlainTextVisualization(String document);

    void showImageVisualization(DSSDocument document);

    void setPrefWidth(double visualizationWidth);
}
