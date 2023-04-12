package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;

import java.io.IOException;

import com.google.common.io.Files;

public class SaveFileResponder extends Responder {
    private final String filename;

    public SaveFileResponder(String filename) {
        this.filename = filename;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            signedDocument.getDocument().save(getSaveName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }

    // TODO: move elsewhere and solve the problem with existing files
    private String getSaveName() {
        var name = Files.getNameWithoutExtension(filename);
        if (filename.endsWith(".pdf"))
            return name + "_signed.pdf";

        return name + "_signed.asice";
    }
}
