package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.cli.CliApp;
import digital.slovensko.autogram.ui.gui.GUIApp;
import javafx.application.Application;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        addOption("d", "driver", true, "PCKS driver name for signing. Supported values: eid, cz_eid, secure_store, monet, gemalto, keystore, custom_pkcs11 (requires valid path within pkcs11-driver-path option).").
        addOption(null, "keystore", true, "Absolute path to a keystore file that can be used for signing.").
        addOption(null, "slot-id", true, "Slot ID for PKCS11 driver. If not specified, first available slot is used.").
        addOption(null, "pdf-level", true, "PDF signature level. Supported values: PAdES_BASELINE_B (default), XAdES_BASELINE_B, CAdES_BASELINE_B.").
        addOption(null, "en319132", false, "Sign according to EN 319 132 or EN 319 122.").
        addOption(null, "tsa-server", true, "Url of TimeStamp Authority server that should be used for timestamping in signature level BASELINE_T. If provided, BASELINE_T signatures are made.").
        addOption(null, "plain-xml", false, "Enable signing plain (non-slovak-eform) XML files.").
        addOption(null, "pkcs11-driver-path", true, "Absolute path to a file with custom PKCS11 driver.");

    private static final Set<String> OPTIONS_WITH_VALUES = options.getOptions().stream()
            .filter(Option::hasArg)
            .flatMap(option -> Stream.of(option.getOpt(), option.getLongOpt()))
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

    public static void start(String[] args) {
        try {
            var resolvedArgs = resolveArgs(args);
            CommandLine cmd = new DefaultParser().parse(options, resolvedArgs);

            if (cmd.hasOption("h")) {
                printHelp();
            } else if (cmd.hasOption("u")) {
                printUsage();
            } else if (cmd.hasOption("c")) {
                CliApp.start(cmd);
            } else {
                // --url starts a server instance that holds no single-instance lock or
                // socket, so a GUI instance started later becomes the primary instead.
                // This is intentional: the server is a separate integration surface and
                // must not be a forwarding target for the desktop single-instance flow.
                if (!cmd.hasOption("url")) {
                    if (!SingleInstanceManager.start(GUIApp.getFilesToOpen()))
                        return;
                }
                Application.launch(GUIApp.class, resolvedArgs);
            }
        } catch (ParseException e) {
            System.err.println("Unable to parse program args");
            System.err.println(e);
        }
    }

    static String[] resolveArgs(String[] args) {
        var filesToOpen = new ArrayList<String>();
        var resolved = new ArrayList<String>();

        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if (isValueOfOption(args, i)) {
                resolved.add(arg);
            } else if (arg.startsWith("autogram://")) {
                resolved.add("--url=" + arg);
            } else if (arg.startsWith("file://")) {
                filesToOpen.add(decodeFileUri(arg));
            } else if (new File(arg).isFile()) {
                filesToOpen.add(arg);
            } else {
                resolved.add(arg);
            }
        }

        if (!filesToOpen.isEmpty()) {
            GUIApp.setFilesToOpen(filesToOpen);
        }

        return resolved.toArray(new String[0]);
    }

    private static String decodeFileUri(String uri) {
        try {
            return Path.of(URI.create(uri)).toString();
        } catch (IllegalArgumentException e) {
            return uri.substring(7);
        }
    }

    private static boolean isValueOfOption(String[] args, int index) {
        if (index == 0)
            return false;

        var previous = args[index - 1];
        if (previous.contains("="))
            return false;

        return OPTIONS_WITH_VALUES.contains(normalizeOptionName(previous));
    }

    private static String normalizeOptionName(String arg) {
        var name = arg;
        while (name.startsWith("-"))
            name = name.substring(1);
        return name;
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
                autogram --cli -s target/file-example.pdf -d eid --tsa-server http://timestamp.sectigo.com/qualified
                autogram --cli -s target/file-example.pdf -d eid --tsa-server "http://tsa.belgium.be/connect,http://timestamp.sectigo.com/qualified"
                """;
        final PrintWriter pw = new PrintWriter(System.out);
        formatter.printUsage(pw, 80, syntax);
        pw.flush();
    }
}
