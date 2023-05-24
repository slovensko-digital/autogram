package digital.slovensko.autogram.core;

import org.apache.commons.cli.*;

import java.io.PrintWriter;

public class CliManager {

    private Options options;

    public CliManager() {
        this.options = new Options();
        options.addOption("h", "help", false, "Displays the help message, providing information on how to use the application and its available options.");
        options.addOption("u", "usage", false, "This argument provides guidance to users on how to properly execute the application, including the available options and their corresponding arguments.");
        options.addOption("url", "url", true, "Specifies the URL to be used by the application. This option allows you to provide a custom URL for the application to interact with.");
        options.addOption("sd", "sourceDirectory", true, "Specifies the source directory for the application. This option sets the directory from which the application should read the necessary files for signing.");
        options.addOption("td", "targetDirectory", true, "Specifies the target directory for the application. This option determines the directory where the application should output or save the signed files.");
        options.addOption("sf", "sourceFile", true, "Specifies the source file for the application. This option allows you to provide a specific file that the application should sign.");
        options.addOption("rf", "rewriteFile", false, "Specifies the file to be rewritten by the application. This option instructs the application to modify or overwrite the content of file if file with the same name exists.");
        options.addOption("c", "cli", false, "Specifies the CLI (Command Line Interface) to be used by the application. This option allows you to specify mode of application. If not set GUI application starts.");
        options.addOption("d", "driver", true, "Specifies the driver for the application. This option sets the driver for signing.");
    }

    public CommandLine parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    public void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "java -jar Autogram.jar [options]";
        formatter.printHelp(syntax, options);
    }

    public void printUsage() {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "java -jar Autogram.jar [options]";
        final PrintWriter pw  = new PrintWriter(System.out);
        formatter.printUsage(pw, 80, syntax, options);
        pw.flush();
    }
}
