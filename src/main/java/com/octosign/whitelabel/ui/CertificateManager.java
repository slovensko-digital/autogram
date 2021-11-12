package com.octosign.whitelabel.ui;

import java.util.List;
import java.util.Locale;

import javafx.scene.control.*;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import com.octosign.whitelabel.signing.*;
import com.octosign.whitelabel.error_handling.UserException;

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
        SigningCertificate certificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback());

        // Try to fallback to MSCAPI on Windows
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        if (certificate == null && osName.contains("win")) {
            certificate = new SigningCertificateMSCAPI();
        }

        if (certificate == null)
            throw new UserException("error.tokenNotFound.header", "error.tokenNotFound.description");

        List<DSSPrivateKeyEntry> keys = certificate.getAvailablePrivateKeys();

        if (keys.size() == 0)
            throw new UserException("error.tokenEmpty.header", "error.tokenEmpty.description");

        // Use the first available key
        certificate.setPrivateKey(keys.get(0));

        return certificate;
    }
}
