package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.gui.SignatureLevelStringConverter;
import eu.europa.esig.dss.enumerations.SignatureLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class UserSettings {

    private enum UserSettingsKeys {
        SIGNATURE_LEVEL("signature_level"),
        DRIVER("driver"),
        EN319132("en319132"),
        SIGN_INDIVIDUALLY("sign_individually"),
        CORRECT_DOCUMENT_DISPLAY("correct_document_display"),
        SIGNATURES_VALIDITY("signatures_validity"),
        PDFA_COMPLIANCE("pdfa_compliance"),
        SERVER_ENABLED("server_enabled"),
        TRUSTED_LIST("trusted_list"),;

        private String key;

        UserSettingsKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private SignatureLevel signatureLevel;

    private TokenDriver driver;

    private boolean en319132;

    private boolean signIndividually;

    private boolean correctDocumentDisplay;

    private boolean signaturesValidity;

    private boolean pdfaCompliance;

    private boolean serverEnabled;

    private List<String> trustedList;


    private UserSettings(SignatureLevel signatureLevel, TokenDriver driver, boolean en319132,
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
        Preferences prefs = Preferences.userNodeForPackage(UserSettings.class);

        var signatureType = prefs.get(UserSettingsKeys.SIGNATURE_LEVEL.getKey(), null);
        var driver = prefs.get(UserSettingsKeys.DRIVER.getKey(), null);
        var en319132 = prefs.getBoolean(UserSettingsKeys.EN319132.getKey(), false);
        var signIndividually = prefs.getBoolean(UserSettingsKeys.SIGN_INDIVIDUALLY.getKey(), true);
        var correctDocumentDisplay = prefs.getBoolean(UserSettingsKeys.CORRECT_DOCUMENT_DISPLAY.getKey(), true);
        var signaturesValidity = prefs.getBoolean(UserSettingsKeys.SIGNATURES_VALIDITY.getKey(), true);
        var pdfaCompliance = prefs.getBoolean(UserSettingsKeys.PDFA_COMPLIANCE.getKey(), true);
        var serverEnabled = prefs.getBoolean(UserSettingsKeys.SERVER_ENABLED.getKey(), true);
        var trustedList = prefs.get(UserSettingsKeys.TRUSTED_LIST.getKey(), "SK,CZ,AT,PL,HU");

        var tokenDriver = new DefaultDriverDetector()
                .getAvailableDrivers()
                .stream()
                .filter(d -> d.getShortname().equals(driver))
                .findFirst();

        SignatureLevelStringConverter signatureLevelStringConverter = new SignatureLevelStringConverter();
        var signatureLevel = Arrays
                .asList(SignatureLevel.XAdES_BASELINE_B, SignatureLevel.PAdES_BASELINE_B)
                .stream()
                .map(signatureLevelStringConverter::toString)
                .filter(sl -> sl.equals(signatureType))
                .map(signatureLevelStringConverter::fromString)
                .findFirst();

        return new UserSettings(signatureLevel.isEmpty() ? SignatureLevel.PAdES_BASELINE_B : signatureLevel.get(),
                tokenDriver.isEmpty() ? null : tokenDriver.get(),
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

    public void setSignatureLevel(SignatureLevel signatureLevel) {
        this.signatureLevel = signatureLevel;
    }

    public TokenDriver getDriver() {
        return driver;
    }

    public void setDriver(TokenDriver driver) {
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

    public List<String> getTrustedList() {
        return trustedList;
    }

    public void addToTrustedList(String country) {
        this.trustedList.add(country);
    }

    public void removeFromTrustedList(String country) {
        this.trustedList.remove(country);
    }

    public void save() {
        Preferences prefs = Preferences.userNodeForPackage(UserSettings.class);

        SignatureLevelStringConverter signatureLevelStringConverter = new SignatureLevelStringConverter();
        prefs.put(UserSettingsKeys.SIGNATURE_LEVEL.getKey(), signatureLevelStringConverter.toString(signatureLevel));
        prefs.put(UserSettingsKeys.DRIVER.getKey(), driver == null ? "" : driver.getShortname());
        prefs.putBoolean(UserSettingsKeys.EN319132.getKey(), en319132);
        prefs.putBoolean(UserSettingsKeys.SIGN_INDIVIDUALLY.getKey(), signIndividually);
        prefs.putBoolean(UserSettingsKeys.CORRECT_DOCUMENT_DISPLAY.getKey(), correctDocumentDisplay);
        prefs.putBoolean(UserSettingsKeys.SIGNATURES_VALIDITY.getKey(), signaturesValidity);
        prefs.putBoolean(UserSettingsKeys.PDFA_COMPLIANCE.getKey(), pdfaCompliance);
        prefs.putBoolean(UserSettingsKeys.SERVER_ENABLED.getKey(), serverEnabled);
        prefs.put(UserSettingsKeys.TRUSTED_LIST.getKey(), trustedList.stream().collect(Collectors.joining(",")));
    }

    public boolean signPDFAsPades() {
        return signatureLevel == SignatureLevel.PAdES_BASELINE_B;
    }
}
