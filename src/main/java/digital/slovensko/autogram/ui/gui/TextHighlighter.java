package digital.slovensko.autogram.ui.gui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;

public class TextHighlighter {

    private final String textToHighlight;
    private final String styleClass;

    private TextHighlighter(String textToHighlight, String styleClass) {
        this.textToHighlight = textToHighlight;
        this.styleClass = styleClass;
    }

    public static TextHighlighter highlight(String textToHighlight) {
        return new TextHighlighter(textToHighlight, "autogram-claim");
    }

    /**
     * Shows the text as code, e.g. a command, in monospace on a grey background
     */
    public static TextHighlighter code(String code) {
        return new TextHighlighter(code, "autogram-code");
    }

    /**
     * Splits the {@link Text} children of the given {@code textFlow} to highlight the given text.
     * Keeps the {@link Text#getStyleClass() styleClass} and {@link Text#getStyle() style} of the original children.
     * @param textFlow parent of Text nodes that will be modified
     */
    public void in(TextFlow textFlow) {
        var newChildren = textFlow.getChildren().stream()
                .flatMap(child -> {
                    if (child instanceof Text text) {
                        return splitWhereHighlighted(text);
                    }

                    return Stream.of(child);
                }).toList();

        textFlow.getChildren().clear();
        textFlow.getChildren().addAll(newChildren);
    }

    private Stream<Node> splitWhereHighlighted(Text originalNode) {
        if (!originalNode.getText().contains(textToHighlight)) {
            return Stream.of(originalNode);
        }

        var beforeOrAfterHighlighted = format("(?={0})|(?<={0})", textToHighlight);
        return stream(originalNode.getText().split(beforeOrAfterHighlighted))
                .map(textFragment -> {
                    if (textFragment.equals(textToHighlight)) {
                        var highlightedNode = new Label(textToHighlight);
                        highlightedNode.getStyleClass().add(styleClass);
                        highlightedNode.getStyleClass().addAll(originalNode.getStyleClass());
                        highlightedNode.setStyle(originalNode.getStyle());
                        return highlightedNode;
                    }

                    var textNode = new Text(textFragment);
                    textNode.getStyleClass().addAll(originalNode.getStyleClass());
                    textNode.setStyle(originalNode.getStyle());
                    return textNode;
                });
    }
}
