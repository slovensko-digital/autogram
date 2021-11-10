package com.octosign.whitelabel.ui.certificate_picker;

import com.octosign.whitelabel.signing.SigningCertificate;
import com.octosign.whitelabel.ui.FXUtils;
import com.octosign.whitelabel.ui.Main;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;

import java.util.stream.Collectors;

import static com.octosign.whitelabel.ui.I18n.translate;

public class SelectCertificateDialog extends Dialog<SigningCertificate> {
    private static final int MIN_WIDTH = 520;
    private static final String[] DRIVER_PATHS = { "/etc/" };

    public SelectCertificateDialog() {
        super();
        setTitle(translate("text.selectCert.header"));
        setContentText(getContent());

        FXUtils.addStylesheets(this);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(MIN_WIDTH);
        getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
    }

    private static String getContent() {
        String appDescription = translate("text.selectCert.description", Main.getVersion(), translate("app.author"));

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
        //dialog.getDialogPane().getButtonTypes().add(buttonType);
        return "";
    }

}
