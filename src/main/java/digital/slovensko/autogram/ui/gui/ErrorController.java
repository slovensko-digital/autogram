package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ErrorController extends BaseController implements SuppressedFocusController {
    private final AutogramException exception;
    private final boolean errorDetailsDisabled;

    @FXML
    VBox mainBox;

    @FXML
    private Button mainButton;

    @FXML
    ErrorSummaryComponentController errorSummaryComponentController; // this is {include fx:id} + "Controller"

    public ErrorController(AutogramException e) {
        this.exception = e;
        this.errorDetailsDisabled = false;
    }

    public ErrorController(AutogramException e, boolean errorDetailsDisabled) {
        this.exception = e;
        this.errorDetailsDisabled = errorDetailsDisabled;
    }

    @Override
    public void initialize() {
        errorSummaryComponentController.setException(exception);

        if (errorDetailsDisabled)
            errorSummaryComponentController.disableErrorDetails();
    }

    public void onMainButtonAction() {
        ((Stage) mainBox.getScene().getWindow()).close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public void setMainButtonText(String buttonI18nKey) {
        mainButton.setText(i18n(buttonI18nKey));
    }

}
