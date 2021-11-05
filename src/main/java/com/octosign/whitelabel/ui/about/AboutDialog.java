package com.octosign.whitelabel.ui.about;

import com.octosign.whitelabel.ui.FX;
import com.octosign.whitelabel.ui.Main;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.octosign.whitelabel.ui.Main.translate;

public class AboutDialog extends Dialog<Boolean> {
    public AboutDialog() {
        super();
        setTitle(translate("txt.aboutHelp"));
        setContentText(getContent());

        FX.addStylesheets(this);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(520);
        getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonData.OK_DONE));
    }

    private static String getContent() {
        // TODO This should be properly styled FXML with clickable links

        String appDescription = String.format(
                translate("txt.aboutHelp.description"),
                Main.getVersion(),
                translate("app.author")
        );

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
                translate("txt.aboutHelp.license"),
                "",
                translate("txt.aboutHelp.usedOSS"),
                translate("txt.aboutHelp.javaInfo"),
                translate("txt.aboutHelp.depsInfo"),
                usedOSS,
                translate("txt.aboutHelp.moreLegalInfo")
        );
    }

    private static List<String> getUsedOSS() {
        return Arrays.asList(
                "Digital Signature Service 5.8 - github.com/esig/dss - CEF Digital - LGPL-2.1",
                "Gson 2 - github.com/google/gson - Google Inc. - Apache 2"
        );
    }
}
