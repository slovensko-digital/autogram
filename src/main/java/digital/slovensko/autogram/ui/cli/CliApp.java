package digital.slovensko.autogram.ui.cli;

import com.google.common.io.Files;
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

        String sourceExtension = Files.getFileExtension(params.getSource().getName());
        String targetExtension = params.getTarget() != null ? Files.getFileExtension(params.getTarget()) : "";

        if (params.getSource().isDirectory()) {
            if (params.getTarget() != null && !targetExtension.equals("")) {
                throw new IllegalArgumentException("If source is directory, target must also be a directory");
            }
        } else {
            if (!targetExtension.equals("") && !targetExtension.equals("pdf") && !targetExtension.equals("asice")) {
                throw new IllegalArgumentException("Unsupported target file extension");
            }

            if (!targetExtension.equals("") &&
                    ((sourceExtension.equals("pdf") && !targetExtension.equals("pdf")) ||
                        (targetExtension.equals("pdf") && !sourceExtension.equals("pdf")))) {
                throw new IllegalArgumentException("Source and target file types are not compatible");
            }
        }

        try {
            var source = params.getSource();
            if (source.isDirectory()) {
                var jobs = Arrays.stream(source.listFiles()).filter(f -> f.isFile()).map(f ->
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
