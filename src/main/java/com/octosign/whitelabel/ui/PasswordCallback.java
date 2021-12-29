package com.octosign.whitelabel.ui;

import java.util.Optional;

import com.octosign.whitelabel.error_handling.UserException;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

import eu.europa.esig.dss.token.PasswordInputCallback;

import static com.octosign.whitelabel.ui.I18n.translate;

public class PasswordCallback implements PasswordInputCallback {
    @Override
    public char[] getPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(translate("text.enterPassword"));
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

        dialog.setOnShown(event -> {
            Platform.runLater(pwd::requestFocus);
            event.consume();
        });
        Optional<String> result = dialog.showAndWait();

        return result.isPresent() ? result.get().toCharArray() : "".toCharArray();
    }

    public String getPasswordString() {
        var input = this.getPassword();
        if (input == null)
            throw new UserException("error.nullPassword.header", "error.nullPassword.description");

        return new String(input);
    }
}
