package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;

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
                var jobs = Arrays.stream(source.listFiles()).map(f -> SigningJob.buildFromFile(f, autogram, params.getTarget(), params.isForce())).toList();
                if(params.shouldCheckPDFACompliance()) {
                    jobs.forEach(autogram::checkPDFACompliance);
                }
                jobs.forEach(autogram::sign);
            } else {
                var job = SigningJob.buildFromFile(source, autogram, null, params.isForce());
                if (params.shouldCheckPDFACompliance()) {
                    autogram.checkPDFACompliance(job);
                }
                autogram.sign(job);
            }
        } catch (AutogramException e) {
            ui.showError(e);
        }
    }
}
