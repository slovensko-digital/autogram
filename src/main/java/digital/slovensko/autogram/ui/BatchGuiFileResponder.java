package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.ResponderInBatch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

public abstract class BatchGuiFileResponder extends BatchResponder {
    protected final Autogram autogram;
    protected final List<File> list;
    protected final Map<File, File> targetFiles = new HashMap<>();
    protected final Map<File, AutogramException> errors = new HashMap<>();
    protected boolean uiNotifiedOnAllFilesSigned = false;
    protected final TargetPath targetPath;
    protected final boolean checkPDFACompliance;
    protected final SignatureLevel pDFSignatureLevel;
    protected final boolean isEn319132;
    protected final TSPSource tspSource;
    protected final boolean plainXmlEnabled;

    public BatchGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory, boolean checkPDFACompliance, SignatureLevel pDFSignatureLevel, boolean signPDFAsPades, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        this.autogram = autogram;
        this.list = list;
        this.checkPDFACompliance = checkPDFACompliance;
        this.pDFSignatureLevel = pDFSignatureLevel;
        this.isEn319132 = isEn319132;
        this.targetPath = TargetPath.fromTargetDirectory(targetDirectory, signPDFAsPades);
        this.tspSource = tspSource;
        this.plainXmlEnabled = plainXmlEnabled;
    }

    @Override
    public void onBatchStartSuccess(Batch batch) {
        targetPath.mkdirIfDir();
        processFiles(batch);
    }

    protected abstract void processFiles(Batch batch);

    protected SigningJob buildBatchJob(File file, Batch batch, Runnable onSuccess, Consumer<AutogramException> onFailure) {
        var responder = new ResponderInBatch(new SaveFileFromBatchResponder(file, targetPath, targetFile -> {
            targetFiles.put(file, targetFile);
            Logging.log(batch.getProcessedDocumentsCount() + " / " + batch.getTotalNumberOfDocuments() + " signed " + file);
            onSuccess.run();
        }, error -> {
            Logging.log("Signing failed " + file + " all:" + batch.isAllProcessed());
            errors.put(file, error);
            onFailure.accept(error);
        }), batch);

        return SigningJob.buildFromFile(file, responder, checkPDFACompliance, pDFSignatureLevel, isEn319132, tspSource, plainXmlEnabled);
    }

    protected void initFileResult(File file) {
        targetFiles.put(file, null);
        errors.put(file, null);
    }

    protected void handleFileSubmissionFailure(File file, Batch batch, AutogramException error) {
        // If responder callbacks already set the error, the batch counters were already updated.
        if (errors.get(file) != null)
            return;

        errors.put(file, error);
        batch.onJobFailure();
        Logging.log("Signing failed before job submission " + file + " all:" + batch.isAllProcessed());
    }

    protected void abortRemainingFiles(Batch batch, AutogramException error) {
        for (File file : list) {
            if (targetFiles.containsKey(file))
                continue;

            initFileResult(file);
            errors.put(file, error);
            batch.onJobFailure();
        }

        onAllFilesSigned(batch);
    }

    protected void onAllFilesSigned(Batch batch) {
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
