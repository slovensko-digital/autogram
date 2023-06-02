package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.util.OperatingSystem;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

import java.nio.file.Path;
import java.util.List;

import static digital.slovensko.autogram.core.Configuration.getProperty;

public abstract class TokenDriver {
    public static final List<TokenDriver> LINUX_DRIVERS = List.of(
            new PKCS11TokenDriver("Občiansky preukaz (eID klient)", Path.of("/usr/lib/eID_klient/libpkcs11_x64.so"), false),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("/usr/lib/pkcs11/libICASecureStorePkcs11.so"), true),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of("/usr/lib/x86_64-linux-gnu/libproidqcm11.so"), true),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("/usr/lib/libIDPrimePKCS11.so"), true),
            new FakeTokenDriver("Fake token driver",  Path.of("fakeTokenDriver.so"), false)
    );

    public static final List<TokenDriver> WINDOWS_DRIVERS = List.of(
            new PKCS11TokenDriver("Občiansky preukaz (eID klient)", Path.of("C:\\Program Files (x86)\\eID_klient\\pkcs11_x64.dll"), false),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("C:\\Windows\\System32\\SecureStorePkcs11.dll"), true),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of( "C:\\Windows\\system32\\proidqcm11.dll"), true),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("C:\\Windows\\System32\\eTPKCS11.dll"), true),
            new FakeTokenDriver("Fake token driver",  Path.of("fakeTokenDriver.dll"), false)
    );

    public static final List<TokenDriver> MAC_DRIVERS = List.of(
            new PKCS11TokenDriver("Občiansky preukaz (eID klient)", Path.of("/Applications/eID_klient.app/Contents/Frameworks/libPkcs11.dylib"), false),
            new PKCS11TokenDriver("I.CA SecureStore", Path.of("/usr/local/lib/pkcs11/libICASecureStorePkcs11.dylib"), true),
            new PKCS11TokenDriver("MONET+ ProID+Q", Path.of("/usr/local/lib/ProIDPlus/libproidqcm11.dylib"), true),
            new PKCS11TokenDriver("Gemalto IDPrime 940", Path.of("/usr/local/lib/libIDPrimePKCS11.dylib"), true),
            new FakeTokenDriver("Fake token driver",  Path.of("fakeTokenDriver.dylib"), false)
    );

    protected final String name;
    private final Path path;
    private final boolean needsPassword;

    public TokenDriver(String name, Path path, boolean needsPassword) {
        this.name = name;
        this.path = path;
        this.needsPassword = needsPassword;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return this.path;
    }

    public boolean isInstalled() {
        return path.toFile().exists();
    }

    public static List<TokenDriver> getAvailableDrivers() {
        return getAllDrivers().stream().filter(TokenDriver::isInstalled).toList();
    }

    private static List<TokenDriver> getAllDrivers() {
        switch (OperatingSystem.current()) {
            case WINDOWS -> {
                return WINDOWS_DRIVERS;
            }
            case LINUX -> {
                return LINUX_DRIVERS;
            }
            case MAC -> {
                return MAC_DRIVERS;
            }
            default -> throw new IllegalStateException("Unexpected value: " + OperatingSystem.current());
        }
    }

    public abstract AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password);

    public boolean needsPassword() {
        return needsPassword;
    }
}
