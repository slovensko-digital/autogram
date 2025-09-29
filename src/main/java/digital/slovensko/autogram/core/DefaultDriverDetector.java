package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.FakeTokenDriver;
import digital.slovensko.autogram.drivers.PKCS11TokenDriver;
import digital.slovensko.autogram.drivers.PKCS12KeystoreTokenDriver;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.SupportedLanguage;
import digital.slovensko.autogram.util.OperatingSystem;

import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import static digital.slovensko.autogram.ui.gui.HasI18n.translate;

public class DefaultDriverDetector implements DriverDetector {
    private final DriverDetectorSettings settings;

    public static class TokenDriverShortnames {
        public static final String EID = "eid";
        public static final String CZ_EID = "cz_eid";
        public static final String SECURE_STORE = "secure_store";
        public static final String MONET = "monet";
        public static final String GEMALTO = "gemalto";
        public static final String FAKE = "fake";
        public static final String KEYSTORE = "keystore";
        public static final String CUSTOM_PKCS11 = "custom_pkcs11";
    }

    private final String EMPTY_HELPER_TEXT = "";

    public DefaultDriverDetector(DriverDetectorSettings settings) {
        this.settings = settings;
    }

    private List<TokenDriver> getLinuxDrivers(ResourceBundle r){
        return List.of(
            new PKCS11TokenDriver(translate(r, "tokenDriver.eid.label"), Path.of("/usr/lib/eID_klient/libpkcs11_x64.so"), TokenDriverShortnames.EID, translate(r, "tokenDriver.eid.text")),
            new PKCS11TokenDriver(translate(r, "tokenDriver.eid.old.label"), Path.of("/usr/lib/eac_mw_klient/libpkcs11_x64.so"), TokenDriverShortnames.EID, translate(r, "tokenDriver.eid.text")),
            new PKCS11TokenDriver(translate(r, "tokenDriver.cz.eid.label"), Path.of("/usr/lib/x86_64-linux-gnu/libeopproxy11.so"), TokenDriverShortnames.CZ_EID, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("/usr/lib/pkcs11/libICASecureStorePkcs11.so"), TokenDriverShortnames.SECURE_STORE, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of("/usr/lib/x86_64-linux-gnu/libproidqcm11.so"), TokenDriverShortnames.MONET, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("/usr/lib/libIDPrimePKCS11.so"), TokenDriverShortnames.GEMALTO, translate(r, "tokenDriver.gemalto.text")),
            new PKCS12KeystoreTokenDriver(translate(r, "tokenDriver.keystore.label"), Path.of(settings.getCustomKeystorePath()), TokenDriverShortnames.KEYSTORE, EMPTY_HELPER_TEXT),
            new FakeTokenDriver(translate(r, "tokenDriver.fake.label"),  Path.of("fakeTokenDriver"), TokenDriverShortnames.FAKE, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver(translate(r, "tokenDriver.custom.pkcs11.label"), Path.of(settings.getCustomPKCS11DriverPath()), TokenDriverShortnames.CUSTOM_PKCS11, EMPTY_HELPER_TEXT)
        );
    }

    private List<TokenDriver> getWindowsDrivers(ResourceBundle r) {
        return List.of(
            new PKCS11TokenDriver(translate(r, "tokenDriver.eid.label"), Path.of("C:\\Program Files (x86)\\eID_klient\\pkcs11_x86.dll"), TokenDriverShortnames.EID, translate(r, "tokenDriver.eid.text")),
            new PKCS11TokenDriver("eObčanka", Path.of("C:\\Windows\\System32\\eopproxyp11.dll"), TokenDriverShortnames.CZ_EID, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("C:\\Windows\\System32\\SecureStorePkcs11.dll"), TokenDriverShortnames.SECURE_STORE, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of( "C:\\Windows\\system32\\proidqcm11.dll"), TokenDriverShortnames.MONET, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("C:\\Windows\\System32\\eTPKCS11.dll"), TokenDriverShortnames.GEMALTO, translate(r, "tokenDriver.gemalto.text")),
            new PKCS12KeystoreTokenDriver(translate(r, "tokenDriver.keystore.label"), Path.of(settings.getCustomKeystorePath()), TokenDriverShortnames.KEYSTORE, EMPTY_HELPER_TEXT),
            new FakeTokenDriver(translate(r, "tokenDriver.fake.label"),  Path.of("fakeTokenDriver"), TokenDriverShortnames.FAKE, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver(translate(r, "tokenDriver.custom.pkcs11.label"), Path.of(settings.getCustomPKCS11DriverPath()), TokenDriverShortnames.CUSTOM_PKCS11, EMPTY_HELPER_TEXT)
        );
    }

    private List<TokenDriver> getMacDrivers(ResourceBundle r) {
        return List.of(
            new PKCS11TokenDriver(translate(r, "tokenDriver.eid.label"), Path.of("/Applications/eID_klient.app/Contents/Frameworks/libPkcs11.dylib"), TokenDriverShortnames.EID, translate(r, "tokenDriver.eid.text")),
            new PKCS11TokenDriver("eObčanka", Path.of("/usr/local/lib/eOPCZE/libeopproxyp11.dylib"), TokenDriverShortnames.CZ_EID, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("/usr/local/lib/pkcs11/libICASecureStorePkcs11.dylib"), TokenDriverShortnames.SECURE_STORE, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of("/usr/local/lib/ProIDPlus/libproidqcm11.dylib"), TokenDriverShortnames.MONET, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("/usr/local/lib/libIDPrimePKCS11.dylib"), TokenDriverShortnames.GEMALTO, translate(r, "tokenDriver.gemalto.text")),
            new PKCS12KeystoreTokenDriver(translate(r, "tokenDriver.keystore.label"), Path.of(settings.getCustomKeystorePath()), TokenDriverShortnames.KEYSTORE, EMPTY_HELPER_TEXT),
            new FakeTokenDriver(translate(r, "tokenDriver.fake.label"),  Path.of("fakeTokenDriver"), TokenDriverShortnames.FAKE, EMPTY_HELPER_TEXT),
            new PKCS11TokenDriver(translate(r, "tokenDriver.custom.pkcs11.label"), Path.of(settings.getCustomPKCS11DriverPath()), TokenDriverShortnames.CUSTOM_PKCS11, EMPTY_HELPER_TEXT)
        );
    }

    public List<TokenDriver> getAvailableDrivers() {
        var resources = SupportedLanguage.loadResources(settings);
        return getAllDrivers(resources).stream().filter(TokenDriver::isInstalled).toList();
    }

    private List<TokenDriver> getAllDrivers(ResourceBundle resources) {
        switch (OperatingSystem.current()) {
            case WINDOWS -> {
                return getWindowsDrivers(resources);
            }
            case LINUX -> {
                return getLinuxDrivers(resources);
            }
            case MAC -> {
                return getMacDrivers(resources);
            }
            default -> throw new IllegalStateException("Unexpected value: " + OperatingSystem.current());
        }
    }
}
