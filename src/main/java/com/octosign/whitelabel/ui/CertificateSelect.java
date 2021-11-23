package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.signing.SigningCertificate;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.annotation.Nonnull;
import java.util.List;

public class CertificateSelect extends Stage implements SelectCallback<DSSPrivateKeyEntry> {

    private DSSPrivateKeyEntry selected = null;

    public CertificateSelect(@Nonnull List<DSSPrivateKeyEntry> elements) {
        super();
        var vbox = new VBox();
        var buttons = elements.stream().map(this::createButton).toList();
        vbox.getChildren().addAll(buttons);

        var scene = new Scene(vbox);
        scene.getStylesheets().addAll(FXUtils.getStylesheets());

//        initOwner(parent);
        initStyle(StageStyle.UNDECORATED);
        initModality(Modality.WINDOW_MODAL);
        setScene(scene);
    }

    private Button createButton(DSSPrivateKeyEntry element) {
        var button = new Button((SigningCertificate.getNicePrivateKeyDescription(element, SigningCertificate.KeyDescriptionVerbosity.SHORT)));
        button.setPadding(new Insets(32, 48, 32, 48));
        button.setFont(Font.font("DejaVu Sans", 20));

        button.setOnAction(e -> {
            selected = element;
            close();
        });

        return button;
    }

    public DSSPrivateKeyEntry showDialog() {
        selected = null;
        showAndWait();

        return selected;
    }

    @Override
    public DSSPrivateKeyEntry select() {
        return showDialog();
    }
}
