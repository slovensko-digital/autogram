package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;

import java.io.File;

public class CliApp {
    public static void start(CliParameters cliParameters) {
        var ui = new CliUI(cliParameters.getDriver());
        var autogram = new Autogram(ui);

        autogram.checkForUpdate();

        if (cliParameters.getSourceDirectory() == null && cliParameters.getSourceFile() == null) {
            throw new IllegalArgumentException("Neither source file nor source directory is defined");
        }

        try {
            if (cliParameters.getSourceDirectory() != null) {
                signDocuments(ui, autogram, cliParameters.getSourceDirectory(), cliParameters.getTargetDirectory(), cliParameters.isRewriteFile());
            }

            if (cliParameters.getSourceFile() != null) {
                signDocument(ui, autogram, cliParameters.getSourceFile(), cliParameters.getTargetDirectory(), cliParameters.isRewriteFile());
            }
        } catch (AutogramException e) {
            ui.showError(e);
        }
    }

    private static void signDocuments(CliUI ui, Autogram autogram, File sourceDirectory, String targetDirectory, boolean rewriteFile) {
        for (File file : sourceDirectory.listFiles()) {
            signDocument(ui, autogram, file, targetDirectory, rewriteFile);
        }
    }

    private static void signDocument(CliUI ui, Autogram autogram, File file, String targetDirectory, boolean rewriteFile) {
        ui.sign(file, autogram, targetDirectory, rewriteFile);
    }
}
