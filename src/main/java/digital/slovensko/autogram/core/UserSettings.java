package digital.slovensko.autogram.core;

import digital.slovensko.autogram.ui.SupportedLanguage;
import digital.slovensko.autogram.ui.gui.SignatureLevelStringConverter;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.CompositeTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Stream;


public class UserSettings implements PasswordManagerSettings, SignatureTokenSettings, DriverDetectorSettings {
    private static final String DEFAULT_LANGUAGE = null; // system language
    private final String DEFAULT_SIGNATURE_LEVEL = SignatureLevelStringConverter.PADES;
    private final String DEFAULT_DRIVER = "";
    private final String DRIVER_SLOT_INDEX_MAP = "";
    private final boolean DEFAULT_EN319132 = false;
    private final boolean DEFAULT_BULK_ENABLED = false;
    private final boolean DEFAULT_PLAIN_XML_ENABLED = false;
    private final boolean DEFAULT_SIGN_INDIVIDUALLY = true;
    private final boolean DEFAULT_CORRECT_DOCUMENT_DISPLAY = true;
    private final boolean DEFAULT_SIGNATURES_VALIDITY = true;
    private final boolean DEFAULT_PDFA_COMPLIANCE = true;
    private final boolean DEFAULT_SERVER_ENABLED = true;
    private final boolean DEFAULT_EXPIRED_CERTS_ENABLED = false;
    private final String DEFAULT_TRUSTED_LIST = "SK,CZ,AT,PL,HU,BE,NL,LT";
    private final String DEFAULT_CUSTOM_KEYSTORE_PATH = "";
    private final String DEFAULT_CUSTOM_TSA_SERVER = "";
    private final boolean DEFAULT_TSA_ENABLE = false;
    private final int DEFAULT_PDF_DPI = 100;
    private final long DEFAULT_TOKEN_SESSION_TIMEOUT = 5L;
    private final String DEFAULT_CUSTOM_PKCS11_DRIVER_PATH = "";
    private final String DEFAULT_TSA_SERVER = "http://tsa.baltstamp.lt,http://ts.quovadisglobal.com/eu";

    private SupportedLanguage language;
    private SignatureLevel signatureLevel;
    private String driver;
    private int slotIndex;
    private boolean en319132;
    private boolean plainXmlEnabled;
    private boolean signIndividually;
    private boolean correctDocumentDisplay;
    private boolean signaturesValidity;
    private boolean pdfaCompliance;
    private boolean serverEnabled;
    private boolean expiredCertsEnabled;
    private List<String> trustedList;
    private String customKeystorePath;
    private String tsaServer;
    private CompositeTSPSource tspSource;
    private boolean tsaEnabled;
    private String customTsaServer;
    private boolean bulkEnabled;
    private int pdfDpi;
    private long tokenSessionTimeout;
    private String customPKCS11DriverPath;
    private Map<String, Integer> driverSlotIndexMap = new HashMap<>();

    public static UserSettings load() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);
        var settings = new UserSettings();
        settings.setLanguage(SupportedLanguage.getByLanguage(prefs.get("LANGUAGE", DEFAULT_LANGUAGE)));
        settings.setSignatureType(prefs.get("SIGNATURE_LEVEL", settings.DEFAULT_SIGNATURE_LEVEL));
        settings.setDriver(prefs.get("DRIVER", settings.DEFAULT_DRIVER));
        settings.setEn319132(prefs.getBoolean("EN319132", settings.DEFAULT_EN319132));
        settings.setBulkEnabled(prefs.getBoolean("BULK_ENABLED", settings.DEFAULT_BULK_ENABLED));
        settings.setPlainXmlEnabled(prefs.getBoolean("PLAIN_XML_ENABLED", settings.DEFAULT_PLAIN_XML_ENABLED));
        settings.setSignIndividually(prefs.getBoolean("SIGN_INDIVIDUALLY", settings.DEFAULT_SIGN_INDIVIDUALLY));
        settings.setCorrectDocumentDisplay(prefs.getBoolean("CORRECT_DOCUMENT_DISPLAY", settings.DEFAULT_CORRECT_DOCUMENT_DISPLAY));
        settings.setSignaturesValidity(prefs.getBoolean("SIGNATURES_VALIDITY", settings.DEFAULT_SIGNATURES_VALIDITY));
        settings.setPdfaCompliance(prefs.getBoolean("PDFA_COMPLIANCE", settings.DEFAULT_PDFA_COMPLIANCE));
        settings.setServerEnabled(prefs.getBoolean("SERVER_ENABLED", settings.DEFAULT_SERVER_ENABLED));
        settings.setExpiredCertsEnabled(prefs.getBoolean("EXPIRED_CERTS_ENABLED", settings.DEFAULT_EXPIRED_CERTS_ENABLED));
        settings.setTrustedList(prefs.get("TRUSTED_LIST", settings.DEFAULT_TRUSTED_LIST));
        settings.setCustomKeystorePath(prefs.get("CUSTOM_KEYSTORE_PATH", settings.DEFAULT_CUSTOM_KEYSTORE_PATH));
        settings.setCustomTsaServer(prefs.get("CUSTOM_TSA_SERVER", settings.DEFAULT_CUSTOM_TSA_SERVER));
        settings.setTsaEnabled(prefs.getBoolean("TSA_ENABLE", settings.DEFAULT_TSA_ENABLE));
        settings.setPdfDpi(prefs.getInt("PDF_DPI", settings.DEFAULT_PDF_DPI));
        settings.setTokenSessionTimeout(prefs.getLong("TOKEN_SESSION_TIMEOUT", settings.DEFAULT_TOKEN_SESSION_TIMEOUT));
        settings.setCustomPKCS11DriverPath(prefs.get("CUSTOM_PKCS11_DRIVER_PATH", settings.DEFAULT_CUSTOM_PKCS11_DRIVER_PATH));

        String mapString = prefs.get("DRIVER_SLOT_INDEX_MAP", "");
        if (!mapString.isEmpty()) {
            String[] entries = mapString.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String key = parts[0];
                    try {
                        int value = Integer.parseInt(parts[1]);
                        settings.setDriverSlotIndex(key, value);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } else {
            // Legacy support for single slot index
            var slotIndex = prefs.getInt("SLOT_INDEX", -1);
            if (slotIndex != -1) {
                settings.setDriverSlotIndex("gemalto", slotIndex);
                settings.setDriverSlotIndex("monet", slotIndex);
                settings.setDriverSlotIndex("secure_store", slotIndex);
            }
        }

        var tsaServerPref = prefs.get("TSA_SERVER", settings.DEFAULT_TSA_SERVER);
        if (tsaServerPref.equals("http://tsa.belgium.be/connect,http://ts.quovadisglobal.com/eu,http://tsa.sep.bg") ||
                tsaServerPref.equals("http://ts.quovadisglobal.com/eu,http://tsa.baltstamp.lt")) // old default
            tsaServerPref = "http://tsa.baltstamp.lt,http://ts.quovadisglobal.com/eu";

        settings.setTsaServer(tsaServerPref);

        return settings;
    }

    public void save() {
        var prefs = Preferences.userNodeForPackage(UserSettings.class);

        prefs.put("LANGUAGE", (language == null) ? "" : language.getLocale().getLanguage());
        prefs.put("SIGNATURE_LEVEL", new SignatureLevelStringConverter().toString(signatureLevel));
        prefs.put("DRIVER", driver == null ? "" : driver);
        prefs.putBoolean("EN319132", en319132);
        prefs.putBoolean("BULK_ENABLED", bulkEnabled);
        prefs.putBoolean("PLAIN_XML_ENABLED", plainXmlEnabled);
        prefs.putBoolean("SIGN_INDIVIDUALLY", signIndividually);
        prefs.putBoolean("CORRECT_DOCUMENT_DISPLAY", correctDocumentDisplay);
        prefs.putBoolean("SIGNATURES_VALIDITY", signaturesValidity);
        prefs.putBoolean("PDFA_COMPLIANCE", pdfaCompliance);
        prefs.putBoolean("SERVER_ENABLED", serverEnabled);
        prefs.putBoolean("EXPIRED_CERTS_ENABLED", expiredCertsEnabled);
        prefs.put("TRUSTED_LIST", String.join(",", trustedList));
        prefs.put("CUSTOM_KEYSTORE_PATH", customKeystorePath);
        prefs.put("TSA_SERVER", tsaServer);
        prefs.put("CUSTOM_TSA_SERVER", customTsaServer);
        prefs.putBoolean("TSA_ENABLE", tsaEnabled);
        prefs.putInt("PDF_DPI", pdfDpi);
        prefs.putLong("TOKEN_SESSION_TIMEOUT", tokenSessionTimeout);
        prefs.put("CUSTOM_PKCS11_DRIVER_PATH", customPKCS11DriverPath);

        StringBuilder builder = new StringBuilder();
        for (var entry : driverSlotIndexMap.entrySet()) {
            builder.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        prefs.put("DRIVER_SLOT_INDEX_MAP", builder.toString());
    }

    public void reset() {
        setLanguage(SupportedLanguage.SYSTEM);
        setSignatureType(DEFAULT_SIGNATURE_LEVEL);
        setDriver(DEFAULT_DRIVER);
        setEn319132(DEFAULT_EN319132);
        setBulkEnabled(DEFAULT_BULK_ENABLED);
        setPlainXmlEnabled(DEFAULT_PLAIN_XML_ENABLED);
        setSignIndividually(DEFAULT_SIGN_INDIVIDUALLY);
        setCorrectDocumentDisplay(DEFAULT_CORRECT_DOCUMENT_DISPLAY);
        setSignaturesValidity(DEFAULT_SIGNATURES_VALIDITY);
        setPdfaCompliance(DEFAULT_PDFA_COMPLIANCE);
        setServerEnabled(DEFAULT_SERVER_ENABLED);
        setExpiredCertsEnabled(DEFAULT_EXPIRED_CERTS_ENABLED);
        setTrustedList(DEFAULT_TRUSTED_LIST);
        setCustomKeystorePath(DEFAULT_CUSTOM_KEYSTORE_PATH);
        setTsaServer(DEFAULT_TSA_SERVER);
        setCustomTsaServer(DEFAULT_CUSTOM_TSA_SERVER);
        setTsaEnabled(DEFAULT_TSA_ENABLE);
        setPdfDpi(DEFAULT_PDF_DPI);
        setTokenSessionTimeout(DEFAULT_TOKEN_SESSION_TIMEOUT);
        setCustomPKCS11DriverPath(DEFAULT_CUSTOM_PKCS11_DRIVER_PATH);
        driverSlotIndexMap.clear();
        driverSlotIndexMap.put("default", -1); // default slot index

        save();
    }

    private void setSignatureType(String signatureType) {
        var signatureLevelStringConverter = new SignatureLevelStringConverter();

        this.signatureLevel = Stream.of(SignatureLevel.XAdES_BASELINE_B, SignatureLevel.PAdES_BASELINE_B, SignatureLevel.CAdES_BASELINE_B)
                .map(signatureLevelStringConverter::toString)
                .filter(sl -> sl.equals(signatureType))
                .map(signatureLevelStringConverter::fromString)
                .findFirst().orElse(SignatureLevel.PAdES_BASELINE_B);
    }

    private void setTrustedList(String trustedList) {
        this.trustedList = trustedList == null ? new ArrayList<>() : new ArrayList<>(List.of(trustedList.split(",")));
    }

    public Optional<SupportedLanguage> getLanguage() {
        return Optional.ofNullable(language);
    }

    /**
     * @return preferred language of the user, or default (system) language
     */
    @Override
    public Locale getLanguageLocale() {
        return getLanguage().map(SupportedLanguage::getLocale)
                .orElse(Locale.getDefault());
    }

    public void setLanguage(SupportedLanguage language) {
        this.language = language;
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

    public boolean isPlainXmlEnabled() {
        return plainXmlEnabled;
    }

    public void setPlainXmlEnabled(boolean value) {
        this.plainXmlEnabled = value;
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

    public String getTsaServer() {
        return tsaServer;
    }

    public void setTsaServer(String value) {
        if (value == null || value.isEmpty()) {
            tspSource = null;
            return;
        }

        // set default TSA if older problematic default is set
        if (List.of("http://tsa.izenpe.com", "http://kstamp.keynectis.com/KSign/").contains(value))
            value = "http://tsa.baltstamp.lt,http://ts.quovadisglobal.com/eu";

        tsaServer = value;
        tspSource = new CompositeTSPSource();
        var timestampDataLoader = new TimestampDataLoader();
        var tspSources = new LinkedHashMap<String, TSPSource>();
        for (var tsaServer : tsaServer.split(","))
            tspSources.put(tsaServer, new OnlineTSPSource(tsaServer, timestampDataLoader));

        tspSource.setTspSources(tspSources);
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
    public int getDriverSlotIndex(String tokenDriverShortname) {
        return driverSlotIndexMap.getOrDefault(tokenDriverShortname, driverSlotIndexMap.getOrDefault("default", -1));
    }

    public void setDriverSlotIndex(String tokenDriverShortname, int index) {
        driverSlotIndexMap.put(tokenDriverShortname, index);
    }

    public DriverDetector getDriverDetector() {
        return new DefaultDriverDetector(this);
    }

    public boolean isBulkEnabled() {
        return bulkEnabled;
    }

    public int getPdfDpi() {
        return pdfDpi;
    }

    public void setPdfDpi(int value) {
        pdfDpi = value;
    }

    public long getTokenSessionTimeout() {
        return tokenSessionTimeout;
    }

    public void setTokenSessionTimeout(long value) {
        if (value <= 0)
            return;

        tokenSessionTimeout = value;
    }

    public String getCustomPKCS11DriverPath() {
        return customPKCS11DriverPath;
    }

    public void setCustomPKCS11DriverPath(String driverPath) {
        Path path = Paths.get(driverPath);
        if (! Files.exists(path)) {
            return;
        }
        customPKCS11DriverPath = driverPath;
    }
}