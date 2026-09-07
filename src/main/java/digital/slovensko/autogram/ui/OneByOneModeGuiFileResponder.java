package digital.slovensko.autogram.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.BatchCanceledException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

public class OneByOneModeGuiFileResponder extends BatchGuiFileResponder {
    private int currentFileIndex = 0;

    public OneByOneModeGuiFileResponder(Autogram autogram, List<File> list, Path targetDirectory, boolean checkPDFACompliance, SignatureLevel pDFSignatureLevel, boolean signPDFAsPades, boolean isEn319132, TSPSource tspSource, boolean plainXmlEnabled) {
        super(autogram, list, targetDirectory, checkPDFACompliance, pDFSignatureLevel, signPDFAsPades, isEn319132, tspSource, plainXmlEnabled);
    }

    @Override
    protected void processFiles(Batch batch) {
        processNextFile(batch);
    }

    private void processNextFile(Batch batch) {
        if (currentFileIndex < list.size()) {
            File file = list.get(currentFileIndex);
            currentFileIndex++;
            processFile(file, batch);
        } else {
            onAllFilesSigned(batch);
        }
    }

    private void processFile(File file, Batch batch) {
        initFileResult(file);
        try {
            var currentFileNumber = currentFileIndex;
            var job = buildBatchJob(file, batch, () -> processNextFile(batch), error -> {
                if (!error.batchCanContinue()) {
                    abortRemainingFiles(batch, new BatchCanceledException());
                    return;
                }
                processNextFile(batch);
            });
            job.setSkipActions(
                    () -> skipCurrentFile(file, batch),
                    () -> skipRemainingFiles(file, batch));
            job.setDialogTitleSuffix(String.format("(%d z %d)", currentFileNumber, list.size()));
            batch.addJob(batch.getBatchId());
            autogram.sign(job);
        } catch (AutogramException e) {
            handleFileSubmissionFailure(file, batch, e);
            if (!e.batchCanContinue()) {
                abortRemainingFiles(batch, new BatchCanceledException());
                return;
            }
            processNextFile(batch);
        }
    }

    private void skipCurrentFile(File file, Batch batch) {
        errors.put(file, new BatchCanceledException());
        batch.onJobFailure();
        processNextFile(batch);
    }

    private void skipRemainingFiles(File currentFile, Batch batch) {
        skipCurrentFileWithoutAdvancing(currentFile, batch);
        while (currentFileIndex < list.size()) {
            var file = list.get(currentFileIndex++);
            initFileResult(file);
            errors.put(file, new BatchCanceledException());
            batch.addJob(batch.getBatchId());
            batch.onJobFailure();
        }
        onAllFilesSigned(batch);
    }

    private void skipCurrentFileWithoutAdvancing(File file, Batch batch) {
        errors.put(file, new BatchCanceledException());
        batch.onJobFailure();
    }
}
