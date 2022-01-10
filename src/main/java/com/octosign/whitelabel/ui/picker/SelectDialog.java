package com.octosign.whitelabel.ui.picker;

import com.octosign.whitelabel.ui.utils.FXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import javax.annotation.Nonnull;
import java.util.List;

import static com.octosign.whitelabel.ui.utils.I18n.translate;

public class SelectDialog<T extends SelectableItem> extends Stage {

    private final List<ToggleButton> buttons;
    private final ToggleGroup group = new ToggleGroup();
    private final Button continueButton;

    private T selectedItem = null;

    public SelectDialog(@Nonnull List<T> items, @Nonnull Window parent) {
        super();
        var vbox = new VBox();

        var titleLabel = new Label(translate("text.pickCertificate"));
        vbox.getChildren().add(titleLabel);

        buttons = items.stream().map(this::createButton).toList();
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

    public T getResult() {
        selectedItem = null;
        showAndWait();

        return selectedItem;
    }
}
