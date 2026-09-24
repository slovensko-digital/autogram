package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.SigningResponder;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Per-document responder for a GUI file batch. It saves the signed file and
 * reports the outcome back to the batch driver; batch counters are owned by
 * {@code Autogram}.
 */
public class SaveFileFromBatchResponder implements SigningResponder {
    private final File file;
    private final TargetPath targetPath;
    private final Consumer<File> callbackSuccess;
    private final Consumer<AutogramException> callbackError;
    private final Runnable callbackSkipped;
    private final Runnable callbackSkippedRemaining;

    public SaveFileFromBatchResponder(File file, TargetPath targetPath,
            Consumer<File> callbackSuccess, Consumer<AutogramException> callbackError) {
        this(file, targetPath, callbackSuccess, callbackError, () -> {
        }, () -> {
        });
    }

    public SaveFileFromBatchResponder(File file, TargetPath targetPath,
            Consumer<File> callbackSuccess, Consumer<AutogramException> callbackError,
            Runnable callbackSkipped, Runnable callbackSkippedRemaining) {
        this.file = file;
        this.targetPath = targetPath;
        this.callbackSuccess = callbackSuccess;
        this.callbackError = callbackError;
        this.callbackSkipped = callbackSkipped;
        this.callbackSkippedRemaining = callbackSkippedRemaining;
    }

    @Override
    public void onDocumentSigned(SignedDocument signedDocument) {
        try {
            var targetFile = targetPath.getSaveFilePath(file.toPath(), MimeTypeEnum.PDF.equals(signedDocument.getDocument().getMimeType()));
            signedDocument.getDocument().save(targetFile.toString());
            Logging.log("Saved file " + targetFile.toString());
            callbackSuccess.accept(targetFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDocumentFailed(AutogramException error) {
        Logging.log("Sign failed - error occurred: " + error.toString());
        callbackError.accept(error);
    }

    @Override
    public void onDocumentCanceled() {
        onDocumentFailed(new SigningCanceledByUserException());
    }

    @Override
    public void onDocumentSkipped() {
        callbackSkipped.run();
    }

    @Override
    public void onDocumentSkippedRemaining() {
        callbackSkippedRemaining.run();
    }
}
