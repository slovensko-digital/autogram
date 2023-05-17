package digital.slovensko.autogram;

import digital.slovensko.autogram.core.CliManager;
import digital.slovensko.autogram.core.CliParameters;
import digital.slovensko.autogram.ui.gui.GUIApp;
import digital.slovensko.autogram.ui.cli.CliApp;
import javafx.application.Application;
import org.apache.commons.cli.*;

import java.util.Arrays;

import static java.util.Objects.requireNonNullElse;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting with args: " + Arrays.toString(args));

        CliManager cliManager = new CliManager();
        try {
            CommandLine cmd = cliManager.parse(args);
            
            if (cmd.hasOption("h")) {
                cliManager.printHelp();
            } else if (cmd.hasOption("u")) {
                cliManager.printUsage();
            } else if (cmd.hasOption("c") && Boolean.valueOf(cmd.getOptionValue("c"))) {
                CliApp.start(new CliParameters(cmd));
            } else {
                Application.launch(GUIApp.class, args);
            }
        } catch (ParseException e) {
            System.err.println("Unable to parse program args");
            System.err.println(e);
        }
    }

    public static String getVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }
}
