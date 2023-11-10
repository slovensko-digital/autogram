package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.PDFSignatureLevelIsNotValidException;
import digital.slovensko.autogram.core.errors.SlotIdIsNotANumberException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.core.errors.TokenDriverDoesNotExistException;
import digital.slovensko.autogram.drivers.TokenDriver;
import eu.europa.esig.dss.enumerations.SignatureLevel;

import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.Optional;

public class CliParameters {
    private final File source;
    private final String target;
    private final boolean force;
    private final TokenDriver driver;
    private final Integer slotId;
    private final boolean checkPDFACompliance;
    private final boolean makeParentDirectories;
    private final SignatureLevel pdfSignatureLevel;
    private final boolean en319132;

    public CliParameters(CommandLine cmd) throws SourceDoesNotExistException, TokenDriverDoesNotExistException,
            SlotIdIsNotANumberException, PDFSignatureLevelIsNotValidException {
        source = getValidSource(cmd.getOptionValue("s"));
        target = cmd.getOptionValue("t");
        driver = getValidTokenDriver(cmd.getOptionValue("d"));
        slotId = getValidSlotId(cmd.getOptionValue("slot-id"));
        force = cmd.hasOption("f");
        checkPDFACompliance = cmd.hasOption("pdfa");
        makeParentDirectories = cmd.hasOption("parents");
        pdfSignatureLevel = getValidSignatureLevel(
                cmd.getOptionValue("pdf-level", SignatureLevel.PAdES_BASELINE_B.name()));
        en319132 = cmd.hasOption("en319132");
    }

    private SignatureLevel getValidSignatureLevel(String optionValue) throws PDFSignatureLevelIsNotValidException {
        try {
            return SignatureLevel.valueOf(optionValue);
        } catch (Exception e) {
            throw new PDFSignatureLevelIsNotValidException(optionValue);
        }
    }

    private Integer getValidSlotId(String optionValue) throws SlotIdIsNotANumberException {
        if (optionValue == null)
            return -1;

        try {
            return Integer.parseInt(optionValue);
        } catch (NumberFormatException e) {
            throw new SlotIdIsNotANumberException(optionValue);
        }
    }

    public File getSource() {
        return source;
    }

    public TokenDriver getDriver() {
        return driver;
    }

    public Integer getSlotId() {
        return slotId;
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

    private static File getValidSource(String sourcePath) throws SourceDoesNotExistException {
        if (sourcePath != null && !new File(sourcePath).exists())
            throw new SourceDoesNotExistException(sourcePath);

        return sourcePath == null ? null : new File(sourcePath);
    }

    private static TokenDriver getValidTokenDriver(String driverName) throws TokenDriverDoesNotExistException {
        if (driverName == null)
            return null;

        Optional<TokenDriver> tokenDriver = new DefaultDriverDetector("", false)
                .getAvailableDrivers()
                .stream()
                .filter(d -> d.getShortname().equals(driverName))
                .findFirst();

        if (tokenDriver.isEmpty())
            throw new TokenDriverDoesNotExistException(driverName);

        return tokenDriver.get();
    }

    public boolean shouldSignPDFAsPades() {
        return pdfSignatureLevel == SignatureLevel.PAdES_BASELINE_B;
    }

    public boolean shouldSignAsEn319132() {
        return en319132;
    }

    public SignatureLevel pdfSignatureLevel() {
        return pdfSignatureLevel;
    }
}
