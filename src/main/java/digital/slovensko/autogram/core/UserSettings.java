package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.gui.SignatureLevelStringConverter;
import eu.europa.esig.dss.enumerations.SignatureLevel;

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
    private List<String> trustedList;

    private UserSettings(SignatureLevel signatureLevel, String driver, boolean en319132,
            boolean signIndividually, boolean correctDocumentDisplay,
            boolean signaturesValidity, boolean pdfaCompliance,
            boolean serverEnabled, List<String> trustedList) {
        this.signatureLevel = signatureLevel;
        this.driver = driver;
        this.en319132 = en319132;
        this.signIndividually = signIndividually;
        this.correctDocumentDisplay = correctDocumentDisplay;
        this.signaturesValidity = signaturesValidity;
        this.pdfaCompliance = pdfaCompliance;
        this.serverEnabled = serverEnabled;
        this.trustedList = trustedList;
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
        var trustedList = prefs.get("TRUSTED_LIST", "SK,CZ,AT,PL,HU");

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
                trustedList == null ? new ArrayList<>() : new ArrayList<>(List.of(trustedList.split(","))));
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
        prefs.put("TRUSTED_LIST", trustedList.stream().collect(Collectors.joining(",")));
    }
}
