package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.model.DSSDocument;

public interface SigningJob {

    DSSDocument getDocument();
    
    boolean isPlainText();

    boolean isHTML();

    int getVisualizationWidth();

    boolean isPDF();

    boolean isImage();

    String getDocumentAsPlainText() throws AutogramException;

    String getDocumentAsHTML() throws AutogramException;

    String getDocumentAsBase64Encoded() throws AutogramException;

    void signWithKeyAndRespond(SigningKey key) throws AutogramException;

    void onDocumentSignFailed(AutogramException e);

    boolean shouldCheckPDFCompliance();
}
