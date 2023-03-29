package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;

public class CliResponder extends Responder {
    public void onDocumentSigned(DSSDocument d, SigningKey signingKey) {
        System.out.println("Sign success for document " + d.toString());
        try {
            d.save("pom.xml.asice");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
