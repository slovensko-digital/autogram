package com.octosign.whitelabel.ui;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.octosign.whitelabel.signing.SigningCertificatePKCS12;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;
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
     * Loads and sets default signing certificate
     */
    public SigningCertificate useDefault() {
        certificate = getDefaulCertificate();
        return certificate;
    }

    /**
     * Use dialog picker to choose the certificate
     */
    public SigningCertificate useDialogPicker() {
        Dialog<SigningCertificate> dialog = new Dialog<>();
        dialog.setTitle(translate("text.certSettings"));
        FXUtils.addCustomStyles(dialog);

//        var treeTableView = new TreeTableView<SigningCertificate>();
//        var nameColumn = new TreeTableColumn<SigningCertificate, String>(translate("text.subjectName"));
//        nameColumn.setCellValueFactory((cert) ->
//                        new SimpleStringProperty(
//                                cert.getValue().getValue().getNicePrivateKeyDescription(Verbosity.LONG)
//                        )
//        );
//        treeTableView.getColumns().add(nameColumn);
//        treeTableView.setRoot(new TreeItem<>(certificate));
//        dialog.getDialogPane().setContent(treeTableView);
        var buttonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);

        return dialog.showAndWait().get();
    }

    /**
     * Currently uses certificate
     */
    public SigningCertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(SigningCertificate certificate) {
        this.certificate = certificate;
    }

    /**
     * Tries to automatically choose the most appropriate token and private key
     *
     */
    public static SigningCertificate getDefaulCertificate() {
        SigningCertificate signingCertificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback());

        // Try to fallback to MSCAPI on Windows
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if (signingCertificate == null && osName.contains("win")) {
            signingCertificate = new SigningCertificateMSCAPI();
        }

        if (signingCertificate == null)
            throw new UserException("error.tokenNotFound.header", "error.tokenNotFound.description");

        List<DSSPrivateKeyEntry> keys = signingCertificate.getAvailablePrivateKeys();

        if (keys.size() == 0)
            throw new UserException("error.tokenEmpty.header", "error.tokenEmpty.description");

        // Use the first available key
        signingCertificate.setPrivateKey(keys.get(0));

        return signingCertificate;
    }

    public static List<SigningCertificate> getEidCerts() {
        var certificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback());
        return List.of(certificate);
    }

    public static List<SigningCertificate> getMandateCerts() {
        return List.of(
            SigningCertificatePKCS12.create(getPropertyArray("[].drivers.pkcs12.linux")[0], "null"),
            SigningCertificatePKCS12.create(getPropertyArray("[].drivers.pkcs12.linux")[0], "null"),
            SigningCertificatePKCS12.create(getPropertyArray("[].drivers.pkcs12.linux")[0], "null")
        );
    }

    public static Map<String, Supplier<List<SigningCertificate>>> getSelectionParameters() {
        return Map.of(
            "Podpísať pomocou eID", CertificateManager::getEidCerts,
            "Podpísať mandátnym certifikátom", CertificateManager::getMandateCerts
        );
    }
}
