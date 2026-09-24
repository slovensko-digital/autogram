package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningMode;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.SigningResponder;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.FileDocument;

/**
 * Drives a GUI file batch for both {@link SigningMode}s. Automated batches submit
 * all files at once and share one key; interactive batches open the signing dialog
 * for one file at a time. Batch counters are owned by {@code Autogram}; this
 * responder only keeps the per-file result maps used for the summary dialog.
 */
public class BatchGuiFileResponder implements SigningResponder {
    private final Autogram autogram;
    private final List<File> list;
    private final Map<File, File> targetFiles = new HashMap<>();
    private final Map<File, AutogramException> errors = new HashMap<>();
    private boolean uiNotifiedOnAllFilesSigned = false;
    private final TargetPath targetPath;
    private final SigningParameters signingParameters;
    private final EFormAttributes eFormAttributes;
    private int currentFileIndex = 0;

    public BatchGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory,
            SigningParameters signingParameters, EFormAttributes eFormAttributes, boolean signPDFAsPades) {
        this.autogram = autogram;
        this.list = list;
        this.signingParameters = signingParameters;
        this.eFormAttributes = eFormAttributes;
        this.targetPath = TargetPath.fromTargetDirectory(targetDirectory, signPDFAsPades);
    }

    @Override
    public void onBatchStarted(Batch batch, SigningMode mode) {
        try {
            targetPath.mkdirIfDir();
        } catch (AutogramException e) {
            autogram.onSigningFailed(e);
            throw e;
        }

        if (mode == SigningMode.INTERACTIVE)
            processNextInteractive(batch);
        else
            processAllAtOnce(batch);
    }

    @Override
    public void onBatchStartFailed(AutogramException error) {
        autogram.onSigningFailed(error);
    }

    private void processAllAtOnce(Batch batch) {
        for (File file : list) {
            initFileResult(file);
            try {
                var responder = new SaveFileFromBatchResponder(file, targetPath,
                        targetFile -> {
                            targetFiles.put(file, targetFile);
                            onAllFilesSigned(batch);
                        },
                        error -> {
                            errors.put(file, error);
                            onAllFilesSigned(batch);
                        });
                var job = buildJob(file, batch, null);
                autogram.batchSign(job, batch.getBatchId(), responder);
            } catch (AutogramException e) {
                handleFileSubmissionFailure(file, e);
                autogram.onSigningFailed(e);
                if (!e.batchCanContinue()) {
                    abortRemainingFiles(batch, e);
                    return;
                }
                onAllFilesSigned(batch);
            }
        }
    }

    private void processNextInteractive(Batch batch) {
        if (currentFileIndex >= list.size()) {
            onAllFilesSigned(batch);
            return;
        }

        File file = list.get(currentFileIndex);
        currentFileIndex++;
        initFileResult(file);

        try {
            var responder = new SaveFileFromBatchResponder(file, targetPath,
                    targetFile -> {
                        targetFiles.put(file, targetFile);
                        processNextInteractive(batch);
                    },
                    error -> {
                        errors.put(file, error);
                        if (batch.isEnded() || !error.batchCanContinue())
                            abortRemainingFiles(batch, new BatchCanceledException());
                        else
                            processNextInteractive(batch);
                    },
                    () -> {
                        errors.put(file, new BatchCanceledException());
                        processNextInteractive(batch);
                    },
                    () -> {
                        errors.put(file, new BatchCanceledException());
                        abortRemainingFiles(batch, new BatchCanceledException());
                    });
            var job = buildJob(file, batch, currentFileIndex);
            autogram.batchSign(job, batch.getBatchId(), responder);
        } catch (AutogramException e) {
            handleFileSubmissionFailure(file, e);
            if (batch.isEnded() || !e.batchCanContinue()) {
                abortRemainingFiles(batch, new BatchCanceledException());
                return;
            }
            processNextInteractive(batch);
        }
    }

    private SigningJob buildJob(File file, Batch batch, Integer batchPosition) {
        var input = SigningInput.fromFile(AutogramDocument.build(new FileDocument(file), eFormAttributes),
                signingParameters);
        return SigningJob.fromInput(input, batch, batchPosition);
    }

    private void initFileResult(File file) {
        targetFiles.put(file, null);
        errors.put(file, null);
    }

    private void handleFileSubmissionFailure(File file, AutogramException error) {
        // If a responder callback already set the error, the outcome was already recorded.
        if (errors.get(file) != null)
            return;

        errors.put(file, error);
        autogram.recordPreSubmissionFailure();
        Logging.log("Signing failed before job submission " + file);
    }

    private void abortRemainingFiles(Batch batch, AutogramException error) {
        var aborted = 0;
        for (File file : list) {
            if (targetFiles.containsKey(file))
                continue;

            initFileResult(file);
            errors.put(file, error);
            aborted++;
        }

        autogram.recordAborted(aborted);
        onAllFilesSigned(batch);
    }

    private void onAllFilesSigned(Batch batch) {
        Logging.log("onAllFilesSigned " + batch.isAllProcessed() + " " + uiNotifiedOnAllFilesSigned);
        if (batch.isAllProcessed() && !uiNotifiedOnAllFilesSigned) {
            uiNotifiedOnAllFilesSigned = true;
            Logging.log(errors.values().stream().map(e -> e == null ? "" : e.toString()).toList());
            var result = new BatchUiResult(targetPath, targetFiles, errors);
            autogram.onDocumentBatchSaved(result);
        }
    }
}
