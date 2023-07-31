package digital.slovensko.autogram.ui;

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
    private final Consumer<File> callbackSuccess;
    private final Consumer<AutogramException> callbackError;
    private final TargetPath targetPath;

    public SaveFileFromBatchResponder(File file, TargetPath targetPath,
            Consumer<File> callbackSuccess, Consumer<AutogramException> callbackError) {
        this.file = file;
        this.targetPath = targetPath;
        this.callbackSuccess = callbackSuccess;
        this.callbackError = callbackError;
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = targetPath.getSaveFilePath(file.toPath());
            signedDocument.getDocument().save(targetFile.toString());
            Logging.log("Saved file " + targetFile.toString());
            callbackSuccess.accept(targetFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDocumentSignFailed(AutogramException error) {
        Logging.log("Sign failed - error occurred: " + error.toString());
        callbackError.accept(error);
    }
}
