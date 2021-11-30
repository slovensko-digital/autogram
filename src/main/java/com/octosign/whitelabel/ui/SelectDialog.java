package com.octosign.whitelabel.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import javax.annotation.Nonnull;
import java.util.List;

public class SelectDialog<T extends SelectableItem> extends Stage {

    private T selectedItem = null;

    public SelectDialog(@Nonnull List<T> items, @Nonnull Window parent) {
        super();
        var vbox = new VBox();
        var buttons = items.stream().map(this::createButton).toList();
        vbox.getChildren().addAll(buttons);

        var scene = new Scene(vbox);
        // TODO to fxml
        scene.getStylesheets().addAll(FXUtils.getStylesheets());

        initOwner(parent);
        initStyle(StageStyle.UNDECORATED);
        initModality(Modality.WINDOW_MODAL);
        setScene(scene);
    }

    private Button createButton(T item) {
        var button = new Button(item.getSimpleName());
        // TODO to fxml
        button.setPadding(new Insets(32, 48, 32, 48));
        button.setFont(Font.font("DejaVu Sans", 20));

        button.setOnAction(e -> {
            selectedItem = item;
            close();
        });

        return button;
    }

    public T getResult() {
        selectedItem = null;
        showAndWait();

        return selectedItem;
    }
}
