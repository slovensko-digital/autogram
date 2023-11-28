package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.PDFSignatureLevelIsNotValidException;
import digital.slovensko.autogram.core.errors.SlotIdIsNotANumberException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.apache.commons.cli.CommandLine;

import java.io.File;

public class CliSettings extends UserSettings {
    private String target;
    private File source;
    private boolean isForce;
    private boolean shouldMakeParentDirectories;

    public static CliSettings fromCmd(CommandLine cmd) {
        var settings = new CliSettings();
        settings.setCorrectDocumentDisplay(false);
        settings.setSource(getValidSource(cmd.getOptionValue("s")));
        settings.setTarget(cmd.getOptionValue("t"));
        settings.setDriver(cmd.getOptionValue("d"));
        settings.setCustomKeystorePath(cmd.getOptionValue("keystore", ""));
        settings.setCustomKeystorePasswordPrompt(true);
        settings.setSlotId(getValidSlotId(cmd.getOptionValue("slot-id")));
        settings.setForce(cmd.hasOption("f"));
        settings.setPdfaCompliance(cmd.hasOption("pdfa"));
        settings.setMakeParentDirectories(cmd.hasOption("parents"));
        settings.setPdfSignatureLevel(getValidSignatureLevel(cmd.getOptionValue("pdf-level", SignatureLevel.PAdES_BASELINE_B.name())));
        settings.setEn319132(cmd.hasOption("en319132"));
        settings.setTsaServer(cmd.getOptionValue("tsa-server", null));
        settings.setBulkEnabled(true);

        return settings;
    }

    private void setPdfSignatureLevel(SignatureLevel value) {
    }

    private void setMakeParentDirectories(boolean value) {
        shouldMakeParentDirectories = value;
    }

    private void setForce(boolean value) {
        isForce = value;
    }

    private void setSource(File source) {
        this.source = source;
    }

    private void setTarget(String value) {
        this.target = value;
    }

    public File getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public boolean isForce() {
        return isForce;
    }

    public boolean shouldMakeParentDirectories() {
        return shouldMakeParentDirectories;
    }

    private static File getValidSource(String sourcePath) throws SourceDoesNotExistException {
        if (sourcePath != null && !new File(sourcePath).exists())
            throw new SourceDoesNotExistException(sourcePath);

        return sourcePath == null ? null : new File(sourcePath);
    }

    private static Integer getValidSlotId(String optionValue) throws SlotIdIsNotANumberException {
        if (optionValue == null)
            return -1;
        try {
            return Integer.parseInt(optionValue);
        } catch (NumberFormatException e) {
            throw new SlotIdIsNotANumberException(optionValue);
        }
    }

    private static SignatureLevel getValidSignatureLevel(String optionValue) throws PDFSignatureLevelIsNotValidException {
        try {
            return SignatureLevel.valueOf(optionValue);
        } catch (Exception e) {
            throw new PDFSignatureLevelIsNotValidException(optionValue);
        }
    }
}
