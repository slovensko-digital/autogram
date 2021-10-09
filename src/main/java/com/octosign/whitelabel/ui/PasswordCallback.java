package com.octosign.whitelabel.ui;

import eu.europa.esig.dss.token.PasswordInputCallback;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class PasswordCallback implements PasswordInputCallback {
    @Override
    public char[] getPassword() {
        Dialog<String> dialog = new Dialog<>();
        // TODO: Replace with message from .properties
        dialog.setTitle("Zadajte heslo");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
    
        PasswordField pwd = new PasswordField();
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(8);
        content.getChildren().add(pwd);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return pwd.getText();
            }
            return null;
        });
    
        Optional<String> result = dialog.showAndWait();

        return result.isPresent() ? result.get().toCharArray() : "".toCharArray();
    }
}
