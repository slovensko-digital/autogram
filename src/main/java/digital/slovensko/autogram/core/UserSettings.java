package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.SignatureLevelStringConverter;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class UserSettings {
    private SignatureLevel signatureLevel;
    private String driver;
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

    private UserSettings(SignatureLevel signatureLevel, String driver, boolean en319132,
            boolean signIndividually, boolean correctDocumentDisplay,
            boolean signaturesValidity, boolean pdfaCompliance,
            boolean serverEnabled, boolean expiredCertsEnabled, List<String> trustedList,
            String customKeystorePath, boolean customKeystorePassword, String tsaServer,
            String customTsaServer, TSPSource tspSource, boolean tsaEnabled) {
        this.signatureLevel = signatureLevel;
        this.driver = driver;
        this.en319132 = en319132;
        this.signIndividually = signIndividually;
        this.correctDocumentDisplay = correctDocumentDisplay;
        this.signaturesValidity = signaturesValidity;
        this.pdfaCompliance = pdfaCompliance;
        this.serverEnabled = serverEnabled;
        this.expiredCertsEnabled = expiredCertsEnabled;
        this.trustedList = trustedList;
        this.customKeystorePath = customKeystorePath;
        this.customKeystorePasswordPrompt = customKeystorePassword;
        this.tsaServer = tsaServer;
        this.customTsaServer = customTsaServer;
        this.tspSource = tspSource;
        this.tsaEnabled = tsaEnabled;
    }

    public static UserSettings load() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);

        var signatureType = prefs.get("SIGNATURE_LEVEL", null);
        var driver = prefs.get("DRIVER", "");
        var en319132 = prefs.getBoolean("EN319132", false);
        var signIndividually = prefs.getBoolean("SIGN_INDIVIDUALLY", true);
        var correctDocumentDisplay = prefs.getBoolean("CORRECT_DOCUMENT_DISPLAY", true);
        var signaturesValidity = prefs.getBoolean("SIGNATURES_VALIDITY", true);
        var pdfaCompliance = prefs.getBoolean("PDFA_COMPLIANCE", true);
        var serverEnabled = prefs.getBoolean("SERVER_ENABLED", true);
        var expiredCertsEnabled = prefs.getBoolean("EXPIRED_CERTS_ENABLED", false);
        var trustedList = prefs.get("TRUSTED_LIST", "SK,CZ,AT,PL,HU");
        var customKeystorePath = prefs.get("CUSTOM_KEYSTORE_PATH", "");
        var customKeystorePasswordPrompt = prefs.getBoolean("CUSTOM_KEYSTORE_PASSWORD_PROMPT", false);
        var tsaServer = prefs.get("TSA_SERVER", "");
        var customTsaServer = prefs.get("CUSTOM_TSA_SERVER", "");
        var tsaEnabled = prefs.getBoolean("TSA_ENABLE", false);

        var tspSource = new OnlineTSPSource(tsaServer);

        var signatureLevelStringConverter = new SignatureLevelStringConverter();
        var signatureLevel = Arrays
                .asList(SignatureLevel.XAdES_BASELINE_B, SignatureLevel.PAdES_BASELINE_B)
                .stream()
                .map(signatureLevelStringConverter::toString)
                .filter(sl -> sl.equals(signatureType))
                .map(signatureLevelStringConverter::fromString)
                .findFirst();

        return new UserSettings(signatureLevel.isEmpty() ? SignatureLevel.PAdES_BASELINE_B : signatureLevel.get(),
                driver.isEmpty() ? null : driver,
                en319132,
                signIndividually,
                correctDocumentDisplay,
                signaturesValidity,
                pdfaCompliance,
                serverEnabled,
                expiredCertsEnabled,
                trustedList == null ? new ArrayList<>() : new ArrayList<>(List.of(trustedList.split(","))),
                customKeystorePath,
                customKeystorePasswordPrompt,
                tsaServer,
                customTsaServer,
                tspSource,
                tsaEnabled);
    }

    public SignatureLevel getSignatureLevel() {
        return signatureLevel;
    }

    public boolean shouldSignPDFAsPades() {
        return signatureLevel == SignatureLevel.PAdES_BASELINE_B;
    }

    public void setSignatureLevel(SignatureLevel signatureLevel) {
        this.signatureLevel = signatureLevel;
        save();
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
        save();
    }

    public boolean isEn319132() {
        return en319132;
    }

    public void setEn319132(boolean en319132) {
        this.en319132 = en319132;
        save();
    }

    public boolean isSignIndividually() {
        return signIndividually;
    }

    public void setSignIndividually(boolean signIndividually) {
        this.signIndividually = signIndividually;
        save();
    }

    public boolean isCorrectDocumentDisplay() {
        return correctDocumentDisplay;
    }

    public void setCorrectDocumentDisplay(boolean correctDocumentDisplay) {
        this.correctDocumentDisplay = correctDocumentDisplay;
        save();
    }

    public boolean isSignaturesValidity() {
        return signaturesValidity;
    }

    public void setSignaturesValidity(boolean signaturesValidity) {
        this.signaturesValidity = signaturesValidity;
        save();
    }

    public boolean isPdfaCompliance() {
        return pdfaCompliance;
    }

    public void setPdfaCompliance(boolean pdfaCompliance) {
        this.pdfaCompliance = pdfaCompliance;
        save();
    }

    public boolean isServerEnabled() {
        return serverEnabled;
    }

    public void setServerEnabled(boolean serverEnabled) {
        this.serverEnabled = serverEnabled;
        save();
    }

    public boolean isExpiredCertsEnabled() {
        return expiredCertsEnabled;
    }

    public void setExpiredCertsEnabled(boolean expiredCertsEnabled) {
        this.expiredCertsEnabled = expiredCertsEnabled;
        save();
    }

    public List<String> getTrustedList() {
        return trustedList;
    }

    public void addToTrustedList(String country) {
        trustedList.add(country);
        save();
    }

    public void removeFromTrustedList(String country) {
        trustedList.remove(country);
        save();
    }

    public String getCustomKeystorePath() {
        return customKeystorePath;
    }

    public void setCustomKeystorePath(String value) {
        customKeystorePath = value;
        save();
    }

    public boolean getCustomKeystorePasswordPrompt() {
        return customKeystorePasswordPrompt;
    }

    public void setCustomKeystorePasswordPrompt(boolean value) {
        customKeystorePasswordPrompt = value;
        save();
    }

    public String getTsaServer() {
        return tsaServer;
    }

    public void setTsaServer(String value) {
        tsaServer = value;
        tspSource = new OnlineTSPSource(tsaServer);
        save();
    }

    public String getCustomTsaServer() {
        return customTsaServer;
    }

    public void setCustomTsaServer(String value) {
        customTsaServer = value;
        save();
    }

    public TSPSource getTspSource() {
        return tspSource;
    }

    public boolean getTsaEnabled() {
        return tsaEnabled;
    }

    public void setTsaEnabled(boolean value) {
        tsaEnabled = value;
        save();
    }

    private void save() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);

        prefs.put("SIGNATURE_LEVEL", new SignatureLevelStringConverter().toString(signatureLevel));
        prefs.put("DRIVER", driver == null ? "" : driver);
        prefs.putBoolean("EN319132", en319132);
        prefs.putBoolean("SIGN_INDIVIDUALLY", signIndividually);
        prefs.putBoolean("CORRECT_DOCUMENT_DISPLAY", correctDocumentDisplay);
        prefs.putBoolean("SIGNATURES_VALIDITY", signaturesValidity);
        prefs.putBoolean("PDFA_COMPLIANCE", pdfaCompliance);
        prefs.putBoolean("SERVER_ENABLED", serverEnabled);
        prefs.putBoolean("EXPIRED_CERTS_ENABLED", expiredCertsEnabled);
        prefs.put("TRUSTED_LIST", trustedList.stream().collect(Collectors.joining(",")));
        prefs.put("CUSTOM_KEYSTORE_PATH", customKeystorePath);
        prefs.putBoolean("CUSTOM_KEYSTORE_PASSWORD_PROMPT", customKeystorePasswordPrompt);
        prefs.put("TSA_SERVER", tsaServer);
        prefs.put("CUSTOM_TSA_SERVER", customTsaServer);
        prefs.putBoolean("TSA_ENABLE", tsaEnabled);
    }
}
