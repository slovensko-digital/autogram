package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.DefaultDriverDetector;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.SourceNotDefindedException;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.ui.SaveFileResponder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;

public class CliApp {
    public static void start(CommandLine cmd) {
        var ui = new CliUI();

        try {
            var params = new CliParameters(cmd);
            var autogram = new Autogram(ui, false, params.getDriver() != null ?
                        () -> Collections.singletonList(params.getDriver())
                        : new DefaultDriverDetector("", false),
                    params.getSlotId(), params.getTspSource());

            if (params.getSource() == null)
                throw new SourceNotDefindedException();

            if (!params.getSource().exists())
                throw new SourceDoesNotExistException();

            var targetPathBuilder = TargetPath.fromParams(params);
            targetPathBuilder.mkdirIfDir();

            var source = params.getSource();
            var sourceList = source.isDirectory() ? source.listFiles() : new File[] { source };
            var jobs = Arrays.stream(sourceList).filter(f -> f.isFile())
                    .map(f -> SigningJob.buildFromFile(f, new SaveFileResponder(f, autogram, targetPathBuilder),
                            params.shouldCheckPDFACompliance(), params.pdfSignatureLevel(), params.shouldSignAsEn319132(),
                            params.getTspSource()))
                    .toList();
            if (params.shouldCheckPDFACompliance()) {
                jobs.forEach(job -> {
                    System.out.println("Checking PDF/A file compatibility for " + job.getDocument().getName());
                    autogram.checkPDFACompliance(job);
                });
            }

            ui.setJobsCount(jobs.size());

            jobs.forEach(autogram::sign);

        } catch (AutogramException e) {
            ui.showError(e);
        }
    }
}
