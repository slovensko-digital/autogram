package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.SaveFileResponder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class CliApp {
    public static void start(CliParameters params) {
        var ui = new CliUI();
        var autogram = params.getDriver() == null ? new Autogram(ui)
                : new Autogram(ui, () -> Collections.singletonList(params.getDriver()));

        if (params.getSource() == null) {
            throw new IllegalArgumentException("Source is not defined");
        }

        if (params.getTarget() != null && params.getSource().isFile()
                && !new File(params.getTarget()).getParentFile().exists()) {
            throw new IllegalArgumentException("Invalid target path");
        }

        var targetPathBuilder = TargetPath.fromParams(params);
        targetPathBuilder.mkdirIfDir();

        try {
            var source = params.getSource();
            var sourceList = source.isDirectory() ? source.listFiles() : new File[] { source };
            var jobs = Arrays
                    .stream(sourceList).filter(f -> f.isFile()).map(f -> SigningJob.buildFromFile(f,
                            new SaveFileResponder(f, autogram, targetPathBuilder), params.shouldCheckPDFACompliance()))
                    .toList();
            if (params.shouldCheckPDFACompliance()) {
                jobs.forEach(job -> {
                    System.out.println("Checking PDF/A file compatibility for " + job.getDocument().getName());
                    autogram.checkPDFACompliance(job);
                });
            }
            jobs.forEach(autogram::sign);
        } catch (AutogramException e) {
            ui.showError(e);
        }
    }
}
