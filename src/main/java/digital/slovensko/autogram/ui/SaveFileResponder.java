package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;

import java.io.IOException;

public class SaveFileResponder extends Responder {
    private final String filename;

    public SaveFileResponder(String filename) {
        this.filename = filename;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        System.out.println("Sign success for document " + signedDocument.getDocument().toString());
        try {
            signedDocument.getDocument().save(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
