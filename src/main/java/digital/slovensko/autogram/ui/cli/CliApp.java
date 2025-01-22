package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.SourceNotDefindedException;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.ui.SaveFileResponder;

import java.io.File;
import java.util.Arrays;
import java.util.Timer;

import org.apache.commons.cli.CommandLine;

public class CliApp {
    public static void start(CommandLine cmd) {
        Autogram autogram = null;
        try {
            var settings = CliSettings.fromCmd(cmd);
            var ui = new CliUI(settings);
            autogram = new Autogram(ui, settings);

            if (settings.getSource() == null)
                throw new SourceNotDefindedException();

            if (!settings.getSource().exists())
                throw new SourceDoesNotExistException();

            var targetPathBuilder = TargetPath.fromParams(settings);
            targetPathBuilder.mkdirIfDir();

            var source = settings.getSource();
            var sourceList = source.isDirectory() ? source.listFiles() : new File[] { source };

            var finalAutogram = autogram;
            var jobs = Arrays.stream(sourceList).filter(f -> f.isFile())
                    .map(f -> SigningJob.buildFromFile(f, new SaveFileResponder(f, finalAutogram, targetPathBuilder),
                            settings.isPdfaCompliance(), settings.getSignatureLevel(), settings.isEn319132(),
                            settings.getTspSource(), settings.isPlainXmlEnabled()))
                    .toList();
            if (settings.isPdfaCompliance()) {
                jobs.forEach(job -> {
                    System.out.println("Checking PDF/A file compatibility for " + job.getDocument().getName());
                    finalAutogram.checkPDFACompliance(job);
                });
            }

            ui.setJobsCount(jobs.size());
            jobs.forEach(autogram::sign);

            autogram.stopTokenSessionTimer();

        } catch (AutogramException e) {
            System.err.println(CliUI.parseError(e));
            if (autogram != null)
                autogram.stopTokenSessionTimer();
        }
    }
}
