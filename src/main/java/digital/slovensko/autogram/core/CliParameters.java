package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.core.errors.TokenDriverDoesNotExistException;
import digital.slovensko.autogram.drivers.TokenDriver;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.Optional;

public class CliParameters {
    private final File source;
    private final String target;
    private final boolean force;
    private final TokenDriver driver;
    private final boolean checkPDFACompliance;
    private final boolean makeParentDirectories;


    public CliParameters(CommandLine cmd) {
        source = getValidSource(cmd.getOptionValue("s"));
        target = cmd.getOptionValue("t");
        driver = getValidTokenDriver(cmd.getOptionValue("d"));
        force = cmd.hasOption("f");
        checkPDFACompliance = cmd.hasOption("pdfa");
        makeParentDirectories = cmd.hasOption("parents");
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

    public boolean shouldMakeParentDirectories() {
        return makeParentDirectories;
    }

    private static File getValidSource(String sourcePath) {
        if (sourcePath != null && !new File(sourcePath).exists())
            throw new SourceDoesNotExistException(sourcePath);

        return sourcePath == null ? null : new File(sourcePath);
    }

    private static TokenDriver getValidTokenDriver(String driverName) {
        if (driverName == null)
            return null;

        Optional<TokenDriver> tokenDriver = new DefaultDriverDetector()
            .getAvailableDrivers()
            .stream()
            .filter(d -> d.getShortname().equals(driverName))
            .findFirst();

        if (tokenDriver.isEmpty())
            throw new TokenDriverDoesNotExistException(driverName);

        return tokenDriver.get();
    }
}
