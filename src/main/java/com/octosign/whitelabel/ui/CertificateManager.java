package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.signing.SigningCertificateMSCAPI;
import com.octosign.whitelabel.signing.SigningCertificatePKCS11;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.util.List;
import java.util.Locale;

import static com.octosign.whitelabel.signing.SigningCertificate.KeyDescriptionVerbosity.LONG;
import static com.octosign.whitelabel.ui.FX.displayError;
import static com.octosign.whitelabel.ui.I18n.getProperty;
import static com.octosign.whitelabel.ui.Main.getProperty;
import static java.util.Optional.ofNullable;

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
        // TODO: Move out and implement actual logic

        Dialog<SigningCertificate> dialog = new Dialog<>();
        dialog.setTitle(getProperty("text.certificateSettings"));

        FX.addStylesheets(dialog);
        var treeTableView = new TreeTableView<SigningCertificate>();
        var nameColumn = new TreeTableColumn<SigningCertificate, String>(getProperty("text.subjectName"));
        nameColumn.setCellValueFactory(input ->
                ofNullable(input.getValue())
                        .map(TreeItem::getValue)
                        .map(certificate -> certificate.getNicePrivateKeyDescription(LONG))
                        .map(SimpleStringProperty::new)
                        .orElse(null)
        );
        treeTableView.getColumns().add(nameColumn);
        treeTableView.setRoot(new TreeItem<>(certificate));
        dialogPane.setContent(treeTableView);

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
            displayError("tokenNotFound");
            return null;
        }

        List<DSSPrivateKeyEntry> keys;
        try {
            keys = certificate.getAvailablePrivateKeys();
        } catch (Exception e) {
            displayError("tokenNotAvailable", e);
            return null;
        }

        if (keys.size() == 0) {
            displayError("tokenEmpty");
            return null;
        }

        // Use the first available key
        certificate.setPrivateKey(keys.get(0));

        return certificate;
    }
}
