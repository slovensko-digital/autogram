package digital.slovensko.autogram.drivers;

import java.io.IOException;

public class Pkcs11LibraryException extends IOException {
    public Pkcs11LibraryException(String path) {
        super("Unable to load PKCS#11 library " + path);
    }
}
