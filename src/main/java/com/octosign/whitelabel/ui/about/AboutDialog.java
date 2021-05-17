package com.octosign.whitelabel.ui.about;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.octosign.whitelabel.ui.Main;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.Region;

public class AboutDialog extends Dialog<Boolean> {
    public AboutDialog() {
        super();

        setTitle(Main.getProperty("text.aboutHelp"));
        addStylesheets();
        setContentText(getContent());
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(520);
        getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonData.OK_DONE));
    }

    private void addStylesheets() {
        var stylesheets = getDialogPane().getStylesheets();
        stylesheets.add(Main.class.getResource("shared.css").toExternalForm());
        stylesheets.add(Main.class.getResource("dialog.css").toExternalForm());
        stylesheets.add(Main.class.getResource("overrides.css").toExternalForm());
    }

    private static String getContent() {
        // TODO This should be properly styled FXML with clickable links

        String appDescription = String.format(
            Main.getProperty("text.aboutHelp.description"),
            Main.getVersion(),
            Main.getProperty("application.author")
        );

        String usedOSS = getUsedOSS()
            .stream()
            .map((dep) -> "- " + dep)
            .collect(Collectors.joining("\n"));

        return String.join(
            "\n",
            Main.getProperty("application.name"),
            appDescription,
            "Help: " + Main.getProperty("application.website"),
            "",
            Main.getProperty("text.aboutHelp.license"),
            "",
            Main.getProperty("text.aboutHelp.usedOSS"),
            Main.getProperty("text.aboutHelp.javaInfo"),
            Main.getProperty("text.aboutHelp.depsInfo"),
            usedOSS,
            Main.getProperty("text.aboutHelp.moreLegalInfo")
        );
    }

    private static List<String> getUsedOSS() {
        return Arrays.asList(new String[]{
            "Digital Signature Service 5.8 - github.com/esig/dss - CEF Digital - LGPL-2.1",
            "Gson 2 - github.com/google/gson - Google Inc. - Apache 2"
        });
    }
}
