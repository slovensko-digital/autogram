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
            var files = Arrays.stream(sourceList).filter(File::isFile).toList();

            ui.setJobsCount(files.size());
            var parameters = settings.getSigningParameters();
            var eFormAttributes = EFormAttributes.build(parameters, true);
            for (var file : files) {
                var input = SigningInput.fromFile(
                        AutogramDocument.build(new FileDocument(file), eFormAttributes), parameters);
                var job = SigningJob.fromInput(input);
                if (settings.isPdfaCompliance()) {
                    System.out.println("Checking PDF/A file compatibility for " + job.getName());
                    autogram.checkPDFACompliance(job);
                }
                autogram.startSigning(job, new SaveFileResponder(file, autogram, targetPathBuilder));
            }

        } catch (AutogramException e) {
            System.err.println(CliUI.parseError(e));

        } finally {
            if (autogram != null)
                autogram.shutdown();
        }
    }
}
