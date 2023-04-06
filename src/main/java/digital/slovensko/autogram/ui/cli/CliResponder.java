package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;

import java.io.IOException;

public class CliResponder extends Responder {
    public void onDocumentSigned(SignedDocument signedDocument) {
        System.out.println("Sign success for document " + signedDocument.getDocument().toString());
        try {
            signedDocument.getDocument().save("dummy.pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
