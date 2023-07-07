package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SignedDocument;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class SaveFileFromBatchResponder extends Responder {
    private final File file;
    private final Autogram autogram;
    // private final File targetDirectory;
    private final Consumer<File> callback;
    private final TargetPath targetPath;

    public SaveFileFromBatchResponder(File file, Autogram autogram, TargetPath targetPath, Consumer<File> callback) {
        this.file = file;
        this.autogram = autogram;
        this.targetPath = targetPath;
        this.callback = callback;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = targetPath.getSaveFilePath(file.toPath());
            signedDocument.getDocument().save(targetFile.toString());
            Logging.log("Saved file " + targetFile.toString());
            autogram.updateBatch();
            callback.accept(targetFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(AutogramException error) {
        // TODO tu je zozrany error
        System.err.println("Sign failed error occurred: " + error.toString());
    }
}
