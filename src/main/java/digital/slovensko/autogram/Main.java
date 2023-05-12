package digital.slovensko.autogram;

import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.core.CliParametersParser;
import digital.slovensko.autogram.ui.gui.GUIApp;
import digital.slovensko.autogram.ui.cli.CliApp;
import javafx.application.Application;

import java.util.Arrays;

import static java.util.Objects.requireNonNullElse;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting with args: " + Arrays.toString(args));

        CliParameters cliParameters = CliParametersParser.parse(args);
        if (cliParameters.isCli()) {
            CliApp.start(cliParameters);
        } else {
            Application.launch(GUIApp.class, args);
        }
    }

    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }
}
