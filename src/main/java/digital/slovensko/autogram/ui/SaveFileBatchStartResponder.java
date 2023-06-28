package digital.slovensko.autogram.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.BatchStartResponder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;

public class SaveFileBatchStartResponder extends BatchStartResponder {
    private final Autogram autogram;
    private final List<File> list;
    private List<SigningJob> jobs = new ArrayList<SigningJob>();

    public SaveFileBatchStartResponder(Autogram autogram, List<File> list) {
        this.autogram = autogram;
        this.list = list;
    }

    @Override
    public void onBatchStartSuccess(String batchId) {
        var targetDirectory = getTargetDirectory();
        for (File file : list) {
            var job = SigningJob.buildFromFileBatch(file, autogram, targetDirectory);
            jobs.add(job);
            autogram.batchSign(job, batchId);
        }

        autogram.onDocumentSaved(list);
    }

    @Override
    public void onBatchStartFailure(AutogramException error) {
        autogram.onSigningFailed(error);
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
