package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.SignatureLevelStringConverter;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class UserSettings implements PasswordManagerSettings, SignatureTokenSettings, DriverDetectorSettings {
    private SignatureLevel signatureLevel;
    private String driver;
    private int slotId;
    private boolean en319132;
    private boolean signIndividually;
    private boolean correctDocumentDisplay;
    private boolean signaturesValidity;
    private boolean pdfaCompliance;
    private boolean serverEnabled;
    private boolean expiredCertsEnabled;
    private List<String> trustedList;
    private String customKeystorePath;
    private boolean customKeystorePasswordPrompt;
    private String tsaServer;
    private TSPSource tspSource;
    private boolean tsaEnabled;
    private String customTsaServer;
    private boolean bulkEnabled;

    public static UserSettings load() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);

        var settings = new UserSettings();
        settings.setSignatureType(prefs.get("SIGNATURE_LEVEL", null));
        settings.setDriver(prefs.get("DRIVER", ""));
        settings.setSlotId(prefs.getInt("SLOT_ID", -1));
        settings.setEn319132(prefs.getBoolean("EN319132", false));
        settings.setBulkEnabled(prefs.getBoolean("BULK_ENABLED", false));
        settings.setSignIndividually(prefs.getBoolean("SIGN_INDIVIDUALLY", true));
        settings.setCorrectDocumentDisplay(prefs.getBoolean("CORRECT_DOCUMENT_DISPLAY", true));
        settings.setSignaturesValidity(prefs.getBoolean("SIGNATURES_VALIDITY", true));
        settings.setPdfaCompliance(prefs.getBoolean("PDFA_COMPLIANCE", true));
        settings.setServerEnabled(prefs.getBoolean("SERVER_ENABLED", true));
        settings.setExpiredCertsEnabled(prefs.getBoolean("EXPIRED_CERTS_ENABLED", false));
        settings.setTrustedList(prefs.get("TRUSTED_LIST", "SK,CZ,AT,PL,HU"));
        settings.setCustomKeystorePath(prefs.get("CUSTOM_KEYSTORE_PATH", ""));
        settings.setCustomKeystorePasswordPrompt(prefs.getBoolean("CUSTOM_KEYSTORE_PASSWORD_PROMPT", false));
        settings.setTsaServer(prefs.get("TSA_SERVER", "http://tsa.izenpe.com"));
        settings.setCustomTsaServer(prefs.get("CUSTOM_TSA_SERVER", ""));
        settings.setTsaEnabled(prefs.getBoolean("TSA_ENABLE", false));

        return settings;
    }

    public void save() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);

        prefs.put("SIGNATURE_LEVEL", new SignatureLevelStringConverter().toString(signatureLevel));
        prefs.put("DRIVER", driver == null ? "" : driver);
        prefs.putInt("SLOT_ID", slotId);
        prefs.putBoolean("EN319132", en319132);
        prefs.putBoolean("BULK_ENABLED", signIndividually);
        prefs.putBoolean("SIGN_INDIVIDUALLY", signIndividually);
        prefs.putBoolean("CORRECT_DOCUMENT_DISPLAY", correctDocumentDisplay);
        prefs.putBoolean("SIGNATURES_VALIDITY", signaturesValidity);
        prefs.putBoolean("PDFA_COMPLIANCE", pdfaCompliance);
        prefs.putBoolean("SERVER_ENABLED", serverEnabled);
        prefs.putBoolean("EXPIRED_CERTS_ENABLED", expiredCertsEnabled);
        prefs.put("TRUSTED_LIST", String.join(",", trustedList));
        prefs.put("CUSTOM_KEYSTORE_PATH", customKeystorePath);
        prefs.putBoolean("CUSTOM_KEYSTORE_PASSWORD_PROMPT", customKeystorePasswordPrompt);
        prefs.put("TSA_SERVER", tsaServer);
        prefs.put("CUSTOM_TSA_SERVER", customTsaServer);
        prefs.putBoolean("TSA_ENABLE", tsaEnabled);
    }

    private void setSignatureType(String signatureType) {
        var signatureLevelStringConverter = new SignatureLevelStringConverter();
        var signatureLevel = Arrays
                .asList(SignatureLevel.XAdES_BASELINE_B, SignatureLevel.PAdES_BASELINE_B)
                .stream()
                .map(signatureLevelStringConverter::toString)
                .filter(sl -> sl.equals(signatureType))
                .map(signatureLevelStringConverter::fromString)
                .findFirst().get();
        this.signatureLevel = signatureLevel;
    }

    private void setTrustedList(String trustedList) {
        this.trustedList = trustedList == null ? new ArrayList<>() : new ArrayList<>(List.of(trustedList.split(",")));
    }

    public SignatureLevel getSignatureLevel() {
        return signatureLevel;
    }

    public boolean shouldSignPDFAsPades() {
        return signatureLevel == SignatureLevel.PAdES_BASELINE_B;
    }

    public void setSignatureLevel(SignatureLevel signatureLevel) {
        this.signatureLevel = signatureLevel;
    }

    public String getDefaultDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public boolean isEn319132() {
        return en319132;
    }

    public void setEn319132(boolean en319132) {
        this.en319132 = en319132;
    }

    public boolean isSignIndividually() {
        return signIndividually;
    }

    public void setSignIndividually(boolean signIndividually) {
        this.signIndividually = signIndividually;
    }

    public boolean isCorrectDocumentDisplay() {
        return correctDocumentDisplay;
    }

    public void setCorrectDocumentDisplay(boolean correctDocumentDisplay) {
        this.correctDocumentDisplay = correctDocumentDisplay;
    }

    public boolean isSignaturesValidity() {
        return signaturesValidity;
    }

    public void setSignaturesValidity(boolean signaturesValidity) {
        this.signaturesValidity = signaturesValidity;
    }

    public boolean isPdfaCompliance() {
        return pdfaCompliance;
    }

    public void setPdfaCompliance(boolean pdfaCompliance) {
        this.pdfaCompliance = pdfaCompliance;
    }

    public boolean isServerEnabled() {
        return serverEnabled;
    }

    public void setServerEnabled(boolean serverEnabled) {
        this.serverEnabled = serverEnabled;
    }

    public boolean isExpiredCertsEnabled() {
        return expiredCertsEnabled;
    }

    public void setExpiredCertsEnabled(boolean expiredCertsEnabled) {
        this.expiredCertsEnabled = expiredCertsEnabled;
    }

    public List<String> getTrustedList() {
        return trustedList;
    }

    public void addToTrustedList(String country) {
        trustedList.add(country);
    }

    public void removeFromTrustedList(String country) {
        trustedList.remove(country);
    }

    public String getCustomKeystorePath() {
        return customKeystorePath;
    }

    public void setCustomKeystorePath(String value) {
        customKeystorePath = value;
    }

    public boolean getCustomKeystorePasswordPrompt() {
        return customKeystorePasswordPrompt;
    }

    public void setCustomKeystorePasswordPrompt(boolean value) {
        customKeystorePasswordPrompt = value;
    }

    public String getTsaServer() {
        return tsaServer;
    }

    public void setTsaServer(String value) {
        tsaServer = value;
        tspSource = new OnlineTSPSource(tsaServer);
    }

    public String getCustomTsaServer() {
        return customTsaServer;
    }

    public void setCustomTsaServer(String value) {
        customTsaServer = value;
    }

    public TSPSource getTspSource() {
        return tspSource;
    }

    public boolean getTsaEnabled() {
        return tsaEnabled;
    }

    public void setTsaEnabled(boolean value) {
        tsaEnabled = value;
    }

    public void setBulkEnabled(boolean value) {
        bulkEnabled = value;
    }

    @Override
    public boolean getCacheContextSpecificPasswordEnabled() {
        return bulkEnabled; // faux settings
    }

    @Override
    public boolean getForceContextSpecificLoginEnabled() {
        return bulkEnabled; // faux settings
    }

    @Override
    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int value) {
        slotId = value;
    }

    public DriverDetector getDriverDetector() {
        return new DefaultDriverDetector(this);
    }

    public boolean isBulkEnabled() {
        return bulkEnabled;
    }
}
