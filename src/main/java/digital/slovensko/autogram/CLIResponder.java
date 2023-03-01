package digital.slovensko.autogram;

import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;

public class CLIResponder extends Responder {
    public void onDocumentSigned(DSSDocument d) {
        System.out.println("Sign success for document " + d.toString());
        try {
            d.save("pom.xml.asice");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(SigningJob job, Error error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
