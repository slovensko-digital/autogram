package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

public class BatchModeGuiFileResponder extends BatchGuiFileResponder {

    public BatchModeGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory, boolean checkPDFACompliance, SignatureLevel pDFSignatureLevel, boolean signPDFAsPades, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        super(autogram, list, targetDirectory, checkPDFACompliance, pDFSignatureLevel, signPDFAsPades, isEn319132, tspSource, plainXmlEnabled);
    }

    public List<File> getFilesList() {
        return list;
    }

    public Path getTargetDirectory() {
        return targetPath.getTargetDirectory();
    }

    @Override
    protected void processFiles(Batch batch) {
        for (File file : list) {
            processFile(file, batch);
        }
    }

    private void processFile(File file, Batch batch) {
        initFileResult(file);
        try {
            var job = buildBatchJob(file, batch, () -> onAllFilesSigned(batch), error -> onAllFilesSigned(batch));
            autogram.batchSign(job, batch.getBatchId());
        } catch (AutogramException e) {
            handleFileSubmissionFailure(file, batch, e);
            autogram.onSigningFailed(e);
            onAllFilesSigned(batch);
        }
    }
}
