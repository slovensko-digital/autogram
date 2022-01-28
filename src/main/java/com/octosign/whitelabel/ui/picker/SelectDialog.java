package com.octosign.whitelabel.ui.picker;

import com.octosign.whitelabel.signing.Driver;
import com.octosign.whitelabel.signing.OperatingSystem;
import com.octosign.whitelabel.signing.token.Token;
import com.octosign.whitelabel.ui.utils.FXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.octosign.whitelabel.signing.KeystoreType.PKCS11;
import static com.octosign.whitelabel.signing.KeystoreType.PKCS12;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.utils.FXUtils.getCurrentStage;

public class SelectDialog<T extends SelectableItem> extends Stage {

    private final List<ToggleButton> buttons = new ArrayList<>();
    private final ToggleGroup group = new ToggleGroup();
    private final Button continueButton;

    private T selectedItem = null;

    public SelectDialog(@Nonnull List<T> items, @Nonnull Window parent) {
        super();
        var vbox = new VBox();

        var titleLabel = new Label(translate("text.pickCertificate"));
        vbox.getChildren().add(titleLabel);

        var driverButtons = items.stream().map(this::createButton).toList();
        buttons.addAll(driverButtons);
        buttons.add(createDriverFileChooserButton());
        vbox.getChildren().addAll(buttons);

        continueButton = new Button(translate("btn.continue"));
        continueButton.setDisable(true);
        continueButton.setOnAction(__ -> close());
        vbox.getChildren().add(continueButton);

        group.selectedToggleProperty().addListener(__ -> continueButton.setDisable(false));

        var scene = new Scene(vbox);
        // TODO to fxml
        scene.getStylesheets().addAll(FXUtils.getStylesheets());
        initOwner(parent);
        initStyle(StageStyle.UNDECORATED);
        initModality(Modality.WINDOW_MODAL);
        setScene(scene);
    }

    private ToggleButton createButton(T item) {
        var button = new ToggleButton(item.getDisplayedDetails());
        button.setToggleGroup(group);
        button.setSelected(false);
        button.setUserData(item);

        // TODO to fxml
        button.setMinSize(148, 148);
        button.setMaxSize(148, 148);
        button.setPadding(new Insets(32, 48, 32, 48));
        button.setFont(Font.font("DejaVu Sans", 20));
        button.setMaxWidth(Double.MAX_VALUE);

        button.setOnAction(e -> {
            buttons.forEach(b -> b.getStyleClass().remove("button-pick-selected"));
            selectedItem = item;
            button.getStyleClass().add("button-pick-selected");
        });

        return button;
    }

    private ToggleButton createDriverFileChooserButton() {
        var button = new ToggleButton(translate("btn.selectDriverPath"));
        button.setToggleGroup(group);
        button.setSelected(false);

        // TODO to fxml
        button.setMinSize(148, 148);
        button.setMaxSize(148, 148);
        button.setPadding(new Insets(32, 48, 32, 48));
        button.setFont(Font.font("DejaVu Sans", 20));
        button.setMaxWidth(Double.MAX_VALUE);

        button.setOnAction(e -> {
            buttons.forEach(b -> b.getStyleClass().remove("button-pick-selected"));
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(translate("text.manualDriverSelection"));
            File file = fileChooser.showOpenDialog(getCurrentStage(button));

            if (file != null) {
                boolean isPkcs12 = file.getPath().endsWith(".p12");
                var driver = Driver.name(file.getName())
                                 .tokenType(translate("text.manuallySelected"))
                                 .file(OperatingSystem.current(), file.getPath())
                                 .keystore(isPkcs12 ? PKCS12 : PKCS11);

                button.setText(driver.getDisplayedDetails());
                Token.addDriver(driver);
                selectedItem = (T) driver;
                button.getStyleClass().add("button-pick-selected");
            }
        });

        return button;
    }

    public T getResult() {
        selectedItem = null;
        showAndWait();

        return selectedItem;
    }
}
