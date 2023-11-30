package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.FakeTokenDriver;
import digital.slovensko.autogram.drivers.PKCS11TokenDriver;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.PKCS12KeystoreTokenDriver;
import digital.slovensko.autogram.util.OperatingSystem;

import java.nio.file.Path;
import java.util.List;

public class DefaultDriverDetector implements DriverDetector {
    private final DriverDetectorSettings settings;

    public static class TokenDriverShortnames {
        public static final String EID = "eid";
        public static final String SECURE_STORE = "secure_store";
        public static final String MONET = "monet";
        public static final String GEMALTO = "gemalto";
        public static final String FAKE = "fake";
        public static final String KEYSTORE = "keystore";
    }

    public DefaultDriverDetector(DriverDetectorSettings settings) {
        this.settings = settings;
    }

    private List<TokenDriver> getLinuxDrivers(){
        return List.of(
            new PKCS11TokenDriver("Občiansky preukaz (eID klient)", Path.of("/usr/lib/eID_klient/libpkcs11_x64.so"), TokenDriverShortnames.EID),
            new PKCS11TokenDriver("Občiansky preukaz (starý eID klient)", Path.of("/usr/lib/eac_mw_klient/libpkcs11_x64.so"), TokenDriverShortnames.EID),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("/usr/lib/pkcs11/libICASecureStorePkcs11.so"), TokenDriverShortnames.SECURE_STORE),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of("/usr/lib/x86_64-linux-gnu/libproidqcm11.so"), TokenDriverShortnames.MONET),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("/usr/lib/libIDPrimePKCS11.so"), TokenDriverShortnames.GEMALTO),
            new PKCS12KeystoreTokenDriver("Zo súboru", Path.of(settings.getCustomKeystorePath()), TokenDriverShortnames.KEYSTORE),
            new FakeTokenDriver("Fake token driver",  Path.of("fakeTokenDriver"), TokenDriverShortnames.FAKE)
        );
    }

    private List<TokenDriver> getWindowsDrivers() {
        return List.of(
            new PKCS11TokenDriver("Občiansky preukaz (eID klient)", Path.of("C:\\Program Files (x86)\\eID_klient\\pkcs11_x64.dll"), TokenDriverShortnames.EID),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("C:\\Windows\\System32\\SecureStorePkcs11.dll"), TokenDriverShortnames.SECURE_STORE),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of( "C:\\Windows\\system32\\proidqcm11.dll"), TokenDriverShortnames.MONET),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("C:\\Windows\\System32\\eTPKCS11.dll"), TokenDriverShortnames.GEMALTO),
            new PKCS12KeystoreTokenDriver("Zo súboru", Path.of(settings.getCustomKeystorePath()), TokenDriverShortnames.KEYSTORE),
            new FakeTokenDriver("Fake token driver",  Path.of("fakeTokenDriver"), TokenDriverShortnames.FAKE)
        );
    }

    private List<TokenDriver> getMacDrivers() {
        return List.of(
            new PKCS11TokenDriver("Občiansky preukaz (eID klient)", Path.of("/Applications/eID_klient.app/Contents/Frameworks/libPkcs11.dylib"), TokenDriverShortnames.EID),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("/usr/local/lib/pkcs11/libICASecureStorePkcs11.dylib"), TokenDriverShortnames.SECURE_STORE),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of("/usr/local/lib/ProIDPlus/libproidqcm11.dylib"), TokenDriverShortnames.MONET),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("/usr/local/lib/libIDPrimePKCS11.dylib"), TokenDriverShortnames.GEMALTO),
            new PKCS12KeystoreTokenDriver("Zo súboru", Path.of(settings.getCustomKeystorePath()), TokenDriverShortnames.KEYSTORE),
            new FakeTokenDriver("Fake token driver",  Path.of("fakeTokenDriver"), TokenDriverShortnames.FAKE)
        );
    }

    public List<TokenDriver> getAvailableDrivers() {
        return getAllDrivers().stream().filter(TokenDriver::isInstalled).toList();
    }

    private List<TokenDriver> getAllDrivers() {
        switch (OperatingSystem.current()) {
            case WINDOWS -> {
                return getWindowsDrivers();
            }
            case LINUX -> {
                return getLinuxDrivers();
            }
            case MAC -> {
                return getMacDrivers();
            }
            default -> throw new IllegalStateException("Unexpected value: " + OperatingSystem.current());
        }
    }
}
