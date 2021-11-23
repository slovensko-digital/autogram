package com.octosign.whitelabel.signing;

import com.octosign.whitelabel.error_handling.UserException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getProperty;
import static com.octosign.whitelabel.ui.ConfigurationProperties.getPropertyArray;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.signing.Driver.*;


public class SigningCertificatePKCS11 extends SigningCertificate {

    public static final String[] LINUX_EID_DLLS = getPropertyArray("[].drivers.pkcs11.eid.linux");
    public static final String[] WINDOWS_EID_DLLS = getPropertyArray("[].drivers.pkcs11.eid.windows"); // Slovak eID default installation directory
    public static final String[] DARWIN_EID_DLLS = getPropertyArray("[].drivers.pkcs11.eid.darwin"); // Slovak eID default installation directory

    public SigningCertificatePKCS11(AbstractKeyStoreTokenConnection token, DSSPrivateKeyEntry privateKey) {
        super(token, privateKey);
    }

    public static String[] resolvePkcsDriverPath() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return DARWIN_EID_DLLS;
        } else if (osName.contains("win")) {
            return WINDOWS_EID_DLLS;
        } else if (osName.contains("nux")) {
            return LINUX_EID_DLLS;
        } else {
            throw new UserException("error.unknownOS.header", translate("error.unknownOS.description", osName));
        }
    }

    public static List<Driver> getDrivers() {
        List<Driver> drivers = new ArrayList<>();

        if (new File(getProperty("driver.pkcs11.eidCert.linux")).exists())
            drivers.add(Type.EID.getDriver());

        if (new File(getProperty("driver.pkcs11.mandateCert.linux")).exists())
            drivers.add(Type.MANDATE.getDriver());

        if (drivers.isEmpty())
            throw new UserException("No driver found!!");

        return drivers;
    }
}
