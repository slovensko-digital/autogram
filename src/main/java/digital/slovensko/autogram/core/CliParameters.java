package digital.slovensko.autogram.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static digital.slovensko.autogram.core.CliParameters.Validations.*;

public class CliParameters {

    private Map<String, String> namedParams;

    private File sourceFile;

    private File sourceDirectory;

    private String targetDirectory;

    private boolean cli;

    public CliParameters(Map<String, String> namedParams) {
        this.namedParams = namedParams;

        var targetDirectory = namedParams.get("targetDirectory");
        var sourceDirectory = namedParams.get("sourceDirectory");
        var sourceFile = namedParams.get("sourceFile");
        var cli = namedParams.get("cli");

        this.targetDirectory = validateTargetDirectory(targetDirectory);
        this.sourceDirectory = validateSourceDirectory(sourceDirectory);
        this.sourceFile = validateSourceFile(sourceFile);
        this.cli = validateCli(cli);
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

        public static boolean validateCli(String cli) {
            return cli != null && Boolean.valueOf(cli);
        }
    }
}
