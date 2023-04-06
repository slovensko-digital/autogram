package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;

public class GUIResponder extends Responder {
    private final GUI ui;

    public GUIResponder(GUI ui) {
        this.ui = ui;
    }

    @Override
    public void onDocumentSigned(SignedDocument signedDocument) {
        System.out.println("Signing DONE for document " + signedDocument.getDocument().toString());
    }

    @Override
    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.out.println("Signing error occurred " + error.toString() + " for job " + job.toString());
    }
}
