package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;
import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.List;
import java.util.Locale;

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
        FXUtils.addStylesheets(dialog);

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

    /**
     * Tries to automatically choose the most appropriate token and private key
     *
     */
    private static SigningCertificate getDefaulCertificate() {
        SigningCertificate signingCertificate;

        try { signingCertificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback()); }
        catch (Exception e) { throw new IntegrationException(Code.PKCS11_INIT_FAILED, e); }

        // Try to fallback to MSCAPI on Windows
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if (signingCertificate == null && osName.contains("win")) {
            try { signingCertificate = new SigningCertificateMSCAPI(); }
            catch (Exception e) { throw new IntegrationException(Code.MSCAPI_INIT_FAILED, e); }
        }

        if (signingCertificate == null)
            throw new UserException("error.tokenNotFound.header", "error.tokenNotFound.description");

        List<DSSPrivateKeyEntry> keys;
        try {
            keys = signingCertificate.getAvailablePrivateKeys();
        } catch (Exception e) {
            throw new UserException("error.tokenNotAvailable.header", "error.tokenNotAvailable.description", e);
        }

        if (keys.size() == 0)
            throw new UserException("error.tokenEmpty.header", "error.tokenEmpty.description");

        // Use the first available key
        signingCertificate.setPrivateKey(keys.get(0));

        return signingCertificate;
    }
}
