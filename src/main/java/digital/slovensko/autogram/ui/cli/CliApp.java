package digital.slovensko.autogram.ui.cli;

import com.google.common.io.Files;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.ui.SaveFileResponder;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class CliApp {
    public static void start(CliParameters params) {
        var ui = new CliUI();
        var autogram = params.getDriver() == null ? new Autogram(ui) : new Autogram(ui, () -> Collections.singletonList(params.getDriver()));

        if (params.getSource() == null) {
            throw new IllegalArgumentException("Source is not defined");
        }

        if (params.getTarget() != null) {
            if (params.getSource().isDirectory()) {
//                File targetDir = new File(params.getTarget());
//                if (!targetDir.exists() && !targetDir.mkdir()) {
//                    throw new IllegalArgumentException("Unable to create target directory");
//                }
            } else {
                File targetFile = new File(params.getTarget());
                if (!targetFile.getParentFile().exists()) {
                    throw new IllegalArgumentException("Invalid target path");
                }
            }
        }

        var shouldGenerateTarget = params.getTarget() == null;
        var targetPath = shouldGenerateTarget ? generateTargetPath(params.getSource()) : params.getTarget();

        File targetFile = new File(targetPath);
        if ((targetFile.isDirectory() != params.getSource().isDirectory()) && targetFile.exists() && !shouldGenerateTarget) {
            throw new IllegalArgumentException("Source and target incompatible file types");
        }

        if (!params.isForce() && !shouldGenerateTarget && targetFile.exists()) {
            throw new IllegalArgumentException(AutogramException.TARGET_ALREADY_EXISTS_EXCEPTION_MESSAGE);
        }

        try {
            var source = params.getSource();
            if (source.isDirectory()) {
                File targetDir = new File(targetPath);
                if (!targetDir.exists() && !targetDir.mkdir()) {
                    throw new IllegalArgumentException("Unable to create target directory");
                }
                var jobs = Arrays.stream(source.listFiles()).filter(f -> f.isFile()).map(f ->
                    SigningJob.buildFromFile(f, new SaveFileResponder(f, autogram, targetFile.getPath(), null,
                            params.isForce(), source.isDirectory()), params.shouldCheckPDFACompliance())
                ).toList();
                if (params.shouldCheckPDFACompliance()) {
                    jobs.forEach(job -> {
                        System.out.println("Checking PDF/A file compatibility for " + job.getDocument().getName());
                        autogram.checkPDFACompliance(job);
                    });
                }
                jobs.forEach(autogram::sign);
            } else {
                var job = SigningJob.buildFromFile(source, new SaveFileResponder(source, autogram, targetFile.getParent(), targetFile.getName(),
                        params.isForce(), source.isDirectory() || params.getTarget() == null), params.shouldCheckPDFACompliance());
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

    private static String generateTargetPath(File source) {
        var isSourceDirectory = source.isDirectory();
        if (isSourceDirectory) {
            return source.getPath() + "_signed";
        } else {
            var extension = source.getName().endsWith(".pdf") ? ".pdf" : ".asice";
            var directory = source.getParent();
            var name = Files.getNameWithoutExtension(source.getName()) + "_signed";
            return Paths.get(directory, name + extension).toString();
        }
    }
}
