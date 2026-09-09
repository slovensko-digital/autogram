package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

public class BatchModeGuiFileResponder extends BatchGuiFileResponder {

    public BatchModeGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory, boolean checkPDFACompliance, SignatureLevel pDFSignatureLevel, boolean signPDFAsPades, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        super(autogram, list, targetDirectory, checkPDFACompliance, pDFSignatureLevel, signPDFAsPades, isEn319132, tspSource, plainXmlEnabled);
    }

    @Override
    protected void processFiles(Batch batch) {
        for (File file : list) {
            if (!processFile(file, batch))
                break;
        }
    }

    private boolean processFile(File file, Batch batch) {
        initFileResult(file);
        try {
            var job = buildBatchJob(file, batch, () -> onAllFilesSigned(batch), _ -> onAllFilesSigned(batch));
            autogram.batchSign(job, batch.getBatchId());
            return true;
        } catch (AutogramException e) {
            handleFileSubmissionFailure(file, batch, e);
            autogram.onSigningFailed(e);
            if (e.batchCanContinue()) {
                onAllFilesSigned(batch);
                return true;
            } else {
                abortRemainingFiles(batch, e);
                return false;
            }
        }
    }
}
