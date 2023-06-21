package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.VisualizationType;

public class DocumentVisualizationResult {

    private final VisualizedDocument visualizedDocument;
    private final Exception transformationException;

    public DocumentVisualizationResult(VisualizedDocument visualizedDocument,
            Exception transformationException) {
        this.visualizedDocument = visualizedDocument == null ? new UnsupportedVisualizedDocument()
                : visualizedDocument;
        this.transformationException = transformationException;
    }

    public VisualizedDocument getVisualizedDocument() {
        return visualizedDocument;
    }

    /**
     * 
     * @return error whichi occured during transformation
     */
    public Exception getError() {
        return transformationException;
    }

    /**
     * 
     * @return transformation type of visualization (visualized document)
     */
    public VisualizationType getVisualizationType() {
        return visualizedDocument.type;
    }

    public boolean hasFailedTransformation() {
        return transformationException != null;
    }

}
