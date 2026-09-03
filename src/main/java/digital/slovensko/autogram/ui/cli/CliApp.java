package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TargetPath;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.core.errors.SourceNotDefinedException;
import digital.slovensko.autogram.ui.SaveFileResponder;
import eu.europa.esig.dss.model.DSSException;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.Arrays;

public class CliApp {
    public static int start(CommandLine cmd) {
        Autogram autogram = null;
        try {
            var settings = CliSettings.fromCmd(cmd);
            var ui = new CliUI(settings);
            autogram = new Autogram(ui, settings);

            if (settings.isListKeys()) {
                var driver = ui.pickTokenDriver(autogram.getAvailableDrivers());
                ui.printSigningKeys(autogram.getSigningKeys(driver));
                return 0;
            }

            if (settings.getSource() == null)
                throw new SourceNotDefinedException();

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
            return 0;

        } catch (AutogramException e) {
            System.err.println(CliUI.parseError(e));
            return 1;
        } catch (DSSException e) {
            return reportFailure(AutogramException.createFromDSSException(e));
        } catch (Exception e) {
            return reportFailure(e);
        } finally {
            if (autogram != null)
                autogram.shutdown();
        }
    }

    static int reportFailure(Exception e) {
        if (e instanceof AutogramException autogramException)
            System.err.println(CliUI.parseError(autogramException));
        else
            e.printStackTrace(System.err);
        return 1;
    }
}
