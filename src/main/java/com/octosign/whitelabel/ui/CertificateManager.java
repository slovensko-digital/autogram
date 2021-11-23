package com.octosign.whitelabel.ui;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.octosign.whitelabel.signing.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import com.octosign.whitelabel.error_handling.*;

import static com.octosign.whitelabel.ui.ConfigurationProperties.getPropertyArray;
import static com.octosign.whitelabel.ui.I18n.translate;

/**
 * Holds currently used certificate and takes care of picking
 */
public class CertificateManager {
    /**
     * Currently selected certificate
     */
    private SigningCertificate certificate;

    /**
     * Currently uses certificate
     */
    public SigningCertificate getCertificate() {
        return certificate;
    }

    /**
     * Use dialog picker to choose the certificate
     */
    public SigningCertificate useDialogPicker() {
        Dialog<SigningCertificate> dialog = new Dialog<>();
        dialog.setTitle(translate("text.certSettings"));
        FXUtils.addCustomStyles(dialog);

        var buttonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);

        return dialog.showAndWait().get();
    }

    public void setCertificate(SigningCertificate certificate) {
        this.certificate = certificate;
    }

    public List<Driver> getAvailableDrivers() {
        return SigningCertificatePKCS11.getDrivers();
    }
}
