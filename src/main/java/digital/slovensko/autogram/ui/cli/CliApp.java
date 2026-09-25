package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.dto.AutogramDocument;
import digital.slovensko.autogram.core.dto.SigningInput;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.core.errors.SourceNotDefinedException;
import digital.slovensko.autogram.ui.SaveFileResponder;
import eu.europa.esig.dss.model.FileDocument;

import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.Arrays;

public class CliApp {
    public static void start(CommandLine cmd) {
        Autogram autogram = null;
        try {
            var settings = CliSettings.fromCmd(cmd);
            var ui = new CliUI(settings);
            autogram = new Autogram(ui, settings);

            if (settings.getSource() == null)
                throw new SourceNotDefinedException();

            if (!settings.getSource().exists())
                throw new SourceDoesNotExistException();

            var targetPathBuilder = TargetPath.fromParams(settings);
            targetPathBuilder.mkdirIfDir();

            var source = settings.getSource();
            var sourceList = source.isDirectory() ? source.listFiles() : new File[] { source };

            var finalAutogram = autogram;
            var parameters = settings.getSigningParameters();
            var jobs = Arrays.stream(sourceList).filter(f -> f.isFile())
                        .map(f -> SigningJob.fromInput(
                            SigningInput.fromFile(AutogramDocument.buildFromFile(new FileDocument(f), EFormAttributes.build(parameters, true)), parameters),
                            new SaveFileResponder(f, finalAutogram, targetPathBuilder)))
                    .toList();
            if (settings.isPdfaCompliance()) {
                jobs.forEach(job -> {
                    System.out.println("Checking PDF/A file compatibility for " + job.getName());
                    finalAutogram.checkPDFACompliance(job);
                });
            }

            ui.setJobsCount(jobs.size());
            jobs.forEach(autogram::sign);

        } catch (AutogramException e) {
            System.err.println(CliUI.parseError(e));

        } finally {
            if (autogram != null)
                autogram.shutdown();
        }
    }
}
