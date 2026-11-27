package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import digital.slovensko.autogram.core.NetworkClients;
import digital.slovensko.autogram.core.NoBatch;
import digital.slovensko.autogram.core.ProxyConfiguration;
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
import java.util.concurrent.atomic.AtomicReference;

public class CliApp {
    public static void start(CommandLine cmd) {
        CliSettings settings;
        try {
            settings = CliSettings.fromCmd(cmd);
            NetworkClients.initialize(ProxyConfiguration.parse(settings.getProxyUrl()));
        } catch (AutogramException e) {
            System.err.println(CliUI.parseError(e));
            return;
        }

        sign(settings, new CliUI(settings));
    }

    /**
     * Signs the source file, or every file in the source folder.
     */
    static void sign(CliSettings settings, CliUI ui) {
        Autogram autogram = null;
        try {
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
            var documents = Arrays.stream(sourceList).filter(f -> f.isFile())
                    .map(f -> new Document(f, SigningInput.fromFile(AutogramDocument.buildFromFile(new FileDocument(f), EFormAttributes.build(parameters, true)), parameters)))
                    .toList();
            if (settings.isPdfaCompliance()) {
                documents.forEach(document -> {
                    var job = document.job(new NoBatch(), finalAutogram, targetPathBuilder);
                    System.out.println("Checking PDF/A file compatibility for " + job.getName());
                    finalAutogram.checkPDFACompliance(job);
                });
            }

            ui.setJobsCount(documents.size());
            if (documents.size() <= 1) {
                documents.forEach(document -> finalAutogram.startSigning(document.job(new NoBatch(), finalAutogram, targetPathBuilder)));
                return;
            }

            // one batch for the whole folder, so the signing PIN is entered once, like for several documents from a website
            var batch = startBatch(autogram, documents.size());
            if (batch == null)
                return; // no key, the UI told the user why

            var batchId = batch.getBatchId();
            for (var document : documents) {
                // e.g. a canceled PIN or a removed card ended it, the UI told the user why
                if (batch.isEnded())
                    break;

                var job = document.job(batch, autogram, targetPathBuilder);
                ui.announceSigning(job);
                autogram.batchSign(job, batchId);
            }
            // it ends by itself once all documents are signed
            autogram.endBatchSigning(batchId);

        } catch (AutogramException e) {
            System.err.println(CliUI.parseError(e));

        } finally {
            if (autogram != null)
                autogram.shutdown();
        }
    }

    private record Document(File file, SigningInput input) {
        SigningJob job(Batch batch, Autogram autogram, TargetPath targetPathBuilder) {
            return SigningJob.fromInput(input, new SaveFileResponder(file, autogram, targetPathBuilder), batch);
        }
    }

    /**
     * Starts a bulk batch, picking the key: the CLI does it all in this thread.
     *
     * @return the started batch, null if no key was picked
     */
    private static Batch startBatch(Autogram autogram, int documents) {
        var started = new AtomicReference<Batch>();
        var failed = new AtomicReference<AutogramException>();
        autogram.startBatchSigning(documents, new BatchResponder() {
            @Override
            public void onBatchStartSuccess(Batch batch) {
                started.set(batch);
            }

            @Override
            public void onBatchStartFailure(AutogramException error) {
                failed.set(error);
            }
        });
        if (failed.get() != null)
            throw failed.get();

        return started.get();
    }
}
