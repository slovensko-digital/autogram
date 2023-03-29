package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import eu.europa.esig.dss.model.DSSDocument;

public class GUIResponder extends Responder {
    private final GUI ui;

    public GUIResponder(GUI ui) {
        this.ui = ui;
    }

    @Override
    public void onDocumentSigned(DSSDocument signedDocument, SigningKey signingKey) {
        System.out.println("Signing DONE for document " + signedDocument.toString());
    }

    @Override
    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.out.println("Signing error occurred " + error.toString() + " for job " + job.toString());
    }
}
