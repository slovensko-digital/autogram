package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenDriver;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.Optional;

import static digital.slovensko.autogram.core.CliParameters.Validations.*;

public class CliParameters {
    private final File source;
    private final String target;
    private final boolean force;
    private final TokenDriver driver;
    private final boolean checkPDFACompliance;


    public CliParameters(CommandLine cmd) {
        source = getValidSource(cmd.getOptionValue("s"));
        target = cmd.getOptionValue("t");
        driver = getValidTokenDriver(cmd.getOptionValue("d"));
        force = cmd.hasOption("f");
        checkPDFACompliance = cmd.hasOption("pdfa");
    }

    public File getSource() {
        return source;
    }

    public TokenDriver getDriver() {
        return driver;
    }

    public boolean isForce() {
        return force;
    }

    public String getTarget() {
        return target;
    }

    public boolean shouldCheckPDFACompliance() {
        return checkPDFACompliance;
    }

    public static class Validations {

        public static File getValidSource(String sourcePath) {
            if (sourcePath != null && !new File(sourcePath).exists()) {
                throw new IllegalArgumentException(String.format("Source %s does not exist", sourcePath));
            }
            return sourcePath == null ? null : new File(sourcePath);
        }

        public static TokenDriver getValidTokenDriver(String driverName) {
            if (driverName == null) {
                return null;
            }

            Optional<TokenDriver> tokenDriver = new DefaultDriverDetector()
                .getAvailableDrivers()
                .stream()
                .filter(d -> d.getShortname().equals(driverName))
                .findFirst();

            if (tokenDriver.isEmpty()) {
                throw new IllegalArgumentException(String.format("Token driver %s not found", driverName));
            }
            return tokenDriver.get();
        }
    }
}
