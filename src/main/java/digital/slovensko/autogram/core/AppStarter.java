package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.ui.cli.CliApp;
import digital.slovensko.autogram.ui.gui.GUIApp;
import javafx.application.Application;
import org.apache.commons.cli.*;

import java.io.PrintWriter;

public class AppStarter {
    private static final Options options = new Options().
        addOptionGroup(new OptionGroup().
            addOption(new Option(null, "url", true, "Start in GUI mode, with HTTP API server listening based on parameters. Application starts minimised when is not empty.")).
            addOption(new Option("c", "cli", false, "Run application in CLI mode."))
        ).
        addOption("h", "help", false, "Print this command line help.").
        addOption("u", "usage", false, "Print usage tips.").
        addOption("s", "source", true, "Source file or directory of files to sign.").
        addOption("t", "target", true, "Target file or directory for signed files.").
        addOption("f", "force", false, "Overwrite existing file(s).").
        addOption(null, "pdfa", false, "Check PDF/A compliance before signing.").
        addOption("d", "driver", true, "PCKS driver for signing. Supported drivers: eid, secure_store, monet, gemalto.");

    public static void start(String[] args) {
        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp();
            } else if (cmd.hasOption("u")) {
                printUsage();
            } else if (cmd.hasOption("c")) {
                CliApp.start(cmd);
            } else {
                Application.launch(GUIApp.class, args);
            }
        } catch (ParseException e) {
            System.err.println("Unable to parse program args");
            System.err.println(e);
        }
    }

    public static void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "autogram [options]";
        formatter.printHelp(syntax, options);
    }

    public static void printUsage() {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = """
                autogram [options]
                autogram --url=http://localhost:32700
                autogram --cli [options]
                autogram --cli -s target/directory-example/file-example.pdf -t target/output-example/out-example.pdf
                autogram --cli -s target/directory-example -t target/output-example -f
                autogram --cli -s target/directory-example/file-example.pdf -pdfa
                autogram --cli -s target/directory-example/file-example.pdf -d eid
                """;
        final PrintWriter pw = new PrintWriter(System.out);
        formatter.printUsage(pw, 80, syntax);
        pw.flush();
    }
}
