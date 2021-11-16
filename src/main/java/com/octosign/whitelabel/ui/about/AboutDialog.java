package com.octosign.whitelabel.ui.about;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.octosign.whitelabel.ui.FXUtils;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;

import com.octosign.whitelabel.ui.Main;

import static com.octosign.whitelabel.ui.I18n.translate;

public class AboutDialog extends Dialog<Boolean> {
    public AboutDialog() {
        super();
        setTitle(translate("text.aboutHelp.header"));
        setContentText(getContent());

        FXUtils.addCustomStyles(this);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(520);
        getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonData.OK_DONE));
    }

    private static String getContent() {
        // TODO This should be properly styled FXML with clickable links

        String appDescription = translate("text.aboutHelp.description", Main.getVersion(), translate("app.author"));

        String usedOSS = getUsedOSS()
            .stream()
            .map((dep) -> "- " + dep)
            .collect(Collectors.joining("\n"));

        return String.join(
            "\n",
            translate("app.name"),
            appDescription,
            "Help: " + translate("app.website"),
            "",
            translate("text.aboutHelp.license"),
            "",
            translate("text.aboutHelp.usedOSS"),
            translate("text.aboutHelp.javaInfo"),
            translate("text.aboutHelp.depsInfo"),
            usedOSS,
            translate("text.aboutHelp.moreLegalInfo")
        );
    }

    private static List<String> getUsedOSS() {
        return Arrays.asList(
            "Digital Signature Service 5.8 - github.com/esig/dss - CEF Digital - LGPL-2.1",
            "Gson 2 - github.com/google/gson - Google Inc. - Apache 2"
        );
    }
}
