package com.octosign.whitelabel.signing.token;

import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.Certificate;
import com.octosign.whitelabel.signing.Driver;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import java.util.*;

import static com.octosign.whitelabel.signing.KeystoreType.PKCS11;
import static digital.slovensko.autogram.util.OperatingSystem.*;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.utils.Utils.isNullOrEmpty;

public abstract class Token {
    private static List<Driver> DRIVERS;

    private SignatureTokenConnection dssToken;

    private List<Certificate> certificates;

    protected Driver driver;

    protected void initialize(SignatureTokenConnection dssToken) {
        this.dssToken = dssToken;
        this.certificates = buildCertificates();
    }

    public static Token fromDriver(Driver driver) {
        return driver.getKeystoreType().createToken(driver);
    }

    public SignatureTokenConnection getDssToken() {
        return dssToken;
    }

    protected List<Certificate> buildCertificates() {
        return getAllPrivateKeys().stream()
                                  .map(key -> new Certificate(key, this))
                                  .toList();
    }

    public List<Certificate> getCertificates() {
        if (isNullOrEmpty(certificates))
            certificates = buildCertificates();

        return certificates;
    }

    protected List<DSSPrivateKeyEntry> getAllPrivateKeys() {
        List<DSSPrivateKeyEntry> keys;
        try {
            keys = dssToken.getKeys();
        } catch (Exception e) {
            throw new UserException("error.tokenNotAvailable.header", "error.tokenNotAvailable.description", e);
        }

        return keys;
    }

    public static List<Driver> getAvailableDrivers() {
        return getDrivers().stream()
                           .filter(Driver::isCompatible)
                           .filter(Driver::isInstalled)
                           .toList();
    }

    public static Collection<Driver> getDrivers() {
        if (DRIVERS == null) loadDrivers();
        return DRIVERS;
    }

    private static void loadDrivers() {
        DRIVERS = new ArrayList<>();

        // PKCS11 drivers
        DRIVERS.addAll(List.of(
                Driver.name("eID")
                      .tokenType(translate("text.idCard"))
                      .file(WINDOWS, "C:\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll")
                      .file(LINUX, "/usr/lib/eac_mw_klient/libpkcs11_x64.so")
                      .file(MAC, "/Applications/Aplikacia_pre_eID.app/Contents/pkcs11/libPkcs11.dylib")
                      .keystore(PKCS11),

                Driver.name("eIDv4")
                        .tokenType(translate("text.idCard"))
                        .file(WINDOWS, "C:\\Program Files (x86)\\eID_klient\\pkcs11_x64.dll")
                        .file(LINUX, "/usr/lib/eID_klient/libpkcs11_x64.so")
                      .file(MAC, "/Applications/eID_klient.app/Contents/Frameworks/libPkcs11.dylib")
                      .keystore(PKCS11),

                Driver.name("ICASecureStore")
                      .tokenType(translate("text.mandateCertificate"))
//                      .file(WINDOWS, "C:\\Program Files\\I.CA SecureStore\\ICASecureStorePkcs11.dll")
                      .file(WINDOWS, "C:\\Windows\\System32\\SecureStorePkcs11.dll")
                      .file(LINUX, "/usr/lib/pkcs11/libICASecureStorePkcs11.so")
                      .file(MAC, "/usr/local/lib/pkcs11/libICASecureStorePkcs11.dylib")
                      .keystore(PKCS11),

                Driver.name("Disig")
                        .tokenType(translate("text.mandateCertificate"))
                        .file(WINDOWS, "C:\\Windows\\System32\\eTPKCS11.dll")
                        .file(LINUX, "/usr/lib/libIDPrimePKCS11.so")
                        .file(MAC, "/usr/local/lib/libIDPrimePKCS11.dylib")
                        .keystore(PKCS11),

                Driver.name("Disig ProID+")
                        .tokenType(translate("text.mandateCertificate"))
                        .file(WINDOWS, "C:\\Windows\\system32\\proidqcm11.dll")
                        .file(LINUX, "/usr/lib/x86_64-linux-gnu/libproidqcm11.so")
                        .file(MAC, "/usr/local/lib/ProIDPlus/libproidqcm11.dylib")
                        .keystore(PKCS11)
                )
        );
    }
}
