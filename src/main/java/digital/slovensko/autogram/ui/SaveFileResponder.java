package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.SigningError;
import digital.slovensko.autogram.core.SigningJob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.google.common.io.Files;

public class SaveFileResponder extends Responder {
    private final File file;

    public SaveFileResponder(File file) {
        this.file = file;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            signedDocument.getDocument().save(getTargetFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(SigningJob job, SigningError error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }

    private String getTargetFilename() {
        var directory = file.getParent();
        var name = Files.getNameWithoutExtension(file.getName());

        var extension = ".asice";
        if (file.getName().endsWith(".pdf"))
            extension = ".pdf";

        var baseName = Paths.get(directory, name + "_signed").toString();
        var newBaseName = baseName;
        for (var count = 1; new File(newBaseName + extension).exists(); count++)
            newBaseName = baseName + " (" + count + ")";

        return newBaseName + extension;
    }
}
