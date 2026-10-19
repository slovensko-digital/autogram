package digital.slovensko.autogram.ui.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.SaveFileFromBatchResponder;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.FileDocument;

/**
 * Signs files picked in the GUI as a batch. Every file gets exactly one result, a signed file or an error, also when
 * it can't be read or the batch ends before it - the result dialog shows once all files have one.
 */
public class BatchGuiFileResponder extends BatchResponder {
    private final Autogram autogram;
    private final List<File> list;
    private final TargetPath targetPath;
    private final SigningParameters signingParameters;
    private final EFormAttributes eFormAttributes;
    // results come from several threads (FX, signing workers)
    private final Map<File, File> targetFiles = Collections.synchronizedMap(new HashMap<>());
    private final Map<File, AutogramException> errors = Collections.synchronizedMap(new HashMap<>());
    private final Set<File> filesWithResult = new HashSet<>(); // guarded by this
    private boolean uiNotifiedOnAllFilesSigned = false; // guarded by this
    private boolean createdTargetDirectory = false;

    public BatchGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory, SigningParameters signingParameters, EFormAttributes eFormAttributes, boolean signPDFAsPades) {
        this.autogram = autogram;
        this.list = list;
        this.signingParameters = signingParameters;
        this.eFormAttributes = eFormAttributes;
        this.targetPath = TargetPath.fromTargetDirectory(targetDirectory, signPDFAsPades);
    }

    @Override
    public void onBatchStartSuccess(Batch batch) {
        for (var file : list) {
            targetFiles.put(file, null);
            errors.put(file, null);
        }

        try {
            var targetDirectory = targetPath.getTargetDirectory();
            createdTargetDirectory = targetDirectory != null && !Files.exists(targetDirectory);
            targetPath.mkdirIfDir();
        } catch (AutogramException e) {
            // nothing could be saved, so nothing is signed
            autogram.finishBatch(batch, e);
            autogram.onSigningFailed(e);
            return;
        }

        if (batch.isInteractive()) {
            submitInteractive(batch, 0);
            return;
        }

        for (var file : list)
            submit(batch, file, null);
    }

    /** Shows the files one at a time, the next one once the previous got its result. */
    private void submitInteractive(Batch batch, int index) {
        for (var i = index; i < list.size(); i++) {
            var file = list.get(i);
            if (batch.isEnded()) {
                // skipped or canceled by the user, or a fatal error ended the batch
                autogram.recordBatchSubmissionFailure(batch);
                onFileResult(file, null, new SigningCanceledByUserException(), null);
                continue;
            }

            var next = i + 1;
            if (submit(batch, file, () -> submitInteractive(batch, next)))
                return;
        }
    }

    /**
     * @param afterResult runs once the file got its result
     * @return true if the file was handed to the batch, false if it failed right away
     */
    private boolean submit(Batch batch, File file, Runnable afterResult) {
        SigningJob job;
        try {
            var responder = new SaveFileFromBatchResponder(file, targetPath,
                    target -> onFileResult(file, target, null, afterResult),
                    error -> onFileResult(file, null, error, afterResult));
            var input = SigningInput.fromFile(AutogramDocument.buildFromFile(new FileDocument(file), eFormAttributes), signingParameters);
            job = SigningJob.fromInput(input, responder, batch);
        } catch (Exception e) {
            // e.g. a password protected PDF or a broken container
            var error = toAutogramException(e);
            Logging.log("Unable to prepare " + file + " for signing: " + error);
            autogram.recordBatchSubmissionFailure(batch);
            if (!error.batchCanContinue())
                autogram.finishBatch(batch, error);
            onFileResult(file, null, error, null);
            return false;
        }

        try {
            autogram.batchSign(job, batch.getBatchId());
            return true;
        } catch (Exception e) {
            // e.g. the batch ended or expired meanwhile; the result goes through the responder above
            if (!job.onJobSignFailed(toAutogramException(e)))
                Logging.log("Signing " + file + " failed after its result: " + e);
            return afterResult != null;
        }
    }

    private void onFileResult(File file, File target, AutogramException error, Runnable afterResult) {
        targetFiles.put(file, target);
        errors.put(file, error);
        Logging.log(error == null ? "Signed " + file : "Signing failed " + file + ": " + error);
        synchronized (this) {
            filesWithResult.add(file);
        }

        if (afterResult != null)
            afterResult.run();

        notifyUiOnceAllFilesHaveResult();
    }

    private void notifyUiOnceAllFilesHaveResult() {
        BatchUiResult result;
        synchronized (this) {
            if (uiNotifiedOnAllFilesSigned || filesWithResult.size() < list.size())
                return;

            uiNotifiedOnAllFilesSigned = true;
            synchronized (targetFiles) {
                synchronized (errors) {
                    result = new BatchUiResult(targetPath, new HashMap<>(targetFiles), new HashMap<>(errors));
                }
            }
        }
        if (result.getSignedCount() == 0 && createdTargetDirectory)
            deleteEmptyTargetDirectory();
        autogram.onDocumentBatchSaved(result);
    }

    /** Nothing was signed, so the directory created for the signed files isn't needed. */
    private void deleteEmptyTargetDirectory() {
        try {
            Files.deleteIfExists(targetPath.getTargetDirectory());
        } catch (IOException e) {
            // e.g. the user put something in it meanwhile
            Logging.log("Unable to delete " + targetPath.getTargetDirectory() + ": " + e);
        }
    }

    private static AutogramException toAutogramException(Exception e) {
        if (e instanceof AutogramException autogramException)
            return autogramException;
        if (e instanceof DSSException dssException)
            return AutogramException.createFromDSSException(dssException);
        return new UnrecognizedException(e);
    }

    @Override
    public void onBatchStartFailure(AutogramException error) {
        // the user closed the mode picker or the batch window, nothing to tell them
        if (error instanceof BatchCanceledException)
            return;

        autogram.onSigningFailed(error);
    }
}
