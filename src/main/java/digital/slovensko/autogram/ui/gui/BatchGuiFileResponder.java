package digital.slovensko.autogram.ui.gui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.SaveFileFromBatchResponder;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.model.FileDocument;

public class BatchGuiFileResponder extends BatchResponder {
    private final Autogram autogram;
    private final List<File> list;
    private final Map<File, File> targetFiles = new HashMap<>();
    private final Map<File, AutogramException> errors = new HashMap<>();
    private boolean uiNotifiedOnAllFilesSigned = false;
    private final TargetPath targetPath;
    private final SigningParameters signingParameters;
    private final EFormAttributes eFormAttributes;

    public BatchGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory, SigningParameters signingParameters, EFormAttributes eFormAttributes, boolean signPDFAsPades) {
        this.autogram = autogram;
        this.list = list;
        this.signingParameters = signingParameters;
        this.eFormAttributes = eFormAttributes;
        this.targetPath = TargetPath.fromTargetDirectory(targetDirectory, signPDFAsPades);
    }

    @Override
    public void onBatchStartSuccess(Batch batch) {
        try {
            targetPath.mkdirIfDir();
        } catch (AutogramException e) {
            autogram.onSigningFailed(e);
            throw e;
        }

        if (batch.isInteractive()) {
            submitNextInteractive(autogram, batch, list, 0, targetPath, signingParameters, eFormAttributes,
                    targetFiles, errors, () -> onAllFilesSigned(batch));
            return;
        }

        for (File file : list) {
            try {
                targetFiles.put(file, null);
                errors.put(file, null);
                var responder = new SaveFileFromBatchResponder(file, targetPath, (File targetFile) -> {
                    targetFiles.put(file, targetFile);
                    Logging.log(batch.getProcessedDocumentsCount() + " / " + batch.getTotalNumberOfDocuments() + " signed " + file.toString());
                    onAllFilesSigned(batch);
                }, (AutogramException error) -> {
                    Logging.log("Signing failed " + file.toString() + " all:" + batch.isAllProcessed());
                    errors.put(file, error);
                    onAllFilesSigned(batch);
                });

                var input = SigningInput.fromFile(AutogramDocument.build(new FileDocument(file), eFormAttributes), signingParameters);
                var job = SigningJob.fromInput(input, responder, batch);
                autogram.batchSign(job, batch.getBatchId());
            } catch (AutogramException e) {
                autogram.onSigningFailed(e);

                break;
            }
        }
    }

    private static void submitNextInteractive(Autogram autogram, Batch batch, List<File> files, int index,
            TargetPath targetPath, SigningParameters signingParameters, EFormAttributes eFormAttributes,
            Map<File, File> targetFiles, Map<File, AutogramException> errors, Runnable onCompleted) {
        if (batch.isEnded()) {
            for (var remaining = index; remaining < files.size(); remaining++) {
                var file = files.get(remaining);
                targetFiles.put(file, null);
                errors.put(file, new SigningCanceledByUserException());
                autogram.recordBatchSubmissionFailure(batch);
            }
            onCompleted.run();
            return;
        }
        if (index == files.size()) {
            onCompleted.run();
            return;
        }

        var file = files.get(index);
        targetFiles.put(file, null);
        errors.put(file, null);
        try {
            var responder = new SaveFileFromBatchResponder(file, targetPath,
                    target -> {
                        targetFiles.put(file, target);
                        submitNextInteractive(autogram, batch, files, index + 1, targetPath, signingParameters,
                                eFormAttributes, targetFiles, errors, onCompleted);
                    }, error -> {
                        errors.put(file, error);
                        submitNextInteractive(autogram, batch, files, index + 1, targetPath, signingParameters,
                                eFormAttributes, targetFiles, errors, onCompleted);
                    });
            var input = SigningInput.fromFile(AutogramDocument.build(new FileDocument(file), eFormAttributes),
                    signingParameters);
            autogram.batchSign(SigningJob.fromInput(input, responder, batch), batch.getBatchId());
        } catch (AutogramException e) {
            if (errors.get(file) != null || targetFiles.get(file) != null)
                throw e;
            errors.put(file, e);
            autogram.recordBatchSubmissionFailure(batch);
            if (!e.batchCanContinue()) autogram.finishBatch(batch);
            submitNextInteractive(autogram, batch, files, index + 1, targetPath, signingParameters,
                    eFormAttributes, targetFiles, errors, onCompleted);
        }
    }

    private void onAllFilesSigned(Batch batch) { // synchronized
        Logging.log("onAllFilesSigned " + batch.isAllProcessed() + " " + uiNotifiedOnAllFilesSigned);
        if (batch.isAllProcessed() && !uiNotifiedOnAllFilesSigned) {
            uiNotifiedOnAllFilesSigned = true;
            Logging.log(errors.values().stream().map(e -> e == null ? "" : e.toString()).toList());
            var result = new BatchUiResult(targetPath, targetFiles, errors);
            autogram.onDocumentBatchSaved(result);
        }
    }

    @Override
    public void onBatchStartFailure(AutogramException error) {
        autogram.onSigningFailed(error);
    }
}
