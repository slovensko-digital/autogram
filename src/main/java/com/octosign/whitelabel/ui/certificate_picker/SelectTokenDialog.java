package com.octosign.whitelabel.ui.certificate_picker;

import com.octosign.whitelabel.ui.FXUtils;
import com.octosign.whitelabel.ui.Main;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;

import java.util.Map;

import static com.octosign.whitelabel.ui.I18n.translate;

public class SelectTokenDialog extends Dialog<String> {
    private static final int MIN_WIDTH = 520;
    private static final Map<String, String> OPTIONS = Map.of("eID certificate",
        "karticka obciansky",
        "mandate certoifocate",
        "mandatny",
        "unknown certificate",
        "skutocne netusim");

    public SelectTokenDialog() {
        super();
        setTitle(translate("text.dialog.selectToken.header"));
        setContentText(getContent());

        FXUtils.addCustomStyles(this);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(MIN_WIDTH);
        getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
    }

    private static String getContent() {
        String appDescription = translate("text.dialog.selectToken.header",
            Main.getVersion(),
            translate("app.author"));

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
//public class SelectTokenPopup extends PopupControl {
//
//
//
//    public static void main(String args[]) {
//        System.out.println("Welcome to My Domains");
//        var cert = new CertificateManager();
//    }
//
//}
