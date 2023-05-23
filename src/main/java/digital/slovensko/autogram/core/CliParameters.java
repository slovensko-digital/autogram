package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenDriver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.List;
import java.util.Map;

import static digital.slovensko.autogram.core.CliParameters.Validations.*;

public class CliParameters {

    private File sourceFile;

    private File sourceDirectory;

    private String targetDirectory;

    private boolean cli;

    private TokenDriver driver;

    private boolean rewriteFile;

    public CliParameters(CommandLine cmd) {

        var targetDirectory = cmd.getOptionValue("td");
        var sourceDirectory = cmd.getOptionValue("sd");
        var sourceFile = cmd.getOptionValue("sf");
        var cli = cmd.hasOption("c");
        var driver = cmd.getOptionValue("d");
        var rewriteFile = cmd.hasOption("rf");

        this.targetDirectory = validateTargetDirectory(targetDirectory);
        this.sourceDirectory = validateSourceDirectory(sourceDirectory);
        this.sourceFile = validateSourceFile(sourceFile);
        this.cli = validateCli(cli);
        this.driver = validateTokenDriver(driver);
        this.rewriteFile = validateRewriteFile(rewriteFile);
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public boolean isCli() {
        return cli;
    }

    public TokenDriver getDriver() {
        return driver;
    }

    public boolean isRewriteFile() {
        return rewriteFile;
    }

    public static class Validations {
        public static String validateTargetDirectory(String targetDirectory) {
            if (targetDirectory != null && !new File(targetDirectory).exists()) {
                throw new IllegalArgumentException(String.format("Target directory %s does not exist", targetDirectory));
            }
            return targetDirectory;
        }

        public static File validateSourceDirectory(String sourceDirectory) {
            if (sourceDirectory != null && !new File(sourceDirectory).exists()) {
                throw new IllegalArgumentException(String.format("Source directory %s does not exist", sourceDirectory));
            }
            return sourceDirectory == null ? null : new File(sourceDirectory);
        }

        public static File validateSourceFile(String sourceFile) {
            if (sourceFile != null && !new File(sourceFile).exists()) {
                throw new IllegalArgumentException(String.format("Source file %s does not exist", sourceFile));
            }
            return sourceFile == null ? null : new File(sourceFile);
        }

        public static boolean validateCli(boolean cli) {
            return cli;
        }

        public static TokenDriver validateTokenDriver(String driver) {
            if (driver == null) {
               return null;
            }
            List<TokenDriver> drivers = TokenDriver.getAvailableDrivers();
            for (TokenDriver tokenDriver : drivers) {
                if (tokenDriver.getName().toLowerCase().contains(driver)) {
                    return tokenDriver;
                }
            }
            throw new IllegalArgumentException(String.format("Token driver %s not found", driver));
        }

        public static boolean validateRewriteFile(boolean rewriteFile) {
            return rewriteFile;
        }
    }
}
