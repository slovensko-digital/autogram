package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.DefaultDriverDetector;
import digital.slovensko.autogram.core.DriverDetector;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.errors.PDFSignatureLevelIsNotValidException;
import digital.slovensko.autogram.core.errors.SlotIndexIsNotANumberException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.apache.commons.cli.CommandLine;

import java.io.File;

public class CliSettings extends UserSettings {
    private String target;
    private File source;
    private boolean isForce;
    private boolean shouldMakeParentDirectories;
    private String keySelector;
    private boolean pinFromStdin;
    private boolean listKeys;

    public static CliSettings fromCmd(CommandLine cmd) {
        var settings = new CliSettings();
        settings.setCorrectDocumentDisplay(false);
        settings.setSource(getValidSource(cmd.getOptionValue("s")));
        settings.setTarget(cmd.getOptionValue("t"));
        settings.setDriver(cmd.getOptionValue("d"));
        settings.setKeySelector(cmd.getOptionValue("key"));
        settings.setPinFromStdin(cmd.hasOption("pin-stdin"));
        settings.setListKeys(cmd.hasOption("list-keys"));
        settings.setCustomKeystorePath(cmd.getOptionValue("keystore", ""));
        settings.setDriverSlotIndex("default", getValidSlotIndex(cmd.getOptionValue("slot-id")));
        settings.setForce(cmd.hasOption("f"));
        settings.setPdfaCompliance(cmd.hasOption("pdfa"));
        settings.setMakeParentDirectories(cmd.hasOption("parents"));
        settings.setSignatureLevel(getValidSignatureLevel(cmd.getOptionValue("pdf-level", SignatureLevel.PAdES_BASELINE_B.name())));
        settings.setEn319132(cmd.hasOption("en319132"));
        settings.setTsaServer(cmd.getOptionValue("tsa-server"));
        settings.setTsaEnabled(settings.getTsaServer() != null);
        settings.setBulkEnabled(true);
        settings.setPlainXmlEnabled(cmd.hasOption("plain-xml"));
        settings.setTokenSessionTimeout(5);
        settings.setCustomPKCS11DriverPath(cmd.getOptionValue("pkcs11-driver-path", ""));
        return settings;
    }

    @Override
    public DriverDetector getDriverDetector() {
        return new DefaultDriverDetector(this);
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

    public String getKeySelector() {
        return keySelector;
    }

    private void setKeySelector(String value) {
        keySelector = value;
    }

    public boolean isPinFromStdin() {
        return pinFromStdin;
    }

    private void setPinFromStdin(boolean value) {
        pinFromStdin = value;
    }

    public boolean isListKeys() {
        return listKeys;
    }

    private void setListKeys(boolean value) {
        listKeys = value;
    }

    private static File getValidSource(String sourcePath) throws SourceDoesNotExistException {
        if (sourcePath != null && !new File(sourcePath).exists())
            throw new SourceDoesNotExistException(sourcePath);

        return sourcePath == null ? null : new File(sourcePath);
    }

    private static Integer getValidSlotIndex(String optionValue) throws SlotIndexIsNotANumberException {
        if (optionValue == null)
            return -1;
        try {
            return Integer.parseInt(optionValue);
        } catch (NumberFormatException e) {
            throw new SlotIndexIsNotANumberException(optionValue);
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
