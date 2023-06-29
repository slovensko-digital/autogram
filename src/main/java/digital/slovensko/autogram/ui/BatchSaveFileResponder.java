package digital.slovensko.autogram.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;

public class BatchSaveFileResponder extends BatchResponder {
    private final Autogram autogram;
    private final List<File> list;
    // private List<SigningJob> jobs = new ArrayList<SigningJob>();
    private Map<File, File> targetFiles = new HashMap<File, File>();

    public BatchSaveFileResponder(Autogram autogram, List<File> list) {
        this.autogram = autogram;
        this.list = list;
    }

    @Override
    public void onBatchStartSuccess(Batch batch) {
        var targetDirectory = getTargetDirectory();
        ReentrantLock lock = new ReentrantLock();

        for (File file : list) {
            lock.lock();
            targetFiles.put(file, null);
            var responder = new SaveFileFromBatchResponder(file, autogram, targetDirectory, (File targetFile) -> {
                targetFiles.put(file, targetFile);
                System.out.println(batch.getProcessedDocumentsCount() + " / " + batch.getTotalNumberOfDocuments());
                var isEveryFileSigned = targetFiles.values().stream().allMatch(b -> b != null);
                if (batch.isAllProcessed() && isEveryFileSigned) {
                    autogram.onDocumentBatchSaved(targetFiles.values().stream().toList());
                }
                lock.unlock();
            });
            var job = SigningJob.buildFromFileBatch(file, autogram, targetDirectory, responder);
            autogram.batchSign(job, batch.getBatchId());
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
