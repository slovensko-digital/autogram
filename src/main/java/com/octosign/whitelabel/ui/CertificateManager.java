package com.octosign.whitelabel.ui;

import java.util.List;
import java.util.Locale;

import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.scene.control.Alert.AlertType;

/**
 * Holds currently used certificate and takes care of picking
 */
public class CertificateManager {
    /**
     * Currently selected certificate
     */
    private SigningCertificate certificate;

    /**
     * Loads and sets default signing certificate
     */
    public void useDefault() {
        certificate = getDefaulCertificate();
    }

    /**
     * Currently uses certificate
     */
    public SigningCertificate getCertificate() {
        return certificate;
    }

    /**
     * Tries to automatically choose the most appropriate token and private key
     *
     * Exits the application if the token or private key is not found
     *
     * TODO: All strings here should come from the properties
     */
    private SigningCertificate getDefaulCertificate() {
        SigningCertificate certificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback());

        // Try to fallback to MSCAPI on Windows
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (certificate == null && osName.indexOf("win") >= 0) {
            certificate = new SigningCertificateMSCAPI();
        }
    
        if (certificate == null) {
            Main.displayAlert(
                AlertType.ERROR,
                "Zlyhanie načítania",
                "Podpisovací token nenájdený",
                "Nepodarilo sa nájsť žiaden podporovaný token vhodný na podpisovanie. Uistite sa, že máte nainštalovaný softvér dodávaný s tokenom a skúste to znova."
            );
            System.exit(1);
            return null;
        }
    
        List<DSSPrivateKeyEntry> keys;
        try {
            keys = certificate.getAvailablePrivateKeys();
        } catch (Exception e) {
            Main.displayAlert(
                AlertType.ERROR,
                "Zlyhanie načítania",
                "Podpisovací token nedostupný",
                "Použitie podpisovacieho tokenu zlyhalo. Uistite sa, že máte správne pripavené podpisovacie zariadenie a skúste to znova. Detail chyby: " + e
            );
            System.exit(1);
            return null;
        }
    
        if (keys.size() == 0) {
            Main.displayAlert(
                AlertType.ERROR,
                "Zlyhanie načítania",
                "Podpisovací token prázdny",
                "Podporovaný podpisovací token neobsahuje použiteľný certifikát. Uistite sa, že máte správne nastavený token a skúste to znova."
            );
            System.exit(1);
            return null;
        }
    
        // Use the first available key
        certificate.setPrivateKey(keys.get(0));

        return certificate;
    }
}
