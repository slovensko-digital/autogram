package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.errors.AutogramException;

public class CliApp {
    public static void start(CliParameters cliParameters) {
        var ui = new CliUI(cliParameters.getDriver());
        var autogram = new Autogram(ui);

        if (cliParameters.getSourceDirectory() == null && cliParameters.getSourceFile() == null) {
            throw new IllegalArgumentException("Neither source file nor source directory is defined");
        }

        try {
            if (cliParameters.getSourceDirectory() != null) {
                ui.sign(cliParameters.getSourceDirectory(), autogram, cliParameters.getTargetDirectory(), cliParameters.isRewriteFile());
            }

            if (cliParameters.getSourceFile() != null) {
                ui.sign(cliParameters.getSourceFile(), autogram, cliParameters.getTargetDirectory(), cliParameters.isRewriteFile());
            }
        } catch (AutogramException e) {
            ui.showError(e);
        }
    }
}
