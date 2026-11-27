package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.TokenCodes;
import digital.slovensko.autogram.core.TokenCodes.Problem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;

public class PasswordController extends BaseController {
    /**
     * Message keys of the dialog: the question, an optional hint under it and the errors for a code breaking the
     * token's rules (the one for a wrong length gets the length as an argument).
     */
    public record Texts(String question, String subtitle, String empty, String notDigits, String wrongLength) {
    }

    private final Texts texts;
    private final boolean isSigningStep;
    private final boolean allowEmpty;
    private final TokenCodes codes;

    private char[] password;

    @FXML
    PasswordField passwordField;
    @FXML
    Text question;
    @FXML
    Text subtitle;
    @FXML
    Text error;
    @FXML
    VBox formGroup;
    @FXML
    Button mainButton;
    @FXML
    Button cancelButton;
    @FXML
    VBox mainBox;

    public PasswordController(Texts texts, boolean isSigningStep, boolean allowEmpty, TokenCodes codes) {
        this.texts = texts;
        this.isSigningStep = isSigningStep;
        this.allowEmpty = allowEmpty;
        this.codes = codes;
    }

    @Override
    public void initialize() {
        question.setText(i18n(texts.question()));
        if (texts.subtitle() != null) {
            subtitle.setText(i18n(texts.subtitle()));
            subtitle.setManaged(true);
            subtitle.setVisible(true);
        }

        // a letter typed instead of a digit never gets in, the user wouldn't see it and the card would count a wrong code
        passwordField.setTextFormatter(new TextFormatter<String>(this::keepAllowed));

        if(isSigningStep) {
            mainButton.setText(i18n("general.sign.btn.single"));
            cancelButton.setManaged(true);
            cancelButton.setVisible(true);
        }
    }

    private TextFormatter.Change keepAllowed(TextFormatter.Change change) {
        if (!change.isContentChange())
            return change;

        var kept = codes.keepAllowed(change.getText());
        if (!kept.removedSomething())
            return change;

        var caret = change.getRangeStart() + kept.kept().length();
        change.setText(kept.kept());
        change.selectRange(caret, caret);
        showError(errorText(Problem.NOT_DIGITS));
        return change;
    }

    public void onPasswordAction() {
        var code = passwordField.getText().toCharArray();
        var problem = codes.check(code, allowEmpty);
        if (problem.isPresent()) {
            Arrays.fill(code, '\0');
            showError(errorText(problem.get()));
            passwordField.requestFocus();
            return;
        }

        this.password = code;
        GUIUtils.closeWindow(mainBox);
    }

    private String errorText(Problem problem) {
        return switch (problem) {
            case EMPTY -> i18n(texts.empty());
            case NOT_DIGITS -> i18n(texts.notDigits());
            case WRONG_LENGTH -> i18n(texts.wrongLength(), codes.length());
        };
    }

    public void showIncorrectPIN() {
        showError(i18n("password.context.incorrect.text"));
    }

    private void showError(String text) {
        error.setText(text);
        error.setManaged(true);
        error.setVisible(true);
        if (!formGroup.getStyleClass().contains("autogram-form-group--error"))
            formGroup.getStyleClass().add("autogram-form-group--error");
        if (!passwordField.getStyleClass().contains("autogram-input--error"))
            passwordField.getStyleClass().add("autogram-input--error");

        // not shown yet when the previous PIN was wrong
        if (formGroup.getScene() != null && formGroup.getScene().getWindow() != null)
            formGroup.getScene().getWindow().sizeToScene();
    }

    public void onCancelButtonPressed(ActionEvent event) {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    public char[] getPassword() {
        return password;
    }
}
