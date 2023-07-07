package digital.slovensko.autogram.ui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.util.Logging;

public class BatchSaveFileResponder extends BatchResponder {
    private final Autogram autogram;
    private final List<File> list;
    // private List<SigningJob> jobs = new ArrayList<SigningJob>();
    private Map<File, File> targetFiles = new HashMap<File, File>();
    private boolean uiNotifiedOnAllFilesSigned = false;

    public BatchSaveFileResponder(Autogram autogram, List<File> list) {
        this.autogram = autogram;
        this.list = list;
    }

    @Override
    public void onBatchStartSuccess(Batch batch) {
        var targetPath = TargetPath.fromBatchSource(list.get(0).toPath());
        targetPath.mkdirIfDir();
        for (File file : list) {
            targetFiles.put(file, null);
            Logging.log("1 Signing " + file.toString());
            var responder = new SaveFileFromBatchResponder(file, autogram, targetPath, (File targetFile) -> {
                targetFiles.put(file, targetFile);
                Logging.log(batch.getProcessedDocumentsCount() + " / " + batch.getTotalNumberOfDocuments()
                        + " signed " + file.toString());
                var isEveryFileSigned = targetFiles.values().stream().allMatch(b -> b != null);
                if (batch.isAllProcessed() && isEveryFileSigned) {
                    onAllFilesSigned();
                }
            });
            var job = SigningJob.buildFromFileBatch(file, autogram, responder);
            autogram.batchSign(job, batch.getBatchId());
            Logging.log("Started batchSigning " + file.toString() + "for job " + job.hashCode());
            Logging.log("Ended batchSigning " + file.toString() + "for job " + job.hashCode());
        }

    }

    private void onAllFilesSigned() { // synchronized
        if (!uiNotifiedOnAllFilesSigned) {
            uiNotifiedOnAllFilesSigned = true;
            autogram.onDocumentBatchSaved(targetFiles.values().stream().toList());
        }
    }

    @Override
    public void onBatchStartFailure(AutogramException error) {
        autogram.onSigningFailed(error);
    }

    @Override
    public void onBatchSignFailed(AutogramException error) {

    }

    private File getTargetDirectory() {
        var baseDir = list.get(0).getParentFile().toPath();
        var i = 0;
        while (true) {
            var dirName = "signed" + (i == 0 ? "" : " (" + i + ")");
            var targetDirectory = baseDir.resolve(dirName).toFile();
            if (targetDirectory.exists()) {
                i++;
            } else {
                targetDirectory.mkdirs();
                return targetDirectory;
            }
        }
    }
}
