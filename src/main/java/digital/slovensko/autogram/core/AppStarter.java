package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.cli.CliApp;
import digital.slovensko.autogram.ui.gui.GUIApp;
import javafx.application.Application;
import org.apache.commons.cli.*;

import java.io.PrintWriter;

public class AppStarter {
    private static final Options options = new Options().
        addOptionGroup(new OptionGroup().
            addOption(new Option(null, "url", true, "Start in GUI mode with API server listening on given port and protocol (HTTP/HTTPS). Application starts minimised when is not empty.")).
            addOption(new Option("c", "cli", false, "Run application in CLI mode."))
        ).
        addOption("h", "help", false, "Print this command line help.").
        addOption("u", "usage", false, "Print usage examples.").
        addOption("s", "source", true, "Source file or directory of files to sign.").
        addOption("t", "target", true, "Target file or directory for signed files. Type (file/directory) must match the source.").
        addOption("f", "force", false, "Overwrite existing file(s).").
        addOption(null, "pdfa", false, "Check PDF/A compliance before signing.").
        addOption(null, "parents", false, "Create all parent directories for target if needed.").
        addOption("d", "driver", true, "PCKS driver name for signing. Supported values: eid, secure_store, monet, gemalto, keystore.").
        addOption(null, "keystore", true, "Absolute path to a keystore file that can be used for signing.").
        addOption(null, "slot-id", true, "Slot ID for PKCS11 driver. If not specified, first available slot is used.").
        addOption(null, "pdf-level", true, "PDF signature level. Supported values: PAdES_BASELINE_B (default), XAdES_BASELINE_B, CAdES_BASELINE_B.").
        addOption(null, "en319132", false, "Sign according to EN 319 132 or EN 319 122.").
        addOption(null, "tsa-server", true, "Url of TimeStamp Authority server that should be used for timestamping in signature level BASELINE_T. If provided, BASELINE_T signatures are made.").
        addOption(null, "plain-xml", false, "Enable signing plain (non-slovak-eform) XML files.");

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
        final String syntax = "autogram";
        final String footer = """

                In CLI mode, signed files are saved with the same name as the source file, but with the suffix "_signed" if no target is specified. If the source is a directory, the target must also be a directory. If the source is a file, the target must also be a file. If the source is a driectory and no target is specified, a target directory is created with the same name as the source directory, but with the suffix "_signed".

                If no target is specified and generated target name already exists, number is added to the target's name suffix if --force is not enabled. For example, if the source is "file.pdf" and the target is not specified, the target will be "file_signed.pdf". If the target already exists, the target will be "file_signed (1).pdf". If that target already exists, the target will be "file_signed (2).pdf", and so on.

                If --force is enabled, the target will be overwritten if it already exists.

                If target is specified with missing parent directories, they are created onyl if --parents is enabled. Otherwise, the signing fails. For example, if the source is "file.pdf" and the target is "target/file_signed.pdf", the target directory "target" must exist. If it does not exist, the signing fails. If --parents is enabled, the target directory "target" is created if it does not exist.
                """;

        formatter.printHelp(80, syntax, "", options, footer, true);
    }

    public static void printUsage() {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = """
                autogram [options]
                autogram --url=http://localhost:32700
                autogram --cli [options]
                autogram --cli -s target/directory-example/file-example.pdf -t target/output-example/out-example.pdf
                autogram --cli -s target/directory-example -t target/output-example -f
                autogram --cli -s target/directory-example -t target/non-existent-dir/output-example --parents
                autogram --cli -s target/directory-example/file-example.pdf -pdfa
                autogram --cli -s target/directory-example/file-example.pdf -d eid
                autogram --cli -s target/file-example.pdf -d eid --tsa-server http://tsa.izenpe.com
                """;
        final PrintWriter pw = new PrintWriter(System.out);
        formatter.printUsage(pw, 80, syntax);
        pw.flush();
    }
}
