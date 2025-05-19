package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class BatchGuiFileResponder extends BatchResponder {
    private final Autogram autogram;
    private final List<File> list;
    private final Map<File, File> targetFiles = new HashMap<>();
    private final Map<File, AutogramException> errors = new HashMap<>();
    private boolean uiNotifiedOnAllFilesSigned = false;
    private final TargetPath targetPath;
    private final boolean checkPDFACompliance;
    private final SignatureLevel pDFSignatureLevel;
    private final boolean isEn319132;
    private final TSPSource tspSource;
    private final boolean plainXmlEnabled;

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
        try {
            targetPath.mkdirIfDir();
        } catch (AutogramException e) {
            autogram.onSigningFailed(e);
            throw e;
        }

        for (File file : list) {
            try {
                targetFiles.put(file, null);
                errors.put(file, null);
                var responder = new ResponderInBatch(new SaveFileFromBatchResponder(file, targetPath, (File targetFile) -> {
                    targetFiles.put(file, targetFile);
                    Logging.log(batch.getProcessedDocumentsCount() + " / " + batch.getTotalNumberOfDocuments() + " signed " + file.toString());
                    onAllFilesSigned(batch);
                }, (AutogramException error) -> {
                    Logging.log("Signing failed " + file.toString() + " all:" + batch.isAllProcessed());
                    errors.put(file, error);
                    onAllFilesSigned(batch);
                }), batch);

                var job = SigningJob.buildFromFile(file, responder, checkPDFACompliance, pDFSignatureLevel, isEn319132, tspSource, plainXmlEnabled);
                autogram.batchSign(job, batch.getBatchId());
            } catch (AutogramException e) {
                autogram.onSigningFailed(e);

                break;
            }
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
