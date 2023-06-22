package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.SaveFileResponder;

import java.util.Arrays;
import java.util.Collections;

public class CliApp {
    public static void start(CliParameters params) {
        var ui = new CliUI();
        var autogram = params.getDriver() == null ? new Autogram(ui) : new Autogram(ui, () -> Collections.singletonList(params.getDriver()));

        if (params.getSource() == null) {
            throw new IllegalArgumentException("Source is not defined");
        }

        try {
            var source = params.getSource();
            if (source.isDirectory()) {
                var jobs = Arrays.stream(source.listFiles()).map(f ->
                    SigningJob.buildFromFile(f, new SaveFileResponder(f, autogram, params.getTarget(), params.isForce()), params.shouldCheckPDFACompliance())
                ).toList();
                if (params.shouldCheckPDFACompliance()) {
                    jobs.forEach(job -> {
                        System.out.println("Checking PDF/A file compatibility for " + job.getDocument().getName());
                        autogram.checkPDFACompliance(job);
                    });
                }
                jobs.forEach(autogram::sign);
            } else {
                var job = SigningJob.buildFromFile(source, new SaveFileResponder(source, autogram, params.getTarget(), params.isForce()), params.shouldCheckPDFACompliance());
                if (params.shouldCheckPDFACompliance()) {
                    System.out.println("Checking PDF/A file compatibility for " + job.getDocument().getName());
                    autogram.checkPDFACompliance(job);
                }
                autogram.sign(job);
            }
        } catch (AutogramException e) {
            ui.showError(e);
        }
    }
}
