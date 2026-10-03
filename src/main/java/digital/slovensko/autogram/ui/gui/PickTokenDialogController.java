package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.DefaultDriverDetector.TokenDriverShortnames;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.ui.TokenOptionDescription;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class PickTokenDialogController extends BaseController {
    private static final double MAX_OPTIONS_HEIGHT = 420;
    // room for the focus ring around a radio button when scrolling to it
    private static final double FOCUS_MARGIN = 8;

    private final TokenOptions options;
    private final TokenOption preferredOption;
    private final Consumer<TokenOption> callback;
    private RadioButton preferredRadioButton;

    @FXML
    VBox mainBox;
    @FXML
    VBox formGroup;
    @FXML
    Text error;
    @FXML
    TextFlow noTokensFound;
    @FXML
    ScrollPane optionsScrollPane;
    @FXML
    VBox radios;
    @FXML
    Label divider;
    @FXML
    VBox otherDriverRadios;
    @FXML
    VBox driverRadios;
    @FXML
    Button showOtherDriversButton;
    private ToggleGroup toggleGroup;
    private static final String OPTION_NODE_KEY = "optionNode";
    private final List<RadioButton> hintedRadioButtons = new ArrayList<>();

    /**
     * @param preferredOption option to preselect, e.g. the last used one, null to preselect the first one
     */
    public PickTokenDialogController(TokenOptions options, TokenOption preferredOption, Consumer<TokenOption> callback) {
        this.options = options;
        this.preferredOption = preferredOption;
        this.callback = callback;
    }

    @Override
    public void initialize() {
        toggleGroup = new ToggleGroup();
        // radio buttons are created in the order they are shown, arrow keys move through them in this order
        var tokens = options.found().stream().filter(TokenOption::isToken).toList();
        var descriptions = TokenOptionDescription.describeAll(tokens);
        for (int i = 0; i < tokens.size(); i++)
            radios.getChildren().add(createOptionNode(tokens.get(i), descriptions.get(i)));

        for (var driver : options.otherDrivers())
            otherDriverRadios.getChildren().add(createRadioButton(driver.getName(), new TokenOption(driver, null)));

        // keystore file is always the last choice, it's the least common one
        options.found().stream()
                .filter((option) -> !option.isToken())
                .sorted(Comparator.comparing(PickTokenDialogController::isKeystore))
                .forEach((option) -> driverRadios.getChildren().add(createRadioButton(option.driver().getName(), option)));

        // without any card found, the user has to pick from other drivers anyway
        var noTokens = !options.hasTokens() && !options.otherDrivers().isEmpty();
        setShown(noTokensFound, noTokens);
        setShown(radios, !tokens.isEmpty());
        setShown(driverRadios, !driverRadios.getChildren().isEmpty());
        setShown(otherDriverRadios, noTokens);
        // hidden radio buttons are still in the toggle group, arrow keys would select them unless disabled
        otherDriverRadios.setDisable(!noTokens);
        setShown(showOtherDriversButton, !noTokens && !options.otherDrivers().isEmpty());

        // the preferred option may be among other drivers, show them then
        if (preferredRadioButton != null && otherDriverRadios.getChildren().contains(preferredRadioButton)) {
            setShown(showOtherDriversButton, false);
            setShown(otherDriverRadios, true);
            otherDriverRadios.setDisable(false);
        }
        updateDivider();

        // focused option is always the selected one, e.g. moving through options with arrow keys
        mainBox.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null)
                scene.focusOwnerProperty().addListener((o, oldOwner, owner) -> onFocusOwnerChanged(owner));
        });
    }

    private void onFocusOwnerChanged(Node owner) {
        if (!(owner instanceof RadioButton radioButton) || radioButton.getToggleGroup() != toggleGroup)
            return;

        radioButton.setSelected(true);
        scrollIntoView((Node) radioButton.getProperties().getOrDefault(OPTION_NODE_KEY, radioButton));
    }

    private Node createOptionNode(TokenOption option, TokenOptionDescription description) {
        var radioButton = createRadioButton(getLabel(description), option);
        if (description.readerName() == null || description.readerName().isEmpty())
            return radioButton;

        var hint = new TextFlow(new Text(i18n("pickDriver.token.reader", description.readerName())));
        hint.getStyleClass().add("autogram-radio-hint");
        hintedRadioButtons.add(radioButton);
        var optionNode = new VBox(radioButton, hint);
        radioButton.getProperties().put(OPTION_NODE_KEY, optionNode);
        return optionNode;
    }

    private String getLabel(TokenOptionDescription description) {
        var parts = new ArrayList<String>();
        parts.add(description.driverName());
        if (description.slotNumber() != null)
            parts.add(i18n("pickDriver.token.slot", description.slotNumber()));

        return String.join(", ", parts);
    }

    private static boolean isKeystore(TokenOption option) {
        return option.driver().getShortname().equals(TokenDriverShortnames.KEYSTORE);
    }

    // GOV.UK radios divider between detected cards and other choices
    private void updateDivider() {
        setShown(divider, radios.isVisible() && (driverRadios.isVisible() || otherDriverRadios.isVisible()));
    }

    private RadioButton createRadioButton(String text, TokenOption option) {
        var radioButton = new RadioButton(text);
        radioButton.setMnemonicParsing(false); // token labels often contain underscores, e.g. SIG_EP
        radioButton.setWrapText(true);
        radioButton.setToggleGroup(toggleGroup);
        radioButton.setUserData(option);
        if (option.equals(preferredOption))
            preferredRadioButton = radioButton;

        return radioButton;
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    /**
     * Call when the dialog is shown.
     */
    public void onShown() {
        fitOptionsToContent();
        // focusing selects it and scrolls to it
        var focused = preferredRadioButton != null ? preferredRadioButton : getFirstShownRadioButton();
        if (focused != null)
            focused.requestFocus();
    }

    private RadioButton getFirstShownRadioButton() {
        return toggleGroup.getToggles().stream()
                .map((toggle) -> (RadioButton) toggle)
                .filter((radioButton) -> !radioButton.isDisabled() && radioButton.getParent().isVisible())
                .findFirst()
                .orElse(null);
    }

    /**
     * Sizes the list of options to its content, up to a limit. Call when the dialog is shown, JavaFX can't tell the
     * height of wrapped texts before it knows the width.
     */
    public void fitOptionsToContent() {
        mainBox.applyCss();
        mainBox.layout();
        alignHints();
        var content = (Region) optionsScrollPane.getContent();
        var contentHeight = content.prefHeight(optionsScrollPane.getViewportBounds().getWidth());
        optionsScrollPane.setPrefViewportHeight(Math.min(contentHeight, MAX_OPTIONS_HEIGHT));
        mainBox.getScene().getWindow().sizeToScene();
    }

    private void scrollIntoView(Node node) {
        var content = optionsScrollPane.getContent();
        var contentHeight = content.getLayoutBounds().getHeight();
        var viewportHeight = optionsScrollPane.getViewportBounds().getHeight();
        var scrollableHeight = contentHeight - viewportHeight;
        if (scrollableHeight <= 0)
            return;

        var bounds = content.sceneToLocal(node.localToScene(node.getLayoutBounds()));
        var top = bounds.getMinY() - FOCUS_MARGIN;
        var bottom = bounds.getMaxY() + FOCUS_MARGIN;
        var viewportTop = optionsScrollPane.getVvalue() * scrollableHeight;

        if (top < viewportTop)
            optionsScrollPane.setVvalue(Math.max(0, top / scrollableHeight));
        else if (bottom > viewportTop + viewportHeight)
            optionsScrollPane.setVvalue(Math.min(1, (bottom - viewportHeight) / scrollableHeight));
    }

    // shows as much of the node as fits, starting from its top
    private void scrollToTop(Node node) {
        var content = optionsScrollPane.getContent();
        var scrollableHeight = content.getLayoutBounds().getHeight() - optionsScrollPane.getViewportBounds().getHeight();
        if (scrollableHeight <= 0)
            return;

        var bounds = content.sceneToLocal(node.localToScene(node.getLayoutBounds()));
        optionsScrollPane.setVvalue(Math.clamp((bounds.getMinY() - FOCUS_MARGIN) / scrollableHeight, 0, 1));
    }

    // hints start where the radio button label text does, which depends on the skin and font size
    private void alignHints() {
        for (var radioButton : hintedRadioButtons) {
            var text = radioButton.lookup(".text");
            if (text == null)
                continue;

            var option = (VBox) radioButton.getParent();
            var textX = option.sceneToLocal(text.localToScene(text.getBoundsInLocal())).getMinX();
            var hint = (TextFlow) option.getChildren().get(1);
            hint.setPadding(new Insets(0, 0, 0, textX));
        }

        mainBox.layout();
    }

    public void onShowOtherDriversButtonAction() {
        setShown(showOtherDriversButton, false);
        setShown(otherDriverRadios, true);
        otherDriverRadios.setDisable(false);
        updateDivider();
        fitOptionsToContent();
        // the user wants to see the other drivers, they may be below the fold
        optionsScrollPane.layout();
        scrollToTop(otherDriverRadios);
    }

    public void onPickButtonAction() {
        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            GUIUtils.closeWindow(mainBox);
            var option = (TokenOption) toggleGroup.getSelectedToggle().getUserData();
            callback.accept(option);
        }
    }
}
