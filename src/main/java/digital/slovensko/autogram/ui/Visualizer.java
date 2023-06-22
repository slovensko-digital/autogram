package digital.slovensko.autogram.ui;

import eu.europa.esig.dss.model.CommonDocument;

public interface Visualizer {
    void showUnsupportedVisualization();

    void showPDFVisualization(String base64EncodedDocument);

    void showHTMLVisualization(String document);

    void showPlainTextVisualization(String document);

    void showImageVisualization(CommonDocument document);

    void setPrefWidth(double visualizationWidth);
}
