package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.util.List;
import java.util.Locale;

import static com.octosign.whitelabel.ui.FX.displayError;

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
        dialog.setTitle(Main.getProperty("txt.certSettings"));
        FX.addStylesheets(dialog);

        var treeTableView = new TreeTableView<SigningCertificate>();
        var nameColumn = new TreeTableColumn<SigningCertificate, String>(Main.getProperty("txt.subjectName"));

    /*    nameColumn.setCellValueFactory(input ->
                ofNullable(input.getValue())
                        .map(TreeItem::getValue)
                        .map(certificate -> certificate.getNicePrivateKeyDescription(LONG))
                        .map(SimpleStringProperty::new)
                        .orElse(null)
        );*/

        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("olala "));
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("olbla "));
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("olcla "));
        treeTableView.getColumns().add(nameColumn);
        treeTableView.setRoot(new TreeItem<>(certificate));
//        dialogPane.setContent(treeTableView);
        ButtonType t = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(t);

        return dialog.showAndWait().orElse(null);
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
     * TODO: All strings here should come from the properties
     */
    private static SigningCertificate getDefaulCertificate() {
        SigningCertificate certificate = SigningCertificatePKCS11.createFromDetected(new PasswordCallback());

        // Try to fallback to MSCAPI on Windows
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (certificate == null && osName.contains("win")) {
            certificate = new SigningCertificateMSCAPI();
        }

        if (certificate == null) {
            displayError("error.tokenNotFound.header", "error.tokenNotFound.description");
            return null;
        }

        List<DSSPrivateKeyEntry> keys;
        try {
            keys = certificate.getAvailablePrivateKeys();
        } catch (Exception e) {
            displayError("error.tokenNotAvailable.header", "error.tokenNotAvailable.description", e);
            return null;
        }

        if (keys.size() == 0) {
            displayError("error.tokenEmpty.header", "error.tokenEmpty.description");
            return null;
        }

        // Use the first available key
        certificate.setPrivateKey(keys.get(0));

        return certificate;
    }
}
