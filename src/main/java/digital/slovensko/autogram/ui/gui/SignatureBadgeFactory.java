package digital.slovensko.autogram.ui.gui;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public abstract class SignatureBadgeFactory {
    public static VBox createInProgressBadge() {
        var text = new Text("Prebieha overovanie...");
        text.getStyleClass().addAll("autogram-heading-s", "autogram-tag-processing--text");
        text.getStyleClass().add("autogram-heading-s");

        var box = new VBox(text);
        box.getStyleClass().addAll("autogram-tag", "autogram-tag-processing");

        var validFlow = new TextFlow(box);
        validFlow.getStyleClass().add("autogram-summary-header__badge");

        return box;
    }

    public static VBox createInvalidBadge() {
        return createInvalidBadge("Neplatný");
    }

    public static VBox createInvalidBadge(String value) {
        var text = new Text(value);
        text.getStyleClass().addAll("autogram-heading-s", "autogram-tag-invalid--text");
        text.getStyleClass().add("autogram-heading-s");

        var box = new VBox(text);
        box.getStyleClass().addAll("autogram-tag", "autogram-tag-invalid");

        var validFlow = new TextFlow(box);
        validFlow.getStyleClass().add("autogram-summary-header__badge");

        return box;
    }

    public static VBox createValidBadge() {
        return createValidBadge("Platný");
    }

    public static VBox createValidBadge(String value) {
        var text = new Text(value);
        text.getStyleClass().addAll("autogram-heading-s", "autogram-tag-valid--text");
        text.getStyleClass().add("autogram-heading-s");

        var box = new VBox(text);
        box.getStyleClass().addAll("autogram-tag", "autogram-tag-valid");

        var validFlow = new TextFlow(box);
        validFlow.getStyleClass().add("autogram-summary-header__badge");

        return box;
    }
}
