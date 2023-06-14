package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CliApp {
    public static void start(CliParameters params) {
        var ui = new CliUI(params);
        var autogram = new Autogram(ui);

        if (params.getSource() == null) {
            throw new IllegalArgumentException("Source is defined");
        }

        try {
            var source = params.getSource();
            if (source.isDirectory()) {
                var jobs = Arrays.stream(source.listFiles()).map(f -> SigningJob.buildFromFile(f, autogram, params.getTarget(), params.isForce())).toList();
                autogram.signMany(jobs, params.shouldCheckPDFACompliance());
            } else {
                autogram.sign(SigningJob.buildFromFile(source, autogram, null, params.isForce()));
            }
        } catch (AutogramException e) {
            ui.showError(e);
        }
    }
}
