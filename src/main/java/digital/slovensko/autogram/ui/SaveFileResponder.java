package digital.slovensko.autogram.ui;

import com.google.common.io.Files;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class SaveFileResponder extends Responder {
    private final File file;
    private final Autogram autogram;
    private final TargetPath targetPathBuilder;

    public SaveFileResponder(File file, Autogram autogram) {
        this(file, autogram, new TargetPath(null, file, false));
    }

    public SaveFileResponder(File file, Autogram autogram, TargetPath targetPathBuilder) {
        this.file = file;
        this.autogram = autogram;
        this.targetPathBuilder = targetPathBuilder;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = targetPathBuilder.getSaveFilePath(file);
            signedDocument.getDocument().save(targetFile.getPath());
            autogram.onDocumentSaved(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(AutogramException error) {
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
